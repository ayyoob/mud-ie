package com.mudie.sdn.controller.mgt.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SequenceWriter;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.mudie.common.config.Controller;
import com.mudie.sdn.controller.mgt.impl.faucet.*;
import com.mudie.sdn.controller.mgt.HostInfo;
import com.mudie.sdn.controller.mgt.OFController;
import com.mudie.common.openflow.OFFlow;
import com.mudie.sdn.controller.mgt.exception.OFControllerException;
import com.mudie.sdn.controller.mgt.internal.SdnControllerDataHolder;
import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.Query;
import org.influxdb.dto.QueryResult;
import org.influxdb.impl.InfluxDBResultMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FaucetOFControllerImpl implements OFController {
	private static final String DEFAULT_ALLOW_ACL_NAME = "default-allow-acl";
	public static final int DEFAULT_MIRROR_PORT = 5;
	private static final String DEVICE_ACL_POSTFIX = "-acl";
	private static final String SWITCH_ACL_FILE_POSTFIX = "-acl-mud.yaml";
	private static final String DEVICE_ACL_FILE_POSTFIX = "-device-acl-mud.yaml";
	private static final Logger log = LoggerFactory.getLogger(FaucetOFControllerImpl.class);
	private static final String FAUCET_REACTIVE_FLOW_SUBJECT = "faucet.msg";
	private static final String FAUCET_CONFIG_DIR = "faucet.config.dir.path";
	private static String faucetConfigPath;
	private static int cookie = 7730494;
	private static long OFPP_NORMAL = 4294967290L;
	private static String OUTPUT_ACTION = "OUTPUT";
	private static InfluxDB influxDB;
	private static final String DB_NAME = "dbName";
	private static final String DB_USERNAME = "username";
	private static final String DB_PASSWORD = "password";
	private static final String DB_URL = "dbUrl";
	private static String dbname;

	public FaucetOFControllerImpl() {
		faucetConfigPath = System.getProperty(FAUCET_CONFIG_DIR);
	}

	@Override
	public void addFlow(String dpId, OFFlow ofFlow) throws OFControllerException {
		ModFlowMsg addFlowMsg = new ModFlowMsg();
		addFlowMsg.setDpid(Long.decode("0x"+dpId));
		addFlowMsg.setCookie(cookie);
		addFlowMsg.setPriority(ofFlow.getPriority());
		Rule rule = new Rule();
		rule.setOFFlow(ofFlow);

		List<Action> actions = new ArrayList<>();
		if (ofFlow.getOfAction() == OFFlow.OFAction.NORMAL) {
			Action action = new Action();
			action.setType(OUTPUT_ACTION);
			action.setPort(OFPP_NORMAL);
			actions.add(action);
		} else {
			throw new UnsupportedOperationException();
		}

		addFlowMsg.setActions(actions);
		rule.setActions(null);
		addFlowMsg.setMatch(rule);
		addFlowMsg.setIdleTimeout(ofFlow.getIdleTimeOutInSeconds());
		ObjectMapper mapper = new ObjectMapper();
		try {
			String msg = mapper.writeValueAsString(addFlowMsg);
			SdnControllerDataHolder.getNatsClient().publish(FAUCET_REACTIVE_FLOW_SUBJECT, msg);
		} catch (IOException e) {
			throw new OFControllerException(e);
		}

	}

	@Override
	public void addFlows(String dpId, List<OFFlow> ofFlows) throws OFControllerException {


	}

	@Override
	public void removeFlow(String dpId, OFFlow ofFlow) throws OFControllerException {

	}

	@Override
	public void removeFlows(String dpId, List<OFFlow> ofFlows) throws OFControllerException {

	}

	@Override
	public void addACLs(String dpId, String deviceMac, List<OFFlow> ofFlows, int vlan) throws OFControllerException {
		if (ofFlows == null) {
			return;
		}
		deviceMac = deviceMac.replace(":", "");
		String deviceFile = deviceMac + DEVICE_ACL_FILE_POSTFIX;

		String path = faucetConfigPath + File.separator + dpId + SWITCH_ACL_FILE_POSTFIX;
		File switchMudConfig = new File(path);
		if (!switchMudConfig.exists()) {
			SwitchFaucetConfig switchFaucetConfig = new SwitchFaucetConfig();
			List<String> deviceList = new ArrayList<>();
			deviceList.add(deviceFile);
			switchFaucetConfig.setInclude(deviceList);
			Map<Integer, AclsIn> vlanMap = new HashMap<>();
			AclsIn aclsIn = new AclsIn();
			List<String> acls = new ArrayList<>();
			acls.add(deviceMac + DEVICE_ACL_POSTFIX);
			acls.add(DEFAULT_ALLOW_ACL_NAME);
			aclsIn.setAclsIn(acls);
			vlanMap.put(vlan, aclsIn);
			switchFaucetConfig.setVlans(vlanMap);
			writeYamlToFile(switchMudConfig, switchFaucetConfig);

		} else {
			ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
			SwitchFaucetConfig switchFaucetConfig = null;
			try {
				switchFaucetConfig = mapper.readValue(switchMudConfig, SwitchFaucetConfig.class);
			} catch (IOException e) {
				throw new OFControllerException(e);
			}
			if (!switchFaucetConfig.getInclude().contains(deviceFile)) {
				switchFaucetConfig.getInclude().add(deviceFile);
			}
			AclsIn aclsIn = switchFaucetConfig.getVlans().get(vlan);
			if (aclsIn == null) {
				aclsIn = new AclsIn();
				List<String> acls = new ArrayList<>();
				acls.add(deviceMac + DEVICE_ACL_POSTFIX);
				acls.add(DEFAULT_ALLOW_ACL_NAME);
				aclsIn.setAclsIn(acls);
			} else {
				if (!aclsIn.getAclsIn().contains(deviceMac + DEVICE_ACL_POSTFIX)) {
					aclsIn.getAclsIn().add(0, deviceMac + DEVICE_ACL_POSTFIX);
				}
			}
			switchFaucetConfig.getVlans().put(vlan, aclsIn);
			writeYamlToFile(switchMudConfig, switchFaucetConfig);
		}

		path = faucetConfigPath + File.separator + deviceFile;
		Acls acls = new Acls();
		List<RuleWrapper> ruleList = new ArrayList<RuleWrapper>();
		for (OFFlow ofFlow : ofFlows) {
			RuleWrapper ruleWrapper = new RuleWrapper();
			Rule rule = new Rule();
			rule.setOFFlow(ofFlow);
			ruleWrapper.setRule(rule);
			ruleList.add(ruleWrapper);

		}
		Map<String, List<RuleWrapper>> map = new HashMap<>();
		map.put(deviceMac + DEVICE_ACL_POSTFIX, ruleList);
		acls.setAcls(map);

		File deviceFaucetConfig = new File(path);
		writeDeviceYamlToFile(deviceFaucetConfig, acls);

//		try {
//			SdnControllerDataHolder.getNatsClient().publish(FAUCET_REACTIVE_FLOW_SUBJECT, "reload");
//		} catch (IOException e) {
//			log.error("failed to restart faucet", e);
//		}

	}

	@Override
	public HostInfo getHostInfo(String device) {
		L2LearnWrapper l2LearnWrapper = SdnControllerDataHolder.getL2LearnWrapperMap().get(device);
		if (l2LearnWrapper != null) {
			HostInfo hostInfo = new HostInfo();
			hostInfo.setDpId((Long.toHexString(l2LearnWrapper.getDpId())));
			hostInfo.setPortNo(l2LearnWrapper.getL2Learn().getPortNumber());
			hostInfo.setVlanId(l2LearnWrapper.getL2Learn().getVlanId());
			return hostInfo;
		}
		return null;
	}

	@Override
	public void clearAllFlows(String dpId) throws OFControllerException {

	}

	@Override
	public List<OFFlow> getFlowStats(String dpId) throws OFControllerException {
		return null;
	}

	@Override
	public List<OFFlow> getFilteredFlowStats(Object filter) throws OFControllerException {
		if (influxDB == null) {
			Controller controller = SdnControllerDataHolder.getController();
			influxDB = InfluxDBFactory.connect(controller.getProperties().get(DB_URL),
					controller.getProperties().get(DB_USERNAME), controller.getProperties().get(DB_PASSWORD));
			dbname = controller.getProperties().get(DB_NAME);
		}

		String deviceMac = (String) filter;
		// Run the query
		String query = "select * from flow_byte_count, flow_packet_count where (table_id = '0' or table_id = '2') and" +
				" (eth_src = '"+deviceMac+"' or eth_dst= '"+deviceMac+"') and time > now() - 1m ";
		Query queryObject = new Query(query, dbname);
		QueryResult queryResult = influxDB.query(queryObject);

		// Map it
		InfluxDBResultMapper resultMapper = new InfluxDBResultMapper();
		List<FaucetByteStatPoint> faucetByteStatPoints = resultMapper.toPOJO(queryResult, FaucetByteStatPoint.class);
		List<OFFlow> ofFlows = new ArrayList<>();
		Map<Integer, OFFlow> ofFlowMap = new HashMap<>();
		for (FaucetByteStatPoint faucetByteStatPoint: faucetByteStatPoints) {
			OFFlow ofFlow = faucetByteStatPoint.getOFFlow();
			ofFlows.add(ofFlow);
			ofFlowMap.put(ofFlow.hashCode(), ofFlow);
		}

		List<FaucetPacketStatPoint> faucePackettStatPoints = resultMapper.toPOJO(queryResult, FaucetPacketStatPoint.class);
		for (FaucetPacketStatPoint faucetPacketStatPoint: faucePackettStatPoints) {
			OFFlow ofFlow = faucetPacketStatPoint.getOFFlow();
			OFFlow existingFlow = ofFlowMap.get(ofFlow.hashCode());
			existingFlow.setPacketCount(ofFlow.getPacketCount());
		}
		return ofFlows;
	}

	@Override
	public Map<String, List<OFFlow>> getFlowStats() throws OFControllerException {
		return null;
	}

	private static void writeYamlToFile(File tobeWritten, SwitchFaucetConfig switchFaucetConfig) throws OFControllerException {
		try {
			//mapper.writeValue(switchMudConfig, switchFaucetConfig);
			ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
			FileOutputStream fos = new FileOutputStream(tobeWritten);
			SequenceWriter sw = mapper.writerWithDefaultPrettyPrinter().writeValues(fos);
			sw.write(switchFaucetConfig);
			sw.close();
		} catch (IOException e) {
			throw new OFControllerException(e);
		}
	}

	private static void writeDeviceYamlToFile
			(File tobeWritten, Acls acls) throws OFControllerException {
		try {
			//mapper.writeValue(switchMudConfig, switchFaucetConfig);
			ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
			FileOutputStream fos = new FileOutputStream(tobeWritten);
			SequenceWriter sw = mapper.writerWithDefaultPrettyPrinter().writeValues(fos);
			sw.write(acls);
			sw.close();
		} catch (IOException e) {
			throw new OFControllerException(e);
		}
	}
}
