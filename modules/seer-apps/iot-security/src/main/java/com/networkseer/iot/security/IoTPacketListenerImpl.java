package com.networkseer.iot.security;

import com.networkseer.common.openflow.OFFlow;
import com.networkseer.common.packet.PacketConstants;
import com.networkseer.common.packet.PacketListener;
import com.networkseer.common.packet.SeerPacket;
import com.networkseer.iot.security.dto.DeviceIdentifier;
import com.networkseer.iot.security.dto.SeerPacketWrapper;
import com.networkseer.iot.security.internal.IoTSecurityDataHolder;
import com.networkseer.sdn.controller.mgt.exception.OFControllerException;
import com.networkseer.seer.mgt.exception.SeerManagementException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class IoTPacketListenerImpl implements PacketListener {
	private static final Logger log = LoggerFactory.getLogger(IoTPacketListenerImpl.class);
	private static LinkedList<SeerPacketWrapper> packetQueue = new LinkedList<>();
	private static Map<String, String> vlanMacMapping = new HashMap<>();

	public IoTPacketListenerImpl() {


		ScheduledExecutorService iotTaskExecutor = Executors.newScheduledThreadPool(1);

		Runnable task = () -> {
			SeerPacketWrapper seerPacketWrapper = packetQueue.remove();
			while(seerPacketWrapper != null) {
				try {
					List<OFFlow> ofFlowList = IoTSecurityDataHolder.getStatsCache()
							.get(new DeviceIdentifier(seerPacketWrapper.getVlanId(),
							seerPacketWrapper.getDeviceMac(), true));

					if (ofFlowList != null) {
						SeerPacket packet = seerPacketWrapper.getSeerPacket();
						String srcMac = packet.getSrcMac();
						String destMac = packet.getDstMac();
						String vlanId = seerPacketWrapper.getVlanId();
						String dstIp = packet.getDstIp();
						String srcIp = packet.getSrcIp();
						String protocol = packet.getIpProto();
						String srcPort = packet.getSrcPort();
						String dstPort = packet.getDstPort();

						//Only UDP and TCP proto
						if (protocol != null && (protocol.equals(PacketConstants.TCP_PROTO) || protocol.equals(PacketConstants.UDP_PROTO))) {

							List<OFFlow> tobeAdded = new ArrayList<>();
							List<OFFlow> tobeRemoved = new ArrayList<>();
							// Device 2 Gateway flow
							if (destMac.contains(vlanId)) {
								String switchMac = destMac;
								String deviceMac = srcMac;
								if (protocol.equals(PacketConstants.TCP_PROTO) && packet.getTcpFlag() == SeerPacket.Flag.SYN) {

									OFFlow ofFlow = new OFFlow();
									ofFlow.setSrcMac(deviceMac);
									ofFlow.setDstMac(switchMac);
									ofFlow.setName(dstPort + "-UP-" + deviceMac);
									ofFlow.setDstPort(dstPort);
									ofFlow.setIpProto(protocol);
									ofFlow.setEthType(PacketConstants.ETH_TYPE_IPV4);
									ofFlow.setOfAction(OFFlow.OFAction.NORMAL);
									ofFlow.setPriority(IoTUtil.D2G_FIXED_FLOW_PRIORITY);
									tobeAdded.add(ofFlow);
									List<OFFlow> deviceFlows = IoTUtil
											.getActiveFlows(switchMac, deviceMac, IoTUtil.D2G_DYNAMIC_FLOW_PRIORITY);
									for (OFFlow flow : deviceFlows) {
										if (flow.getIpProto().equals(protocol) && flow.getDstPort().equals(dstPort)) {
											tobeRemoved.remove(flow);
										}
									}
									ofFlow = new OFFlow();
									ofFlow.setSrcMac(switchMac);
									ofFlow.setDstMac(deviceMac);
									ofFlow.setName(dstPort + "-DOWN-" + deviceMac);
									ofFlow.setSrcPort(dstPort);
									ofFlow.setIpProto(protocol);
									ofFlow.setEthType(PacketConstants.ETH_TYPE_IPV4);
									ofFlow.setOfAction(OFFlow.OFAction.NORMAL);
									ofFlow.setPriority(IoTUtil.G2D_FIXED_FLOW_PRIORITY);
									tobeAdded.add(ofFlow);
									deviceFlows = IoTUtil.getActiveFlows(switchMac, deviceMac, IoTUtil.G2D_DYNAMIC_FLOW_PRIORITY);
									for (OFFlow flow : deviceFlows) {
										if (flow.getIpProto().equals(protocol) && flow.getSrcPort().equals(dstPort)) {
											tobeRemoved.add(flow);
										}
									}
									IoTUtil.addFlow(switchMac, tobeAdded);
									IoTUtil.removeFlow(switchMac, tobeRemoved);
								} else if (protocol.equals(PacketConstants.TCP_PROTO) && packet.getTcpFlag() == SeerPacket.Flag.SYN_ACK) {
									OFFlow ofFlow = new OFFlow();
									ofFlow.setSrcMac(deviceMac);
									ofFlow.setDstMac(switchMac);
									ofFlow.setName(srcPort + "-UP-" + deviceMac);
									ofFlow.setSrcPort(srcPort);
									ofFlow.setIpProto(protocol);
									ofFlow.setEthType(PacketConstants.ETH_TYPE_IPV4);
									ofFlow.setOfAction(OFFlow.OFAction.NORMAL);
									ofFlow.setPriority(IoTUtil.D2G_FIXED_FLOW_PRIORITY);
									tobeAdded.add(ofFlow);
									List<OFFlow> deviceFlows = IoTUtil.getActiveFlows(switchMac, deviceMac, IoTUtil.D2G_DYNAMIC_FLOW_PRIORITY);
									for (OFFlow flow : deviceFlows) {
										if (flow.getIpProto().equals(protocol) && flow.getSrcPort().equals(srcPort)) {
											tobeRemoved.add(flow);
										}
									}

									ofFlow = new OFFlow();
									ofFlow.setSrcMac(switchMac);
									ofFlow.setDstMac(deviceMac);
									ofFlow.setDstPort(srcPort);
									ofFlow.setName(srcPort + "-DOWN-" + deviceMac);
									ofFlow.setIpProto(protocol);
									ofFlow.setEthType(PacketConstants.ETH_TYPE_IPV4);
									ofFlow.setOfAction(OFFlow.OFAction.NORMAL);
									ofFlow.setPriority(IoTUtil.G2D_FIXED_FLOW_PRIORITY);
									tobeAdded.add(ofFlow);
									deviceFlows = IoTUtil.getActiveFlows(switchMac, deviceMac, IoTUtil.G2D_DYNAMIC_FLOW_PRIORITY);
									for (OFFlow flow : deviceFlows) {
										if (flow.getIpProto().equals(protocol) && flow.getDstPort().equals(srcPort)) {
											tobeRemoved.add(flow);
										}
									}
									IoTUtil.addFlow(switchMac, tobeAdded);
									IoTUtil.removeFlow(switchMac, tobeRemoved);
								} else {
									OFFlow ofFlow = new OFFlow();
									ofFlow.setSrcMac(deviceMac);
									ofFlow.setDstMac(switchMac);
									ofFlow.setName(dstPort + "-UP-" + deviceMac);
									ofFlow.setDstPort(dstPort);
									ofFlow.setIpProto(protocol);
									ofFlow.setEthType(PacketConstants.ETH_TYPE_IPV4);
									ofFlow.setOfAction(OFFlow.OFAction.NORMAL);
									ofFlow.setPriority(IoTUtil.D2G_DYNAMIC_FLOW_PRIORITY);
									tobeAdded.add(ofFlow);
									IoTUtil.addFlow(switchMac, tobeAdded);
								}
								// Gateway to Device
							} else if (srcMac.contains(vlanId)) {
								String switchMac = srcMac;
								String deviceMac = destMac;
								if (protocol.equals(PacketConstants.TCP_PROTO) && packet.getTcpFlag() == SeerPacket.Flag.SYN) {
									OFFlow ofFlow = new OFFlow();
									ofFlow.setSrcMac(switchMac);
									ofFlow.setDstMac(deviceMac);
									ofFlow.setName(dstPort + "-DOWN-" + deviceMac);
									ofFlow.setDstPort(dstPort);
									ofFlow.setIpProto(protocol);
									ofFlow.setEthType(PacketConstants.ETH_TYPE_IPV4);
									ofFlow.setOfAction(OFFlow.OFAction.NORMAL);
									ofFlow.setPriority(IoTUtil.G2D_FIXED_FLOW_PRIORITY);
									tobeAdded.add(ofFlow);
									List<OFFlow> deviceFlows = IoTUtil.getActiveFlows(switchMac, deviceMac, IoTUtil.G2D_DYNAMIC_FLOW_PRIORITY);
									for (OFFlow flow : deviceFlows) {
										if (flow.getIpProto().equals(protocol) && flow.getDstPort().equals(dstPort)) {
											tobeRemoved.add(flow);
										}
									}

									ofFlow = new OFFlow();
									ofFlow.setSrcMac(deviceMac);
									ofFlow.setDstMac(switchMac);
									ofFlow.setSrcPort(dstPort);
									ofFlow.setName(dstPort + "-UP-" + deviceMac);
									ofFlow.setIpProto(protocol);
									ofFlow.setEthType(PacketConstants.ETH_TYPE_IPV4);
									ofFlow.setOfAction(OFFlow.OFAction.NORMAL);
									ofFlow.setPriority(IoTUtil.D2G_FIXED_FLOW_PRIORITY);
									tobeAdded.add(ofFlow);
									deviceFlows = IoTUtil.getActiveFlows(switchMac, deviceMac, IoTUtil.D2G_DYNAMIC_FLOW_PRIORITY);
									for (OFFlow flow : deviceFlows) {
										if (flow.getIpProto().equals(protocol) && flow.getSrcPort().equals(dstPort)) {
											tobeRemoved.add(flow);
										}
									}
									IoTUtil.addFlow(switchMac, tobeAdded);
									IoTUtil.removeFlow(switchMac, tobeRemoved);

								} else if (protocol.equals(PacketConstants.TCP_PROTO) && packet.getTcpFlag() == SeerPacket.Flag.SYN_ACK) {
									OFFlow ofFlow = new OFFlow();
									ofFlow.setSrcMac(switchMac);
									ofFlow.setDstMac(deviceMac);
									ofFlow.setSrcPort(srcPort);
									ofFlow.setIpProto(protocol);
									ofFlow.setName(dstPort + "-DOWN-" + deviceMac);
									ofFlow.setEthType(PacketConstants.ETH_TYPE_IPV4);
									ofFlow.setOfAction(OFFlow.OFAction.NORMAL);
									ofFlow.setPriority(IoTUtil.G2D_FIXED_FLOW_PRIORITY);
									tobeAdded.add(ofFlow);
									List<OFFlow> deviceFlows = IoTUtil.getActiveFlows(switchMac, deviceMac, IoTUtil.G2D_DYNAMIC_FLOW_PRIORITY);
									for (OFFlow flow : deviceFlows) {
										if (flow.getIpProto().equals(protocol) && flow.getSrcPort().equals(srcPort)) {
											tobeRemoved.add(flow);
										}
									}


									ofFlow = new OFFlow();
									ofFlow.setSrcMac(deviceMac);
									ofFlow.setDstMac(switchMac);
									ofFlow.setDstPort(srcPort);
									ofFlow.setName(srcPort + "-UP-" + deviceMac);
									ofFlow.setIpProto(protocol);
									ofFlow.setEthType(PacketConstants.ETH_TYPE_IPV4);
									ofFlow.setOfAction(OFFlow.OFAction.NORMAL);
									ofFlow.setPriority(IoTUtil.D2G_FIXED_FLOW_PRIORITY);
									tobeAdded.add(ofFlow);
									deviceFlows = IoTUtil.getActiveFlows(switchMac, deviceMac, IoTUtil.D2G_DYNAMIC_FLOW_PRIORITY);
									for (OFFlow flow : deviceFlows) {
										if (flow.getIpProto().equals(protocol) && flow.getDstPort().equals(srcPort)) {
											tobeRemoved.add(flow);
										}
									}
									IoTUtil.addFlow(switchMac, tobeAdded);
									IoTUtil.removeFlow(switchMac, tobeRemoved);
								} else {
									OFFlow ofFlow = new OFFlow();
									ofFlow.setSrcMac(switchMac);
									ofFlow.setDstMac(deviceMac);
									ofFlow.setDstPort(dstPort);
									ofFlow.setName(dstPort + "-DOWN-" + deviceMac);
									ofFlow.setIpProto(protocol);
									ofFlow.setEthType(PacketConstants.ETH_TYPE_IPV4);
									ofFlow.setPriority(IoTUtil.G2D_DYNAMIC_FLOW_PRIORITY);
									ofFlow.setOfAction(OFFlow.OFAction.NORMAL);
									tobeAdded.add(ofFlow);
									IoTUtil.addFlow(switchMac, tobeAdded);
								}
								//
							} else if ((!destMac.contains(vlanId)) && ((!srcMac.contains(vlanId))  && !seerPacketWrapper.getSeerPacket().isDstIgnore())) {
								String switchMac = getSwitchMac(vlanId);
								if (protocol.equals(PacketConstants.TCP_PROTO) && packet.getTcpFlag() == SeerPacket.Flag.SYN) {
									String deviceMac = destMac;
									OFFlow ofFlow = new OFFlow();
									ofFlow.setDstMac(deviceMac);
									ofFlow.setDstPort(dstPort);
									ofFlow.setName(dstPort + "-LAN_TO-" + deviceMac);
									ofFlow.setIpProto(protocol);
									ofFlow.setEthType(PacketConstants.ETH_TYPE_IPV4);
									ofFlow.setOfAction(OFFlow.OFAction.NORMAL);
									ofFlow.setPriority(IoTUtil.L2D_FIXED_FLOW_PRIORITY);
									tobeAdded.add(ofFlow);
									List<OFFlow> deviceFlows = IoTUtil.getActiveFlows(switchMac, deviceMac, IoTUtil.L2D_DYNAMIC_FLOW_PRIORITY);
									for (OFFlow flow : deviceFlows) {
										if (flow.getIpProto().equals(protocol) && flow.getDstPort().equals(dstPort)) {
											tobeRemoved.add(flow);
										}
									}

									ofFlow = new OFFlow();
									ofFlow.setSrcMac(deviceMac);
									ofFlow.setSrcPort(dstPort);
									ofFlow.setIpProto(protocol);
									ofFlow.setName(dstPort + "-LAN_FROM-" + deviceMac);
									ofFlow.setEthType(PacketConstants.ETH_TYPE_IPV4);
									ofFlow.setOfAction(OFFlow.OFAction.NORMAL);
									ofFlow.setPriority(IoTUtil.L2D_FIXED_FLOW_PRIORITY);
									tobeAdded.add(ofFlow);

									IoTUtil.addFlow(switchMac, tobeAdded);
									IoTUtil.removeFlow(switchMac, tobeRemoved);

								} else if (protocol.equals(PacketConstants.TCP_PROTO) && packet.getTcpFlag() == SeerPacket.Flag.SYN_ACK) {
									String deviceMac = srcMac;

									OFFlow ofFlow = new OFFlow();
									ofFlow.setDstMac(deviceMac);
									ofFlow.setDstPort(srcPort);
									ofFlow.setIpProto(protocol);
									ofFlow.setName(dstPort + "-LAN_TO-" + deviceMac);
									ofFlow.setEthType(PacketConstants.ETH_TYPE_IPV4);
									ofFlow.setOfAction(OFFlow.OFAction.NORMAL);
									ofFlow.setPriority(IoTUtil.L2D_FIXED_FLOW_PRIORITY);

									List<OFFlow> deviceFlows = IoTUtil.getActiveFlows(switchMac, deviceMac, IoTUtil.L2D_DYNAMIC_FLOW_PRIORITY);
									for (OFFlow flow : deviceFlows) {
										if (flow.getIpProto().equals(protocol) && flow.getDstPort().equals(srcPort)) {
											tobeRemoved.add(flow);
										}
									}
									tobeAdded.add(ofFlow);

									ofFlow = new OFFlow();
									ofFlow.setSrcMac(deviceMac);
									ofFlow.setSrcPort(srcPort);
									ofFlow.setIpProto(protocol);
									ofFlow.setName(dstPort + "-LAN_FROM-" + deviceMac);
									ofFlow.setEthType(PacketConstants.ETH_TYPE_IPV4);
									ofFlow.setOfAction(OFFlow.OFAction.NORMAL);
									ofFlow.setPriority(IoTUtil.L2D_FIXED_FLOW_PRIORITY);
									tobeAdded.add(ofFlow);

									IoTUtil.addFlow(switchMac, tobeAdded);
									IoTUtil.removeFlow(switchMac, tobeRemoved);

								} else {
									String deviceMac = destMac;
									OFFlow ofFlow = new OFFlow();
									ofFlow.setDstMac(deviceMac);
									ofFlow.setDstPort(dstPort);
									ofFlow.setIpProto(protocol);
									ofFlow.setName(dstPort + "-LAN_TO-" + deviceMac);
									ofFlow.setEthType(PacketConstants.ETH_TYPE_IPV4);
									ofFlow.setPriority(IoTUtil.L2D_DYNAMIC_FLOW_PRIORITY);
									ofFlow.setOfAction(OFFlow.OFAction.NORMAL);
									tobeAdded.add(ofFlow);

									IoTUtil.addFlow(switchMac, tobeAdded);
								}
							}
						}
					}

				} catch (ExecutionException e) {
					log.error("task execution failed", e);
				} catch (OFControllerException e) {
					log.error("Failed to access the controller", e);
				} catch (SeerManagementException e) {
					log.error("Failed to access seer management func's", e);
				}
				seerPacketWrapper = packetQueue.remove();
			}
		};
		iotTaskExecutor.scheduleWithFixedDelay(task, 10, 20, TimeUnit.SECONDS);
	}

	private String getSwitchMac(String vlanId) throws SeerManagementException {
		String switchMac = null;
		switchMac = vlanMacMapping.get(vlanId);
		if (switchMac == null) {
			String dpId = IoTSecurityDataHolder.getSeerMgtService().getSwitchFromVlanId(vlanId).getDpId();
			if (dpId != null) {
				switchMac = dpId.substring(6);
				vlanMacMapping.put(vlanId, switchMac);
			}
		}
		return switchMac;
	}

	@Override
	public void processPacket(SeerPacket seerPacket) {
		String srcMac = seerPacket.getSrcMac();
		String destMac = seerPacket.getDstMac();

		if (!srcMac.contains(seerPacket.getVlanId())) {
			addToQueue(new SeerPacketWrapper(seerPacket, srcMac, seerPacket.getVlanId()));
		}

		if (!destMac.contains(seerPacket.getVlanId()) && !seerPacket.isDstIgnore()) {
			addToQueue(new SeerPacketWrapper(seerPacket, srcMac, seerPacket.getVlanId()));
		}
	}

	private void addToQueue(SeerPacketWrapper packet) {
		try {
			if (IoTSecurityDataHolder.getStatsCache().get(new DeviceIdentifier(packet.getVlanId(),
					packet.getDeviceMac(), true)) != null) {
				packetQueue.add(packet);
			}
		} catch (ExecutionException e) {
			log.error(e.getMessage(), e);
		}
	}


}
