package com.networkseer.iot.security;

import com.networkseer.common.openflow.OFFlow;
import com.networkseer.common.packet.PacketConstants;
import com.networkseer.iot.security.dto.DeviceIdentifier;
import com.networkseer.iot.security.internal.IoTSecurityDataHolder;
import com.networkseer.sdn.controller.mgt.OFController;
import com.networkseer.sdn.controller.mgt.exception.OFControllerException;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class IoTUtil {
	public static final int MINIMUM_IOT_FLOW_RULES = 10;
	public static final int COMMON_FLOW_PRIORITY = 1000;
	public static final int D2G_FIXED_FLOW_PRIORITY = 850;
	public static final int D2G_DYNAMIC_FLOW_PRIORITY = 810;
	public static final int D2G_PRIORITY = 800;
	public static final int G2D_FIXED_FLOW_PRIORITY = 750;
	public static final int G2D_DYNAMIC_FLOW_PRIORITY = 710;
	public static final int G2D_PRIORITY = 700;
	public static final int L2D_FIXED_FLOW_PRIORITY = 650;
	public static final int L2D_DYNAMIC_FLOW_PRIORITY = 610;
	public static final int L2D_PRIORITY = 600;
	public static final int SKIP_FLOW_PRIORITY = 400;
	public static final int SKIP_FLOW_HIGHER_PRIORITY = 1400;
	public static final String DPID_PREFIX = "00:00:";

	public static void initializeDeviceFlows(String dpId, String deviceMac) throws OFControllerException {
		String gwMac = dpId.substring(6);
		List<OFFlow> flowList = new ArrayList<>();
		//SKIP GATEWAY
		OFFlow ofFlow = new OFFlow();
//		ofFlow.setSrcMac(deviceMac);
//		ofFlow.setDstMac(gwMac);
//		ofFlow.setDstIp(OFController.getInstance().getSwitch(dpId).getIp());
//		ofFlow.setEthType(Constants.ETH_TYPE_IPV4);
//		ofFlow.setPriority(SKIP_FLOW_HIGHER_PRIORITY);
//		ofFlow.setOfAction(OFFlow.OFAction.NORMAL);
//		OFController.getInstance().addFlow(dpId, ofFlow);
//
//		ofFlow = new OFFlow();
//		ofFlow.setDstMac(deviceMac);
//		ofFlow.setSrcMac(gwMac);
//		ofFlow.setSrcIp(OFController.getInstance().getSwitch(dpId).getIp());
//		ofFlow.setEthType(PacketConstants.ETH_TYPE_IPV4);
//		ofFlow.setPriority(SKIP_FLOW_HIGHER_PRIORITY);
//		ofFlow.setOfAction(OFFlow.OFAction.NORMAL);
//		OFController.getInstance().addFlow(dpId, ofFlow);

		//DNS
		ofFlow = new OFFlow();
		ofFlow.setSrcMac(deviceMac);
		ofFlow.setName("ARP-UP-" + deviceMac);
		ofFlow.setEthType(PacketConstants.ETH_TYPE_ARP);
		ofFlow.setPriority(COMMON_FLOW_PRIORITY);
		ofFlow.setOfAction(OFFlow.OFAction.NORMAL);
		flowList.add(ofFlow);

		ofFlow = new OFFlow();
		ofFlow.setDstMac(deviceMac);
		ofFlow.setName("ARP-DOWN-" + deviceMac);
		ofFlow.setEthType(PacketConstants.ETH_TYPE_ARP);
		ofFlow.setPriority(COMMON_FLOW_PRIORITY);
		ofFlow.setOfAction(OFFlow.OFAction.NORMAL);
		flowList.add(ofFlow);

		ofFlow = new OFFlow();
		ofFlow.setSrcMac(deviceMac);
		ofFlow.setDstMac(gwMac);
		ofFlow.setName("DNS-UP-" + deviceMac);
		ofFlow.setIpProto(PacketConstants.UDP_PROTO);
		ofFlow.setDstPort(PacketConstants.DNS_PORT);
		ofFlow.setEthType(PacketConstants.ETH_TYPE_IPV4);
		ofFlow.setPriority(COMMON_FLOW_PRIORITY);
		ofFlow.setOfAction(OFFlow.OFAction.NORMAL);
		flowList.add(ofFlow);

		ofFlow = new OFFlow();
		ofFlow.setSrcMac(gwMac);
		ofFlow.setDstMac(deviceMac);
		ofFlow.setName("DNS-DOWN-" + deviceMac);
		ofFlow.setIpProto(PacketConstants.UDP_PROTO);
		ofFlow.setSrcPort(PacketConstants.DNS_PORT);
		ofFlow.setEthType(PacketConstants.ETH_TYPE_IPV4);
		ofFlow.setPriority(COMMON_FLOW_PRIORITY);
		ofFlow.setOfAction(OFFlow.OFAction.NORMAL);
		flowList.add(ofFlow);

		//NTP
		ofFlow = new OFFlow();
		ofFlow.setSrcMac(deviceMac);
		ofFlow.setDstMac(gwMac);
		ofFlow.setName("NTP-UP-" + deviceMac);
		ofFlow.setIpProto(PacketConstants.UDP_PROTO);
		ofFlow.setDstPort(PacketConstants.NTP_PORT);
		ofFlow.setEthType(PacketConstants.ETH_TYPE_IPV4);
		ofFlow.setPriority(COMMON_FLOW_PRIORITY);
		ofFlow.setOfAction(OFFlow.OFAction.NORMAL);
		flowList.add(ofFlow);

		ofFlow = new OFFlow();
		ofFlow.setName("NTP-DOWN-" + deviceMac);
		ofFlow.setSrcMac(gwMac);
		ofFlow.setDstMac(deviceMac);
		ofFlow.setIpProto(PacketConstants.UDP_PROTO);
		ofFlow.setSrcPort(PacketConstants.NTP_PORT);
		ofFlow.setEthType(PacketConstants.ETH_TYPE_IPV4);
		ofFlow.setPriority(COMMON_FLOW_PRIORITY);
		ofFlow.setOfAction(OFFlow.OFAction.NORMAL);
		flowList.add(ofFlow);

		//ICMP
		ofFlow = new OFFlow();
		ofFlow.setSrcMac(deviceMac);
		ofFlow.setDstMac(gwMac);
		ofFlow.setName("ICMP-UP-" + deviceMac);
		ofFlow.setIpProto(PacketConstants.ICMP_PROTO);
		ofFlow.setEthType(PacketConstants.ETH_TYPE_IPV4);
		ofFlow.setPriority(COMMON_FLOW_PRIORITY);
		ofFlow.setOfAction(OFFlow.OFAction.NORMAL);
		flowList.add(ofFlow);

		ofFlow = new OFFlow();
		ofFlow.setSrcMac(gwMac);
		ofFlow.setDstMac(deviceMac);
		ofFlow.setName("ICMP-DOWN-" + deviceMac);
		ofFlow.setIpProto(PacketConstants.ICMP_PROTO);
		ofFlow.setEthType(PacketConstants.ETH_TYPE_IPV4);
		ofFlow.setPriority(COMMON_FLOW_PRIORITY);
		ofFlow.setOfAction(OFFlow.OFAction.NORMAL);
		flowList.add(ofFlow);

		//Device -> GW
		ofFlow = new OFFlow();
		ofFlow.setSrcMac(deviceMac);
		ofFlow.setDstMac(gwMac);
		ofFlow.setName("TCP-UP-" + deviceMac);
		ofFlow.setIpProto(PacketConstants.TCP_PROTO);
		ofFlow.setEthType(PacketConstants.ETH_TYPE_IPV4);
		ofFlow.setPriority(D2G_PRIORITY);
		ofFlow.setOfAction(OFFlow.OFAction.MIRROR_TO_VXLAN);
		flowList.add(ofFlow);

		ofFlow = new OFFlow();
		ofFlow.setSrcMac(deviceMac);
		ofFlow.setDstMac(gwMac);
		ofFlow.setName("UDP-UP-" + deviceMac);
		ofFlow.setIpProto(PacketConstants.UDP_PROTO);
		ofFlow.setEthType(PacketConstants.ETH_TYPE_IPV4);
		ofFlow.setPriority(D2G_PRIORITY);
		ofFlow.setOfAction(OFFlow.OFAction.MIRROR_TO_VXLAN);
		flowList.add(ofFlow);

		//GW - > Device

		ofFlow = new OFFlow();
		ofFlow.setSrcMac(gwMac);
		ofFlow.setDstMac(deviceMac);
		ofFlow.setName("TCP-DOWN-" + deviceMac);
		ofFlow.setIpProto(PacketConstants.TCP_PROTO);
		ofFlow.setEthType(PacketConstants.ETH_TYPE_IPV4);
		ofFlow.setPriority(G2D_PRIORITY);
		ofFlow.setOfAction(OFFlow.OFAction.MIRROR_TO_VXLAN);
		flowList.add(ofFlow);

		ofFlow = new OFFlow();
		ofFlow.setSrcMac(gwMac);
		ofFlow.setDstMac(deviceMac);
		ofFlow.setName("UDP-DOWN-" + deviceMac);
		ofFlow.setIpProto(PacketConstants.UDP_PROTO);
		ofFlow.setEthType(PacketConstants.ETH_TYPE_IPV4);
		ofFlow.setPriority(G2D_PRIORITY);
		ofFlow.setOfAction(OFFlow.OFAction.MIRROR_TO_VXLAN);
		flowList.add(ofFlow);

		//Local
		ofFlow = new OFFlow();
		ofFlow.setDstMac(deviceMac);
		ofFlow.setName("TCP-LOCAL-" + deviceMac);
		ofFlow.setEthType(PacketConstants.ETH_TYPE_IPV4);
		ofFlow.setPriority(L2D_PRIORITY);
		ofFlow.setIpProto(PacketConstants.TCP_PROTO);
		ofFlow.setOfAction(OFFlow.OFAction.MIRROR_TO_VXLAN);
		flowList.add(ofFlow);

		ofFlow = new OFFlow();
		ofFlow.setDstMac(deviceMac);
		ofFlow.setName("UDP-LOCAL-" + deviceMac);
		ofFlow.setEthType(PacketConstants.ETH_TYPE_IPV4);
		ofFlow.setPriority(L2D_PRIORITY);
		ofFlow.setIpProto(PacketConstants.UDP_PROTO);
		ofFlow.setOfAction(OFFlow.OFAction.MIRROR_TO_VXLAN);
		flowList.add(ofFlow);

		ofFlow = new OFFlow();
		ofFlow.setDstMac(deviceMac);
		ofFlow.setName("ICMP-LOCAL-" + deviceMac);
		ofFlow.setIpProto(PacketConstants.ICMP_PROTO);
		ofFlow.setEthType(PacketConstants.ETH_TYPE_IPV4);
		ofFlow.setPriority(L2D_PRIORITY + 1);
		ofFlow.setOfAction(OFFlow.OFAction.NORMAL);
		flowList.add(ofFlow);

		ofFlow = new OFFlow();
		ofFlow.setDstMac(deviceMac);
		ofFlow.setName("ALL-FROM-" + deviceMac);
		ofFlow.setPriority(SKIP_FLOW_PRIORITY);
		ofFlow.setOfAction(OFFlow.OFAction.NORMAL);
		flowList.add(ofFlow);

		ofFlow = new OFFlow();
		ofFlow.setSrcMac(deviceMac);
		ofFlow.setName("ALL-TO-" + deviceMac);
		ofFlow.setPriority(SKIP_FLOW_PRIORITY);
		ofFlow.setOfAction(OFFlow.OFAction.NORMAL);
		flowList.add(ofFlow);

		IoTSecurityDataHolder.getOfController().addFlows(dpId, flowList);
	}

	public static List<OFFlow> getActiveFlows(String switchMac, String deviceMac, int priority) throws ExecutionException {

		List<OFFlow> flowList = IoTSecurityDataHolder.getStatsCache().get(new DeviceIdentifier(DPID_PREFIX + switchMac, deviceMac));
		List<OFFlow> deviceFlowList = new ArrayList<OFFlow>();
		for (OFFlow ofFlow : flowList) {
			if (ofFlow.getPriority() == priority) {
				deviceFlowList.add(ofFlow);
			}
		}
		return deviceFlowList;
	}

	public static void removeFlow(String switchMac, List<OFFlow> ofFlows) throws OFControllerException {
		String dpId = DPID_PREFIX + switchMac;
		IoTSecurityDataHolder.getOfController().addFlows(dpId, ofFlows);
	}

	public static void addFlow(String switchMac, List<OFFlow> ofFlows) throws OFControllerException {
		String dpId = DPID_PREFIX + switchMac;
		IoTSecurityDataHolder.getOfController().removeFlows(dpId, ofFlows);
	}
}
