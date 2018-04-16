package com.networkseer.sdn.controller.mgt.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SequenceWriter;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.networkseer.sdn.controller.mgt.HostInfo;
import com.networkseer.sdn.controller.mgt.OFController;
import com.networkseer.common.openflow.OFFlow;
import com.networkseer.sdn.controller.mgt.exception.OFControllerException;
import com.networkseer.sdn.controller.mgt.impl.faucet.*;
import com.networkseer.sdn.controller.mgt.internal.SdnControllerDataHolder;
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
	private static final String FAUCET_REACTIVE_FLOW_SUBJECT = "ryu.msg";
	private static final String FAUCET_CONFIG_DIR = "faucet.config.dir.path";
	private static String faucetConfigPath;

	public FaucetOFControllerImpl() {
		faucetConfigPath = System.getProperty(FAUCET_CONFIG_DIR);
	}

	@Override
	public void addFlow(String dpId, OFFlow ofFlow) throws OFControllerException {
		AddFlowMsg addFlowMsg = new AddFlowMsg();
		addFlowMsg.setDpId(dpId);
		Rule rule = new Rule();
		rule.setOFFlow(ofFlow);
		addFlowMsg.setRule(rule);
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
