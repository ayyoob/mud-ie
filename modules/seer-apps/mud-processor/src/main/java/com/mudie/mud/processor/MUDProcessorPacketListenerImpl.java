package com.mudie.mud.processor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mudie.common.DeviceMudWrapper;
import com.mudie.common.SeerDirectory;
import com.mudie.common.SeerUtil;
import com.mudie.common.mud.*;
import com.mudie.common.packet.PacketConstants;
import com.mudie.mud.processor.dhcp.DHCP;
import com.mudie.mud.processor.dhcp.DHCPOption;
import com.mudie.common.openflow.OFFlow;
import com.mudie.sdn.controller.mgt.exception.OFControllerException;
import com.mudie.seer.mgt.dto.Device.Status;

import com.mudie.common.packet.PacketListener;
import com.mudie.common.packet.SeerPacket;
import com.mudie.mud.processor.internal.MUDProcesserDataHolder;
import com.mudie.seer.mgt.dto.Device;
import com.mudie.seer.mgt.dto.DeviceRecord;
import com.mudie.seer.mgt.dto.Switch;
import com.mudie.seer.mgt.exception.SeerManagementException;
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
import java.io.FileWriter;
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
	private static final int FIXED_LOCAL_CONTROLLER_COMMUNICATION = 11;
	private static final int DEFAULT_INTERNET_COMMUNICATION = 9;
	private static final int DYNAMIC_INTERNET_COMMUNICATION = 15000;
	private static final String MUD_URN = "urn:ietf:params:mud";
	private static boolean isExistingDevicesloaded = false;

	public MUDProcessorPacketListenerImpl() {
		ScheduledExecutorService deviceExecutor = Executors.newScheduledThreadPool(1);

		Runnable task = () -> {
			DeviceIdentifier deviceIdentifier = null;
			if (deviceQueue.size() > 0) {
				deviceIdentifier = deviceQueue.remove();
			}

			while (deviceIdentifier != null) {
				try {
					DeviceRecord deviceRecord = MUDProcesserDataHolder.getSeerMgtService()
							.getDeviceRecord(deviceIdentifier.getVxlanId(), deviceIdentifier.getDeviceMac());
					if (deviceRecord == null || deviceRecord.getDevice() == null
							|| deviceRecord.getDevice().getStatus() != Status.IDENTIFIED) {
						String mudPayload = getMUDContent(deviceIdentifier.getMudUrl());
						Switch aSwitch = MUDProcesserDataHolder
								.getSeerMgtService().getSwitchFromVxlanId(deviceIdentifier.getVxlanId());
						int vLan = getDeviceVLAN(deviceIdentifier.getDeviceMac());
						Device device = new Device();
						device.setMac(deviceIdentifier.getDeviceMac());
						device.setSwitchId(aSwitch.getId());
						device.setStatus(Status.IDENTIFIED);
						device.setName(deviceIdentifier.getName());
						DeviceMudWrapper deviceMudWrapper = new DeviceMudWrapper();
						deviceMudWrapper.setVlan(vLan);
						deviceMudWrapper.setMudProfile(mudPayload);
						device.setProperty(deviceMudWrapper.toString());
						addMudConfigs(mudPayload, deviceMudWrapper.getVlan(), deviceIdentifier.getDeviceMac(),
								SeerUtil.getSwitchMacFromDpID(aSwitch.getDpId()));
						MUDProcesserDataHolder.getSeerMgtService().addDevice(device);
						log.info("mud config is added for device " + deviceIdentifier.getDeviceMac());


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
								log.info("mud config is loaded for device " + deviceIdentifier.getDeviceMac());
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
								log.info("mud config is reloaded for device " + deviceIdentifier.getDeviceMac());

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
									log.info("mud config is reloaded for device " + deviceIdentifier.getDeviceMac());
								}
							}
						}
					}
				} catch (Exception e) {
					log.error(e.getMessage(), e);
				}
				deviceIdentifier = null;
				if (deviceQueue.size() > 0) {
					deviceIdentifier = deviceQueue.remove();
				}
			}

			if (!isExistingDevicesloaded) {
				try {
					List<Switch> switches = MUDProcesserDataHolder.getSeerMgtService().getSwitches();
					if (switches != null) {
						for (Switch aswitch : switches) {
							List<Device> devices = MUDProcesserDataHolder.getSeerMgtService().getDevices(aswitch.getDpId());
							for (Device device : devices) {
								DeviceMudWrapper deviceMudWrapper = new  DeviceMudWrapper(device.getProperty());
								DeviceMUDFlowMap deviceMUDFlowMap = processMUD(device.getMac(),
										SeerUtil.getSwitchMacFromDpID(aswitch.getDpId()),
										deviceMudWrapper.getMudProfile());
								deviceFlowMapHolder.put(device.getMac(), deviceMUDFlowMap);
								log.info("mud config is loaded for device " + device.getMac());
							}
						}
						isExistingDevicesloaded = true;
					}
				} catch (SeerManagementException e) {
					log.error("Failed to load data from db", e);
				} catch (IOException e) {
					log.error("Failed to process mud", e);
				}
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

	private int getDeviceVLAN(String deviceMac) throws OFControllerException {
		if (MUDProcesserDataHolder.getOfController().getHostInfo(deviceMac) != null) {
			return MUDProcesserDataHolder.getOfController().getHostInfo(deviceMac).getVlanId();
		} else {
			try {
				//wait till host data is updated.
				Thread.sleep(10000);
			} catch (InterruptedException e) {
				//ignore
			}
			if (MUDProcesserDataHolder.getOfController().getHostInfo(deviceMac) != null) {
				return MUDProcesserDataHolder.getOfController().getHostInfo(deviceMac).getVlanId();
			}
		}
		throw new OFControllerException("vlan not found for " + deviceMac);
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

		if (PacketConstants.UDP_PROTO.equals(seerPacket.getIpProto())
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
					dhcpOption = dhcp.getOption(
							DHCP.DHCPOptionCode.OptionCode_Hostname);
					if (dhcpOption != null) {
						deviceIdentifier.setName(new String(dhcpOption.getData()));
					} else {
						deviceIdentifier.setName("");
					}
					deviceQueue.add(deviceIdentifier);
				} else if(MUDProcesserDataHolder.getMudDevices().containsKey(srcMac)) {
					DeviceIdentifier deviceIdentifier = new DeviceIdentifier(seerPacket.getVxlanId(), srcMac);
					deviceIdentifier.setMudUrl(MUDProcesserDataHolder.getMudDevices().get(srcMac));
					dhcpOption = dhcp.getOption(
							DHCP.DHCPOptionCode.OptionCode_Hostname);
					if (dhcpOption != null) {
						deviceIdentifier.setName(new String(dhcpOption.getData()));
					} else {
						deviceIdentifier.setName("");
					}
					deviceQueue.add(deviceIdentifier);
				}
			}
		} else if (deviceFlowMapHolder.keySet().contains(srcMac) || deviceFlowMapHolder.keySet().contains(destMac)) {
			logPacket(seerPacket);
			if (PacketConstants.UDP_PROTO.equals(seerPacket.getIpProto())
					&& seerPacket.getSrcPort().equals(PacketConstants.DNS_PORT)) {
				try {
					DnsPacket dnsPacket = DnsPacket.newPacket(seerPacket.getPayload(), 0
							, seerPacket.getPayload().length);
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
			} else if (seerPacket.getEthType().equals(EtherType.IPV4.valueAsString())
					|| seerPacket.getEthType().equals(EtherType.IPV6.valueAsString())) {
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
						ofFlow.setIdleTimeOutInSeconds(MUDProcesserDataHolder.getMudConfig().getMudReactiveIdleTimeout());
						if (ofFlow.getSrcIp().equals(dns) || dns.endsWith(ofFlow.getSrcIp())) {
							ofFlow.setSrcIp(srcIp);
						}
						try {
							MUDProcesserDataHolder.getOfController().addFlow(SeerUtil.getDpidFromMac(srcMac), ofFlow);
						} catch (OFControllerException e) {
							log.error("Failed to add a flow to device " + deviceMac + "flow " + ofFlow.getFlowString(), e);
						}
					} else {
						logAnomalyPacket(seerPacket);
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
						ofFlow.setIdleTimeOutInSeconds(MUDProcesserDataHolder.getMudConfig().getMudReactiveIdleTimeout());
						if (ofFlow.getDstIp().equals(dns) || dns.endsWith(ofFlow.getDstIp())) {
							ofFlow.setDstIp(dstIp);
						}
						try {
							MUDProcesserDataHolder.getOfController().addFlow(SeerUtil.getDpidFromMac(destMac), ofFlow);
						} catch (OFControllerException e) {
							log.error("Failed to add a flow to device " + deviceMac + "flow " + ofFlow.getFlowString(), e);
						}
					} else {
						logAnomalyPacket(seerPacket);
					}
				} else {
					logAnomalyPacket(seerPacket);
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
						ofFlow.setPriority(FIXED_LOCAL_COMMUNICATION);
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
							ofFlow.setPriority(FIXED_LOCAL_CONTROLLER_COMMUNICATION);
						}

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
						ofFlow.setPriority(FIXED_LOCAL_COMMUNICATION);
						ofFlow.setOfAction(OFFlow.OFAction.NORMAL);

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
							if (ofFlow.getSrcPort().equals(PacketConstants.DNS_PORT)) {
								ofFlow.setOfAction(OFFlow.OFAction.MIRROR_TO_VXLAN);
							}
						}


						if ((match.getIpv4Match() != null && match.getIpv4Match().getSourceIp() != null)) {
							ofFlow.setSrcIp(match.getIpv4Match().getSourceIp());
						} else if (match.getIpv6Match() != null && match.getIpv6Match().getSourceIp() != null) {
							ofFlow.setSrcIp(match.getIpv6Match().getSourceIp());
						} else if (match.getIetfMudMatch().getController() != null &&
								(match.getIetfMudMatch().getController().contains(MUD_URN))) {
							ofFlow.setSrcIp(MUDProcesserDataHolder.getMUDControllerValue(match.getIetfMudMatch().getController()));
							ofFlow.setPriority(FIXED_LOCAL_CONTROLLER_COMMUNICATION);
						}
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
					(srcIp.endsWith(flow.getSrcIp()) ||srcIp.equals(flow.getSrcIp()) || flow.getSrcIp().equals("*")) &&
					(dstIp.endsWith(flow.getDstIp()) || dstIp.equals(flow.getDstIp()) || flow.getDstIp().equals("*")) &&
					(ipProto.equals(flow.getIpProto()) || flow.getIpProto().equals("*")) &&
					(srcPort.equals(flow.getSrcPort()) || flow.getSrcPort().equals("*")) &&
					(dstPort.equals(flow.getDstPort()) || flow.getDstPort().equals("*"));

			if (condition) {
				return flow;
			}
		}
		return null;
	}

	private List<OFFlow> sortFlowsWithPriority(List<OFFlow> flows) {

		LinkedList<OFFlow> ofFlows = new LinkedList<OFFlow>();

		for (OFFlow flow : flows) {
			boolean exist = false;
			for (int i = 0; i < ofFlows.size(); i++) {
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
				for (int i = 0; i < ofFlows.size(); i++) {
					OFFlow currentFlow = ofFlows.get(i);

					if (flow.getPriority() >= currentFlow.getPriority()) {
						if (i == 0) {
							ofFlows.addFirst(flow);
							break;
						} else {
							ofFlows.add(i, flow);
							break;
						}
					} else if (i == ofFlows.size() - 1) {
						ofFlows.addLast(flow);
						break;
					}
				}

			}
		}
		return ofFlows;
	}

	private void logPacket(SeerPacket packet) {
		if (!MUDProcesserDataHolder.getMudConfig().isMudPacketLogging()) {
			return;
		}
		if (deviceFlowMapHolder.keySet().contains(packet.getSrcMac())) {
			String path = SeerDirectory.getLogDirectory() + File.separator + packet.getSrcMac().replace(":","")  + "-packet.log";
			logPacket(path, packet);
		}

		if (deviceFlowMapHolder.keySet().contains(packet.getDstMac())) {
			String path = SeerDirectory.getLogDirectory() + File.separator + packet.getDstMac().replace(":","")  + "-packet.log";
			logPacket(path, packet);
		}
	}


	private void logAnomalyPacket(SeerPacket packet) {
		if (!MUDProcesserDataHolder.getMudConfig().isMudPacketLogging()) {
			return;
		}
		if (deviceFlowMapHolder.keySet().contains(packet.getSrcMac())) {
			String path = SeerDirectory.getLogDirectory() + File.separator + packet.getSrcMac().replace(":","") + "-packet-anomaly.log";
			logPacket(path, packet);
		}

		if (deviceFlowMapHolder.keySet().contains(packet.getDstMac())) {
			String path = SeerDirectory.getLogDirectory() + File.separator + packet.getDstMac().replace(":","")  + "-packet-anomaly.log";
			logPacket(path, packet);
		}
	}

	private void logPacket(String path, SeerPacket packet) {
		File file = new File(path);
		// need to check how often does this method is called per device.
		FileWriter writer = null;
		try {
			writer = new FileWriter(file, true);
			writer.write(packet.getPacketInfo() + "\n");
			writer.flush();
			writer.close();
		} catch (IOException e) {
			log.error("Failed to log packet " + packet.getPacketInfo(), e);
		}
	}
}
