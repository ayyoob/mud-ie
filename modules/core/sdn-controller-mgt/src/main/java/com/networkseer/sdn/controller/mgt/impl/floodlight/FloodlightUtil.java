package com.networkseer.sdn.controller.mgt.impl.floodlight;
import com.networkseer.common.openflow.OFFlow.OFAction;

import com.networkseer.common.openflow.OFFlow;

import java.util.ArrayList;
import java.util.List;

public class FloodlightUtil {

	public static FlowDTO transform(OFFlow ofFlow, String dpId) {
		FlowDTO flowDTO = new FlowDTO();
		flowDTO.setAswitch(dpId);
		flowDTO.setPriority(ofFlow.getPriority());
		flowDTO.setHard_timeout(ofFlow.getHardTimeout());
		flowDTO.setName(ofFlow.getName());
		flowDTO.setEth_src(ofFlow.getSrcMac());
		flowDTO.setEth_dst(ofFlow.getDstMac());
		flowDTO.setIp_proto(ofFlow.getIpProto());
		flowDTO.setEth_type(ofFlow.getEthType());
		flowDTO.setTp_dst(ofFlow.getDstIp());
		flowDTO.setTp_src(ofFlow.getSrcIp());
		flowDTO.setIpv4_src(ofFlow.getSrcIp());
		flowDTO.setIpv4_dst(ofFlow.getDstIp());
		return flowDTO;
	}


	public static List<OFFlow> transform(PerSwitchStatFlowsDTO perSwitchStatFlowsDTO) {
		List<OFFlow> ofFlows = new ArrayList<>();
		for (StatFlowDTO statFlowDTO : perSwitchStatFlowsDTO.getFlows()) {
			OFFlow ofFlow = new OFFlow();
			ofFlow.setPacketCount(Long.parseLong(statFlowDTO.getPacketCount()));
			ofFlow.setPriority(Integer.parseInt(statFlowDTO.getPriority()));
			ofFlow.setSrcMac(getValue(statFlowDTO.getMatch().getEthSrc()));
			ofFlow.setDstMac(getValue(statFlowDTO.getMatch().getEthDst()));
			ofFlow.setEthType(getValue(statFlowDTO.getMatch().getEthType()));
			ofFlow.setSrcIp(getValue(statFlowDTO.getMatch().getIpv4Src()));
			ofFlow.setDstIp(getValue(statFlowDTO.getMatch().getIpv4Dst()));
			ofFlow.setIpProto(getValue(statFlowDTO.getMatch().getIpProto()));
			ofFlow.setSrcPort(getValue(statFlowDTO.getMatch().getSrcPort()));
			ofFlow.setDstPort(getValue(statFlowDTO.getMatch().getDstPort()));
			ofFlow.setByteCount(Long.parseLong(statFlowDTO.getByteCount()));
			ofFlow.setHardTimeout(0);
			if (statFlowDTO.getActions().getActions().contains("drop")) {
				ofFlow.setOfAction(OFAction.DROP);
			} else if(statFlowDTO.getActions().getActions().contains(",output=")) {
				ofFlow.setOfAction(OFAction.MIRROR_TO_VXLAN);
			} else {
				ofFlow.setOfAction(OFAction.NORMAL);
			}
			ofFlows.add(ofFlow);
		}
		return ofFlows;
	}

	private static String getValue(String value) {
		if (value == null) {
			return "*";
		}
		return value;
	}
}
