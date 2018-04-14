package com.networkseer.mud.processor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.networkseer.common.SeerDirectory;
import com.networkseer.common.SeerUtil;
import com.networkseer.common.packet.PacketConstants;
import com.networkseer.mud.processor.dhcp.DHCP;
import com.networkseer.mud.processor.dhcp.DHCPOption;
import com.networkseer.mud.processor.mud.*;
import com.networkseer.common.openflow.OFFlow;
import com.networkseer.sdn.controller.mgt.exception.OFControllerException;
import com.networkseer.sdn.controller.mgt.impl.FaucetOFControllerImpl;
import com.networkseer.seer.mgt.dto.Device.Status;

import com.networkseer.common.packet.PacketListener;
import com.networkseer.common.packet.SeerPacket;
import com.networkseer.mud.processor.internal.MUDProcesserDataHolder;
import com.networkseer.seer.mgt.dto.Device;
import com.networkseer.seer.mgt.dto.DeviceRecord;
import com.networkseer.seer.mgt.dto.Switch;
import com.networkseer.seer.mgt.exception.SeerManagementException;
import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.pcap4j.packet.*;
import org.pcap4j.packet.namednumber.EtherType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MUDProcessorPacketListenerImpl implements PacketListener {
	private static final Logger log = LoggerFactory.getLogger(MUDProcessorPacketListenerImpl.class);
	private LinkedList<DeviceIdentifier> deviceQueue = new LinkedList<DeviceIdentifier>();
	private Map<String, DeviceMUDFlowMap> deviceFlowMapHolder = new HashMap<>();
	private static final int FIXED_LOCAL_COMMUNICATION = 5;
	private static final int DEFAULT_LOCAL_COMMUNICATION = 4;
	private static final int FIXED_INTERNET_COMMUNICATION = 10;
	private static final int DEFAULT_INTERNET_COMMUNICATION = 9;
	private static final int DYNAMIC_INTERNET_COMMUNICATION = 15000;
	private static final long IDLE_TIMEOUT_IN_SECONDS = 7200;
	private static final String MUD_URN = "urn:ietf:params:mud";


	public MUDProcessorPacketListenerImpl() {
		ScheduledExecutorService deviceExecutor = Executors.newScheduledThreadPool(1);

		Runnable task = () -> {
			DeviceIdentifier deviceIdentifier = deviceQueue.remove();

			while (deviceIdentifier != null) {
				try {
					DeviceRecord deviceRecord = MUDProcesserDataHolder.getSeerMgtService()
							.getDeviceRecord(deviceIdentifier.getVxlanId(), deviceIdentifier.getDeviceMac());
					if (deviceRecord.getDevice() == null || deviceRecord.getDevice().getStatus() != Status.IDENTIFIED) {
						String mudPayload = getMUDContent(deviceIdentifier.getMudUrl());
						Switch aSwitch = MUDProcesserDataHolder
								.getSeerMgtService().getSwitchFromVxlanId(deviceIdentifier.getVxlanId());
						int vLan = getDeviceVLAN(deviceIdentifier.getDeviceMac());
						Device device = new Device();
						device.setMac(deviceIdentifier.getDeviceMac());
						device.setSwitchId(aSwitch.getId());
						device.setStatus(Status.IDENTIFIED);
						DeviceMudWrapper deviceMudWrapper = new DeviceMudWrapper();
						deviceMudWrapper.setVlan(vLan);
						deviceMudWrapper.setMudProfile(mudPayload);
						device.setProperty(deviceMudWrapper.toString());
						MUDProcesserDataHolder.getSeerMgtService().addDevice(device);
						addMudConfigs(mudPayload, deviceMudWrapper.getVlan(), deviceIdentifier.getDeviceMac(),
								SeerUtil.getSwitchMacFromDpID(deviceRecord.getaSwitch().getDpId()));


					} else {
						String storedMudWrapperPayload = deviceRecord.getDevice().getProperty();
						DeviceMudWrapper deviceMudWrapper = new DeviceMudWrapper(storedMudWrapperPayload);
						if (deviceFlowMapHolder.get(deviceIdentifier.getDeviceMac()) == null) {
							String mudPayload = getMUDContent(deviceIdentifier.getMudUrl());
							int vLan = getDeviceVLAN(deviceIdentifier.getDeviceMac());
							if (mudPayload.equals(deviceMudWrapper.getMudProfile()) && deviceMudWrapper.getVlan() == vLan &&
									deviceRecord.getaSwitch().getDpId().contains(deviceIdentifier.getVxlanId())) {
								DeviceMUDFlowMap deviceMUDFlowMap = processMUD(deviceIdentifier.getDeviceMac(),
										SeerUtil.getSwitchMacFromDpID(deviceRecord.getaSwitch().getDpId()), mudPayload);
								deviceFlowMapHolder.put(deviceIdentifier.getDeviceMac(), deviceMUDFlowMap);
							} else {
								removeExistingMUDConfigs(deviceMudWrapper.getVlan(), deviceIdentifier.getDeviceMac()
										, SeerUtil.getSwitchMacFromDpID(deviceRecord.getaSwitch().getDpId()));
								Switch aSwitch = MUDProcesserDataHolder
										.getSeerMgtService().getSwitchFromVxlanId(deviceIdentifier.getVxlanId());

								deviceMudWrapper = new DeviceMudWrapper();
								deviceMudWrapper.setVlan(vLan);
								deviceMudWrapper.setMudProfile(mudPayload);

								MUDProcesserDataHolder.getSeerMgtService().updateDevicePropertyAndSwitch(deviceMudWrapper.toString()
										, aSwitch.getId(), deviceRecord.getDevice().getId());
								addMudConfigs(mudPayload, deviceMudWrapper.getVlan(), deviceIdentifier.getDeviceMac(),
										SeerUtil.getSwitchMacFromDpID(deviceRecord.getaSwitch().getDpId()));

							}
						} else {
							//checking mud cache timeout.
							DeviceMUDFlowMap deviceMUDFlowMap = deviceFlowMapHolder.get(deviceIdentifier.getDeviceMac());
							if (deviceMUDFlowMap.getLastCheckedTimestamp() + deviceMUDFlowMap.getLongCacheTime()
									> System.currentTimeMillis()) {

								String mudPayload = getMUDContent(deviceIdentifier.getMudUrl());
								int vLan = getDeviceVLAN(deviceIdentifier.getDeviceMac());
								if (mudPayload.equals(deviceMudWrapper.getMudProfile()) && deviceMudWrapper.getVlan() == vLan &&
										deviceRecord.getaSwitch().getDpId().contains(deviceIdentifier.getVxlanId())) {
									deviceMUDFlowMap.setLastCheckedTimestamp(System.currentTimeMillis());
								} else {
									removeExistingMUDConfigs(deviceMudWrapper.getVlan(), deviceIdentifier.getDeviceMac()
											, SeerUtil.getSwitchMacFromDpID(deviceRecord.getaSwitch().getDpId()));
									Switch aSwitch = MUDProcesserDataHolder
											.getSeerMgtService().getSwitchFromVxlanId(deviceIdentifier.getVxlanId());

									deviceMudWrapper = new DeviceMudWrapper();
									deviceMudWrapper.setVlan(vLan);
									deviceMudWrapper.setMudProfile(mudPayload);

									MUDProcesserDataHolder.getSeerMgtService().updateDevicePropertyAndSwitch(deviceMudWrapper.toString()
											, aSwitch.getId(), deviceRecord.getDevice().getId());
									addMudConfigs(mudPayload, deviceMudWrapper.getVlan(), deviceIdentifier.getDeviceMac(),
											SeerUtil.getSwitchMacFromDpID(deviceRecord.getaSwitch().getDpId()));

								}
							}
						}
					}
				} catch (SeerManagementException | IOException | OFControllerException e) {
					log.error(e.getMessage(), e);
				}
				deviceIdentifier = deviceQueue.remove();
			}
		};
		deviceExecutor.scheduleWithFixedDelay(task, 10, 20, TimeUnit.SECONDS);
	}

	private String getMUDContent(String url) throws IOException {
		try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
			HttpGet httpget = new HttpGet(url);

			// Create a custom response handler
			ResponseHandler<String> responseHandler = response -> {
				int status = response.getStatusLine().getStatusCode();
				if (status >= 200 && status < 300) {
					HttpEntity entity = response.getEntity();
					return entity != null ? EntityUtils.toString(entity) : null;
				} else {
					throw new ClientProtocolException("Unexpected response status: " + status);
				}
			};
			return httpclient.execute(httpget, responseHandler);
		}
	}

	private int getDeviceVLAN(String deviceMac) {
		return 100;
	}

	private void addMudConfigs(String mudPayload, int vLan, String deviceMac, String switchMac) throws IOException,
			OFControllerException {
		DeviceMUDFlowMap deviceMUDFlowMap = processMUD(deviceMac, switchMac, mudPayload);
		List<OFFlow> ofFlows = new ArrayList<>();
		if (deviceMUDFlowMap != null) {
			ofFlows.addAll(deviceMUDFlowMap.getFromInternetStaticFlows());
			ofFlows.addAll(deviceMUDFlowMap.getToInternetStaticFlows());
			ofFlows.addAll(deviceMUDFlowMap.getFromLocalStaticFlows());
			ofFlows.addAll(deviceMUDFlowMap.getToLocalStaticFlows());
			ofFlows = sortFlowsWithPriority(ofFlows);
			MUDProcesserDataHolder.getOfController().addACLs(SeerUtil.getDpidFromMac(switchMac), deviceMac, ofFlows, vLan);
			deviceFlowMapHolder.put(deviceMac, deviceMUDFlowMap);
		}
	}

	private boolean verifyWithOrganizationPolicy(String mudPayload) {
		//TODO verify mud policy with organization policy.
		return true;
	}

	private void removeExistingMUDConfigs(int vlan, String deviceMac, String switchMac) {
		//TODO remove mudconfigs.
	}

	@Override
	public void processPacket(SeerPacket seerPacket) {
		String srcMac = seerPacket.getSrcMac();
		String destMac = seerPacket.getDstMac();

		if ((!deviceFlowMapHolder.keySet().contains(srcMac)) && PacketConstants.UDP_PROTO.equals(seerPacket.getIpProto())
				&& destMac.equals(PacketConstants.BROADCAST_ADDR) && PacketConstants.DHCP_PORT.equals(seerPacket.getDstPort())) {
			DHCP dhcp = new DHCP();
			dhcp.deserialize(seerPacket.getPayload(), 0, seerPacket.getPayload().length);
			byte opcode = dhcp.getOpCode();
			if (opcode == DHCP.OPCODE_REQUEST) {
				DHCPOption dhcpOption = dhcp.getOption(
						DHCP.DHCPOptionCode.OptionCode_MUDUrl);
				if (dhcpOption != null) {
					String mudUrl = new String(dhcpOption.getData());
					DeviceIdentifier deviceIdentifier = new DeviceIdentifier(seerPacket.getVxlanId(), srcMac);
					deviceIdentifier.setMudUrl(mudUrl);
					deviceQueue.add(deviceIdentifier);
				}
			}
		}
		if (PacketConstants.UDP_PROTO.equals(seerPacket.getIpProto())
				&& seerPacket.getSrcPort().equals(PacketConstants.DNS_PORT)) {
			try {
				DnsPacket dnsPacket = DnsPacket.newPacket(seerPacket.getPayload(), 0
						, seerPacket.getPayload().length -1);
				List<DnsResourceRecord> dnsResourceRecords = dnsPacket.getHeader().getAnswers();
				List<String> answers = new ArrayList<String>();
				for (DnsResourceRecord record : dnsResourceRecords) {
					try {
						DnsRDataA dnsRDataA = (DnsRDataA) record.getRData();
						answers.add(dnsRDataA.getAddress().getHostAddress());
					} catch (ClassCastException ex) {
						//ignore
					}
				}
				deviceFlowMapHolder.get(seerPacket.getDstMac()).addDnsIps(dnsPacket.getHeader().getQuestions().get(0)
						.getQName().getName(), answers);
			} catch (NullPointerException | IllegalRawDataException e) {
				//ignore packet that send to port 53
			}
		} else if (seerPacket.getEthType().equals(EtherType.IPV4.toString())
				|| seerPacket.getEthType().equals(EtherType.IPV4.toString())) {
			if (srcMac.replace(":", "").contains(seerPacket.getVxlanId())) {
				//G2D
				String deviceMac = seerPacket.getDstMac();
				DeviceMUDFlowMap deviceFlowMap = deviceFlowMapHolder.get(deviceMac);
				String dns = deviceFlowMap.getDns(seerPacket.getSrcIp());
				String srcIp = seerPacket.getSrcIp();
				if (dns != null) {
					seerPacket.setSrcIp(dns);
				}
				List<OFFlow> ofFlows = deviceFlowMap.getFromInternetDynamicFlows();
				OFFlow ofFlow = getMatchingFlow(seerPacket, ofFlows);
				if (ofFlow != null) {
					ofFlow = ofFlow.copy();
					ofFlow.setIdleTimeOutInSeconds(IDLE_TIMEOUT_IN_SECONDS);
					if (ofFlow.getSrcIp().equals(dns)) {
						ofFlow.setSrcIp(srcIp);
					}
					try {
						MUDProcesserDataHolder.getOfController().addFlow(SeerUtil.getDpidFromMac(srcMac), ofFlow);
					} catch (OFControllerException e) {
						log.error("Failed to add a flow to device " + deviceMac + "flow " + ofFlow.getFlowString(), e);
					}
				}
			} else if (destMac.replace(":", "").contains(seerPacket.getVxlanId())) {
				//D2G

				String deviceMac = seerPacket.getSrcMac();
				DeviceMUDFlowMap deviceMUDFlowMap = deviceFlowMapHolder.get(deviceMac);
				String dns = deviceMUDFlowMap.getDns(seerPacket.getDstIp());
				String dstIp = seerPacket.getDstIp();
				if (dns != null) {
					seerPacket.setDstIp(dns);
				}
				List<OFFlow> ofFlows = deviceMUDFlowMap.getToInternetDynamicFlows();
				OFFlow ofFlow = getMatchingFlow(seerPacket, ofFlows);
				if (ofFlow != null) {
					ofFlow = ofFlow.copy();
					ofFlow.setIdleTimeOutInSeconds(IDLE_TIMEOUT_IN_SECONDS);
					if (ofFlow.getDstIp().equals(dns)) {
						ofFlow.setDstIp(dstIp);
					}
					try {
						MUDProcesserDataHolder.getOfController().addFlow(SeerUtil.getDpidFromMac(srcMac), ofFlow);
					} catch (OFControllerException e) {
						log.error("Failed to add a flow to device " + deviceMac + "flow " + ofFlow.getFlowString(), e);
					}
				}
			}
		}
	}

	private DeviceMUDFlowMap processMUD(String deviceMac, String switchMac, String mudPayload) throws IOException {
		//TODO verify mud policy with organization policy.
		if (verifyWithOrganizationPolicy(mudPayload)) {
			ObjectMapper mapper = new ObjectMapper();
			MudSpec mudSpec = mapper.readValue(mudPayload, MudSpec.class);
			DeviceMUDFlowMap deviceMUDFlowMap = loadMudSpec(deviceMac, switchMac, mudSpec);
			installInternetNetworkRules(deviceMac, switchMac, deviceMUDFlowMap);
			installLocalNetworkRules(deviceMac, switchMac, deviceMUDFlowMap);
			return deviceMUDFlowMap;

		}
		return null;
	}

	private DeviceMUDFlowMap loadMudSpec(String deviceMac, String switchMac, MudSpec mudSpec) {
		List<String> fromDevicePolicyNames = new ArrayList<>();
		List<String> toDevicePolicyNames = new ArrayList<>();
		for (AccessDTO accessDTO : mudSpec.getIetfMud().getFromDevicePolicy().getAccessList().getAccessDTOList()) {
			fromDevicePolicyNames.add(accessDTO.getName());
		}

		for (AccessDTO accessDTO : mudSpec.getIetfMud().getToDevicePolicy().getAccessList().getAccessDTOList()) {
			toDevicePolicyNames.add(accessDTO.getName());
		}

		List<OFFlow> fromInternetDynamicFlows = new ArrayList<>();
		List<OFFlow> toInternetDynamicFlows = new ArrayList<>();
		List<OFFlow> fromInternetStaticFlows = new ArrayList<>();
		List<OFFlow> toInternetStaticFlows = new ArrayList<>();
		List<OFFlow> fromLocalStaticFlows = new ArrayList<>();
		List<OFFlow> toLocalStaticFlows = new ArrayList<>();

		for (AccessControlListHolder accessControlListHolder : mudSpec.getAccessControlList().getAccessControlListHolder()) {
			if (fromDevicePolicyNames.contains(accessControlListHolder.getName())) {
				for (Ace ace : accessControlListHolder.getAces().getAceList()) {
					Match match = ace.getMatches();

					//filter local
					if (match.getIetfMudMatch() != null && (match.getIetfMudMatch().getController() != null
							|| match.getIetfMudMatch().getLocalNetworks() != null)) {

						//install local network related rules here
						OFFlow ofFlow = new OFFlow();
						ofFlow.setSrcMac(deviceMac);
						String etherType = match.getEthMatch() == null ? PacketConstants.ETH_TYPE_IPV4 : match.getEthMatch()
								.getEtherType();
						ofFlow.setEthType(etherType);
						if (match.getIpv4Match() != null &&
								match.getIpv4Match().getProtocol() != 0) {

							ofFlow.setEthType(PacketConstants.ETH_TYPE_IPV4);
							ofFlow.setIpProto("" + match.getIpv4Match().getProtocol());
						}

						if (match.getIpv6Match() != null) {
							ofFlow.setEthType(PacketConstants.ETH_TYPE_IPV6);
							ofFlow.setIpProto("" + match.getIpv6Match().getProtocol());
						}

						if (match.getEthMatch() != null) {
							if (match.getEthMatch().getEtherType() != null) {
								ofFlow.setEthType(match.getEthMatch().getEtherType());
							}
							if (match.getEthMatch().getSrcMacAddress() != null) {
								ofFlow.setSrcMac(match.getEthMatch().getSrcMacAddress());
							}
							if (match.getEthMatch().getDstMacAddress() != null) {
								ofFlow.setDstMac(match.getEthMatch().getDstMacAddress());
							}

						}
						//tcp
						if (match.getTcpMatch() != null &&
								match.getTcpMatch().getDestinationPortMatch() != null
								&& match.getTcpMatch().getDestinationPortMatch().getPort() != 0) {
							ofFlow.setDstPort("" + match.getTcpMatch().getDestinationPortMatch().getPort());
						}

						if (match.getTcpMatch() != null && match.getTcpMatch().getSourcePortMatch() != null
								&& match.getTcpMatch().getSourcePortMatch().getPort() != 0) {
							ofFlow.setSrcPort("" + match.getTcpMatch().getSourcePortMatch().getPort());
						}
						//udp
						if (match.getUdpMatch() != null && match.getUdpMatch().getDestinationPortMatch() != null
								&& match.getUdpMatch().getDestinationPortMatch().getPort() != 0) {
							ofFlow.setDstPort("" + match.getUdpMatch().getDestinationPortMatch().getPort());
						}

						if (match.getUdpMatch() != null && match.getUdpMatch().getSourcePortMatch() != null
								&& match.getUdpMatch().getSourcePortMatch().getPort() != 0) {
							ofFlow.setSrcPort("" + match.getUdpMatch().getSourcePortMatch().getPort());
						}

						if ((match.getIpv4Match() != null && match.getIpv4Match().getDestinationIp() != null)) {
							ofFlow.setDstIp(match.getIpv4Match().getDestinationIp());
						} else if (match.getIpv6Match() != null && match.getIpv6Match().getDestinationIp() != null) {
							ofFlow.setDstIp(match.getIpv6Match().getDestinationIp());
						} else if (match.getIetfMudMatch().getController() != null &&
								(match.getIetfMudMatch().getController().contains(MUD_URN))) {
							ofFlow.setDstIp(MUDProcesserDataHolder.getMUDControllerValue(match.getIetfMudMatch().getController()));
						}
						ofFlow.setPriority(FIXED_LOCAL_COMMUNICATION);
						ofFlow.setOfAction(OFFlow.OFAction.NORMAL);
						toLocalStaticFlows.add(ofFlow);

					} else {
						OFFlow ofFlow = new OFFlow();
						ofFlow.setSrcMac(deviceMac);
						ofFlow.setDstMac(switchMac);

						String etherType = match.getEthMatch() == null ? PacketConstants.ETH_TYPE_IPV4 : match.getEthMatch()
								.getEtherType();
						ofFlow.setEthType(etherType);
						if (match.getIpv4Match() != null &&
								match.getIpv4Match().getProtocol() != 0) {
							ofFlow.setEthType(PacketConstants.ETH_TYPE_IPV4);
							ofFlow.setIpProto("" + match.getIpv4Match().getProtocol());
						}

						if (match.getIpv6Match() != null) {
							ofFlow.setEthType(PacketConstants.ETH_TYPE_IPV6);
							ofFlow.setIpProto("" + match.getIpv6Match().getProtocol());
						}

						//tcp
						if (match.getTcpMatch() != null && match.getTcpMatch().getDestinationPortMatch() != null
								&& match.getTcpMatch().getDestinationPortMatch().getPort() != 0) {
							ofFlow.setDstPort("" + match.getTcpMatch().getDestinationPortMatch().getPort());
						}

						if (match.getTcpMatch() != null && match.getTcpMatch().getSourcePortMatch() != null
								&& match.getTcpMatch().getSourcePortMatch().getPort() != 0) {
							ofFlow.setSrcPort("" + match.getTcpMatch().getSourcePortMatch().getPort());
						}
						//udp
						if (match.getUdpMatch() != null && match.getUdpMatch().getDestinationPortMatch() != null
								&& match.getUdpMatch().getDestinationPortMatch().getPort() != 0) {
							ofFlow.setDstPort("" + match.getUdpMatch().getDestinationPortMatch().getPort());

						}

						if (match.getUdpMatch() != null && match.getUdpMatch().getSourcePortMatch() != null
								&& match.getUdpMatch().getSourcePortMatch().getPort() != 0) {
							ofFlow.setSrcPort("" + match.getUdpMatch().getSourcePortMatch().getPort());
						}

						if (match.getIpv4Match() != null && match.getIpv4Match().getDestinationIp() != null) {
							ofFlow.setDstIp(match.getIpv4Match().getDestinationIp());
							ofFlow.setPriority(FIXED_INTERNET_COMMUNICATION);
						} else if (match.getIpv4Match() != null && match.getIpv4Match().getDstDnsName() != null) {
							ofFlow.setDstIp(match.getIpv4Match().getDstDnsName());
							ofFlow.setPriority(DYNAMIC_INTERNET_COMMUNICATION);
						} else if (match.getIpv6Match() != null &&
								match.getIpv6Match().getDestinationIp() != null) {
							ofFlow.setDstIp(match.getIpv6Match().getDestinationIp());
							ofFlow.setPriority(FIXED_INTERNET_COMMUNICATION);
						} else if (match.getIpv6Match() != null &&
								match.getIpv6Match().getDstDnsName() != null) {
							ofFlow.setDstIp(match.getIpv6Match().getDstDnsName());
							ofFlow.setPriority(DYNAMIC_INTERNET_COMMUNICATION);
						} else {
							ofFlow.setPriority(FIXED_INTERNET_COMMUNICATION);
						}
						ofFlow.setOfAction(OFFlow.OFAction.NORMAL);
						if (FIXED_INTERNET_COMMUNICATION == ofFlow.getPriority()) {
							toInternetStaticFlows.add(ofFlow);
						} else {
							toInternetDynamicFlows.add(ofFlow);
						}

					}
				}
			} else if (toDevicePolicyNames.contains(accessControlListHolder.getName())) {

				for (Ace ace : accessControlListHolder.getAces().getAceList()) {
					Match match = ace.getMatches();

					//filter local
					if (match.getIetfMudMatch() != null && (match.getIetfMudMatch().getController() != null
							|| match.getIetfMudMatch().getLocalNetworks() != null)) {
						//install local network related rules here
						OFFlow ofFlow = new OFFlow();
						ofFlow.setDstMac(deviceMac);
						if (match.getIpv4Match() != null &&
								match.getIpv4Match().getProtocol() != 0) {
							ofFlow.setIpProto("" + match.getIpv4Match().getProtocol());
							ofFlow.setEthType(PacketConstants.ETH_TYPE_IPV4);
						}

						if (match.getIpv6Match() != null) {
							ofFlow.setEthType(PacketConstants.ETH_TYPE_IPV6);
							ofFlow.setIpProto("" + match.getIpv6Match().getProtocol());
						}

						//tcp
						if (match.getTcpMatch() != null &&
								match.getTcpMatch().getDestinationPortMatch() != null
								&& match.getTcpMatch().getDestinationPortMatch().getPort() != 0) {
							ofFlow.setDstPort("" + match.getTcpMatch().getDestinationPortMatch().getPort());
						}

						if (match.getTcpMatch() != null &&
								match.getTcpMatch().getSourcePortMatch() != null
								&& match.getTcpMatch().getSourcePortMatch().getPort() != 0) {
							ofFlow.setSrcPort("" + match.getTcpMatch().getSourcePortMatch().getPort());
						}
						//udp
						if (match.getUdpMatch() != null &&
								match.getUdpMatch().getDestinationPortMatch() != null
								&& match.getUdpMatch().getDestinationPortMatch().getPort() != 0) {
							ofFlow.setDstPort("" + match.getUdpMatch().getDestinationPortMatch().getPort());
						}

						if (match.getUdpMatch() != null &&
								match.getUdpMatch().getSourcePortMatch() != null
								&& match.getUdpMatch().getSourcePortMatch().getPort() != 0) {
							ofFlow.setSrcPort("" + match.getUdpMatch().getSourcePortMatch().getPort());
						}

						if ((match.getIpv4Match() != null && match.getIpv4Match().getDestinationIp() != null)) {
							ofFlow.setSrcIp(match.getIpv4Match().getDestinationIp());
						} else if (match.getIpv6Match() != null && match.getIpv6Match().getDestinationIp() != null) {
							ofFlow.setSrcIp(match.getIpv6Match().getDestinationIp());
						} else if (match.getIetfMudMatch().getController() != null &&
								(match.getIetfMudMatch().getController().contains(MUD_URN))) {
							ofFlow.setSrcIp(MUDProcesserDataHolder.getMUDControllerValue(match.getIetfMudMatch().getController()));
						}

						ofFlow.setPriority(FIXED_LOCAL_COMMUNICATION);
						ofFlow.setOfAction(OFFlow.OFAction.NORMAL);
						fromLocalStaticFlows.add(ofFlow);
					} else {
						OFFlow ofFlow = new OFFlow();
						ofFlow.setSrcMac(switchMac);
						ofFlow.setDstMac(deviceMac);
						String etherType = match.getEthMatch() == null ? PacketConstants.ETH_TYPE_IPV4 : match.getEthMatch()
								.getEtherType();
						ofFlow.setEthType(etherType);
						ofFlow.setOfAction(OFFlow.OFAction.NORMAL);
						if (match.getIpv4Match() != null &&
								match.getIpv4Match().getProtocol() != 0) {

							ofFlow.setEthType(PacketConstants.ETH_TYPE_IPV4);
							ofFlow.setIpProto("" + match.getIpv4Match().getProtocol());
						}

						if (match.getIpv6Match() != null) {
							ofFlow.setEthType(PacketConstants.ETH_TYPE_IPV6);
							ofFlow.setIpProto("" + match.getIpv6Match().getProtocol());
						}

						//tcp
						if (match.getTcpMatch() != null &&
								match.getTcpMatch().getDestinationPortMatch() != null
								&& match.getTcpMatch().getDestinationPortMatch().getPort() != 0) {
							ofFlow.setDstPort("" + match.getTcpMatch().getDestinationPortMatch().getPort());
						}

						if (match.getTcpMatch() != null &&
								match.getTcpMatch().getSourcePortMatch() != null
								&& match.getTcpMatch().getSourcePortMatch().getPort() != 0) {
							ofFlow.setSrcPort("" + match.getTcpMatch().getSourcePortMatch().getPort());
						}
						//udp
						if (match.getUdpMatch() != null &&
								match.getUdpMatch().getDestinationPortMatch() != null
								&& match.getUdpMatch().getDestinationPortMatch().getPort() != 0) {
							ofFlow.setDstPort("" + match.getUdpMatch().getDestinationPortMatch().getPort());
						}

						if (match.getUdpMatch() != null &&
								match.getUdpMatch().getSourcePortMatch() != null
								&& match.getUdpMatch().getSourcePortMatch().getPort() != 0) {
							ofFlow.setSrcPort("" + match.getUdpMatch().getSourcePortMatch().getPort());
							if (ofFlow.getSrcPort().equals(PacketConstants.DNS_PORT)) {
								ofFlow.setOfAction(OFFlow.OFAction.MIRROR_TO_VXLAN);
							}
						}

						if (match.getIpv4Match() != null && match.getIpv4Match().getSourceIp() != null) {
							ofFlow.setSrcIp(match.getIpv4Match().getSourceIp());
							ofFlow.setPriority(FIXED_INTERNET_COMMUNICATION);
						} else if (match.getIpv4Match() != null && match.getIpv4Match().getSrcDnsName() != null) {
							ofFlow.setSrcIp(match.getIpv4Match().getSrcDnsName());
							ofFlow.setPriority(DYNAMIC_INTERNET_COMMUNICATION);
						} else if (match.getIpv6Match() != null && match.getIpv6Match().getSourceIp() != null) {
							ofFlow.setSrcIp(match.getIpv6Match().getSourceIp());
							ofFlow.setPriority(FIXED_INTERNET_COMMUNICATION);
						} else if (match.getIpv6Match() != null && match.getIpv6Match().getSrcDnsName() != null) {
							ofFlow.setSrcIp(match.getIpv6Match().getSrcDnsName());
							ofFlow.setPriority(DYNAMIC_INTERNET_COMMUNICATION);
						} else {
							ofFlow.setPriority(FIXED_INTERNET_COMMUNICATION);
						}


						if (DYNAMIC_INTERNET_COMMUNICATION == ofFlow.getPriority()) {
							fromInternetDynamicFlows.add(ofFlow);
						} else {
							fromInternetStaticFlows.add(ofFlow);
						}
					}
				}
			}
		}

		DeviceMUDFlowMap deviceFlowMap = new DeviceMUDFlowMap();
		deviceFlowMap.setFromInternetDynamicFlows(fromInternetDynamicFlows);
		deviceFlowMap.setFromInternetStaticFlows(fromInternetStaticFlows);
		deviceFlowMap.setToInternetDynamicFlows(toInternetDynamicFlows);
		deviceFlowMap.setToInternetStaticFlows(toInternetStaticFlows);
		deviceFlowMap.setToLocalStaticFlows(toLocalStaticFlows);
		deviceFlowMap.setFromLocalStaticFlows(fromLocalStaticFlows);
		return deviceFlowMap;

	}

	public class DeviceIdentifier {

		private String vxlanId;
		private String deviceMac;
		private String mudUrl;

		public DeviceIdentifier(String vlanId, String deviceMac) {
			this.vxlanId = vlanId;
			this.deviceMac = deviceMac;
		}

		public String getMudUrl() {
			return mudUrl;
		}

		public void setMudUrl(String mudUrl) {
			this.mudUrl = mudUrl;
		}

		public String getVxlanId() {
			return vxlanId;
		}

		public void setVxlanId(String vxlanId) {
			this.vxlanId = vxlanId;
		}

		public String getDeviceMac() {
			return deviceMac;
		}

		public void setDeviceMac(String deviceMac) {
			this.deviceMac = deviceMac;
		}

		@Override
		public int hashCode() {
			int result = this.vxlanId.hashCode();
			result = 31 * result + ("@" + this.deviceMac).hashCode();
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			return (obj instanceof DeviceIdentifier) && vxlanId.equals(
					((DeviceIdentifier) obj).vxlanId) && deviceMac.equals(
					((DeviceIdentifier) obj).deviceMac);
		}
	}

	private void installLocalNetworkRules(String deviceMac, String switchMac, DeviceMUDFlowMap deviceMUDFlowMap) {
		OFFlow ofFlow = new OFFlow();
		ofFlow.setSrcMac(deviceMac);
		ofFlow.setEthType(PacketConstants.ETH_TYPE_ARP);
		ofFlow.setPriority(FIXED_LOCAL_COMMUNICATION);
		ofFlow.setOfAction(OFFlow.OFAction.NORMAL);
		deviceMUDFlowMap.getToLocalStaticFlows().add(ofFlow);

		ofFlow = new OFFlow();
		ofFlow.setDstMac(deviceMac);
		ofFlow.setEthType(PacketConstants.ETH_TYPE_ARP);
		ofFlow.setPriority(FIXED_LOCAL_COMMUNICATION);
		ofFlow.setOfAction(OFFlow.OFAction.NORMAL);
		deviceMUDFlowMap.getFromLocalStaticFlows().add(ofFlow);

		ofFlow = new OFFlow();
		ofFlow.setDstMac(deviceMac);
		ofFlow.setEthType(PacketConstants.ETH_TYPE_IPV4);
		ofFlow.setIpProto(PacketConstants.ICMP_PROTO);
		ofFlow.setPriority(DEFAULT_LOCAL_COMMUNICATION);
		ofFlow.setOfAction(OFFlow.OFAction.MIRROR_TO_VXLAN);
		deviceMUDFlowMap.getFromLocalStaticFlows().add(ofFlow);

		ofFlow = new OFFlow();
		ofFlow.setDstMac(deviceMac);
		ofFlow.setEthType(PacketConstants.ETH_TYPE_IPV4);
		ofFlow.setIpProto(PacketConstants.TCP_PROTO);
		ofFlow.setPriority(DEFAULT_LOCAL_COMMUNICATION);
		ofFlow.setOfAction(OFFlow.OFAction.MIRROR_TO_VXLAN);
		deviceMUDFlowMap.getFromLocalStaticFlows().add(ofFlow);

		ofFlow = new OFFlow();
		ofFlow.setDstMac(deviceMac);
		ofFlow.setEthType(PacketConstants.ETH_TYPE_IPV4);
		ofFlow.setIpProto(PacketConstants.UDP_PROTO);
		ofFlow.setPriority(DEFAULT_LOCAL_COMMUNICATION);
		ofFlow.setOfAction(OFFlow.OFAction.MIRROR_TO_VXLAN);
		deviceMUDFlowMap.getFromLocalStaticFlows().add(ofFlow);
	}

	private void installInternetNetworkRules(String deviceMac, String switchMac, DeviceMUDFlowMap deviceMUDFlowMap) {

		OFFlow ofFlow = new OFFlow();
		ofFlow.setSrcMac(switchMac);
		ofFlow.setDstMac(deviceMac);
		ofFlow.setEthType(PacketConstants.ETH_TYPE_IPV4);
		ofFlow.setIpProto(PacketConstants.TCP_PROTO);
		ofFlow.setPriority(DEFAULT_INTERNET_COMMUNICATION);
		ofFlow.setOfAction(OFFlow.OFAction.MIRROR_TO_VXLAN);
		deviceMUDFlowMap.getFromInternetStaticFlows().add(ofFlow);

		ofFlow = new OFFlow();
		ofFlow.setSrcMac(switchMac);
		ofFlow.setDstMac(deviceMac);
		ofFlow.setEthType(PacketConstants.ETH_TYPE_IPV4);
		ofFlow.setIpProto(PacketConstants.UDP_PROTO);
		ofFlow.setPriority(DEFAULT_INTERNET_COMMUNICATION);
		ofFlow.setOfAction(OFFlow.OFAction.MIRROR_TO_VXLAN);
		deviceMUDFlowMap.getFromInternetStaticFlows().add(ofFlow);

		ofFlow = new OFFlow();
		ofFlow.setSrcMac(deviceMac);
		ofFlow.setDstMac(switchMac);
		ofFlow.setEthType(PacketConstants.ETH_TYPE_IPV4);
		ofFlow.setIpProto(PacketConstants.ICMP_PROTO);
		ofFlow.setPriority(DEFAULT_INTERNET_COMMUNICATION);
		ofFlow.setOfAction(OFFlow.OFAction.MIRROR_TO_VXLAN);
		deviceMUDFlowMap.getToInternetStaticFlows().add(ofFlow);

		ofFlow = new OFFlow();
		ofFlow.setSrcMac(deviceMac);
		ofFlow.setDstMac(switchMac);
		ofFlow.setEthType(PacketConstants.ETH_TYPE_IPV4);
		ofFlow.setIpProto(PacketConstants.UDP_PROTO);
		ofFlow.setPriority(DEFAULT_INTERNET_COMMUNICATION);
		ofFlow.setOfAction(OFFlow.OFAction.MIRROR_TO_VXLAN);
		deviceMUDFlowMap.getToInternetStaticFlows().add(ofFlow);

		ofFlow = new OFFlow();
		ofFlow.setSrcMac(deviceMac);
		ofFlow.setDstMac(switchMac);
		ofFlow.setEthType(PacketConstants.ETH_TYPE_IPV4);
		ofFlow.setIpProto(PacketConstants.TCP_PROTO);
		ofFlow.setPriority(DEFAULT_INTERNET_COMMUNICATION);
		ofFlow.setOfAction(OFFlow.OFAction.MIRROR_TO_VXLAN);
		deviceMUDFlowMap.getToInternetStaticFlows().add(ofFlow);

		ofFlow = new OFFlow();
		ofFlow.setSrcMac(switchMac);
		ofFlow.setDstMac(deviceMac);
		ofFlow.setEthType(PacketConstants.ETH_TYPE_IPV4);
		ofFlow.setIpProto(PacketConstants.ICMP_PROTO);
		ofFlow.setPriority(DEFAULT_INTERNET_COMMUNICATION);
		ofFlow.setOfAction(OFFlow.OFAction.NORMAL);
		deviceMUDFlowMap.getFromInternetStaticFlows().add(ofFlow);

	}

	private OFFlow getMatchingFlow(SeerPacket packet, List<OFFlow> ofFlows) {
		for (int i = 0; i < ofFlows.size(); i++) {
			OFFlow flow = ofFlows.get(i);
			String srcMac = packet.getSrcMac();
			String dstMac = packet.getDstMac();
			String ethType = packet.getEthType();
			String vlanId = "*";
			String srcIp = packet.getSrcIp() == null ? "*" : packet.getSrcIp();
			String dstIp = packet.getDstIp() == null ? "*" : packet.getDstIp();
			String ipProto = packet.getIpProto() == null ? "*" : packet.getIpProto();
			String srcPort = packet.getSrcPort() == null ? "*" : packet.getSrcPort();
			String dstPort = packet.getDstPort() == null ? "*" : packet.getDstPort();

			boolean condition = (srcMac.equals(flow.getSrcMac()) || flow.getSrcMac().equals("*")) &&
					(dstMac.equals(flow.getDstMac()) || flow.getDstMac().equals("*")) &&
					(ethType.equals(flow.getEthType()) || flow.getEthType().equals("*")) &&
					(vlanId.equals(flow.getVlanId()) || flow.getVlanId().equals("*")) &&
					(srcIp.equals(flow.getSrcIp()) || flow.getSrcIp().equals("*")) &&
					(dstIp.equals(flow.getDstIp()) || flow.getDstIp().equals("*")) &&
					(ipProto.equals(flow.getIpProto()) || flow.getIpProto().equals("*")) &&
					(srcPort.equals(flow.getSrcPort()) || flow.getSrcPort().equals("*")) &&
					(dstPort.equals(flow.getDstPort()) || flow.getDstPort().equals("*"));

			if (condition) {
				return flow;
			}
		}
		return null;
	}

	private List<OFFlow> sortFlowsWithPriority(List<OFFlow> flows){

		LinkedList<OFFlow> ofFlows = new LinkedList<OFFlow>();

		for (OFFlow flow: flows) {
			boolean exist = false;
			for (int i = 0 ; i < ofFlows.size(); i++) {
				OFFlow currentFlow = ofFlows.get(i);
				if (currentFlow.equals(flow)) {
					exist = true;
				}
			}

			if (!exist) {
				if (ofFlows.size() == 0) {
					ofFlows.add(flow);
					continue;
				}
				for (int i = 0 ; i < ofFlows.size(); i++) {
					OFFlow currentFlow = ofFlows.get(i);

					if (flow.getPriority() >= currentFlow.getPriority()) {
						if (i == 0) {
							ofFlows.addFirst(flow);
							break;
						} else {
							ofFlows.add(i, flow);
							break;
						}
					} else if(i == ofFlows.size()-1) {
						ofFlows.addLast(flow);
						break;
					}
				}

			}
		}
		return ofFlows;
	}
}
