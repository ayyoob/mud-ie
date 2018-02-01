package com.networkseer.sdn.controller.mgt.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networkseer.sdn.controller.mgt.OFController;
import com.networkseer.sdn.controller.mgt.dto.OFFlow;
import com.networkseer.sdn.controller.mgt.exception.OFControllerException;
import com.networkseer.sdn.controller.mgt.impl.floodlight.*;
import com.networkseer.sdn.controller.mgt.internal.SdnControllerDataHolder;
import feign.FeignException;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FloodlightOFControllerImpl implements OFController {
	private static final String DEFAULT_FLOW_ACTION = "output=normal";
	private static final String VXLAN_FLOW_ACTION_PREFIX = ",output=";
	private static final String VXLAN_INTERFACE_NAME = "tun-vxlan";

	@Override
	public void addFlow(String dpId, OFFlow ofFlow) throws OFControllerException {
		try {
			FloodlightAPI floodlightAPI = SdnControllerDataHolder.getFloodlightAPI();
			FlowDTO flowDTO = FloodlightUtil.transform(ofFlow, dpId);
			if (ofFlow.getOfAction() == OFFlow.OFAction.MIRROR_TO_VXLAN) {
				String vxlanPort = getVxLanPortId(dpId);
				flowDTO.setActions(DEFAULT_FLOW_ACTION + VXLAN_FLOW_ACTION_PREFIX + vxlanPort);
			} else {
				flowDTO.setActions(DEFAULT_FLOW_ACTION);
			}
			floodlightAPI.addFlow(flowDTO);
		} catch (FeignException e) {
			throw new OFControllerException(e);
		}
	}

	@Override
	public void addFlows(String dpId, List<OFFlow> ofFlows) throws OFControllerException {
		String vxlanPort = null;
		for (OFFlow flow : ofFlows) {
			if (flow.getOfAction()== OFFlow.OFAction.MIRROR_TO_VXLAN) {
				vxlanPort = getVxLanPortId(dpId);
				break;
			}
		}
		for (OFFlow ofFlow : ofFlows) {
			try {
				FloodlightAPI floodlightAPI = SdnControllerDataHolder.getFloodlightAPI();
				FlowDTO flowDTO = FloodlightUtil.transform(ofFlow, dpId);
				if (ofFlow.getOfAction() == OFFlow.OFAction.MIRROR_TO_VXLAN) {
					flowDTO.setActions(DEFAULT_FLOW_ACTION + VXLAN_FLOW_ACTION_PREFIX + vxlanPort);
				} else {
					flowDTO.setActions(DEFAULT_FLOW_ACTION);
				}
				floodlightAPI.addFlow(flowDTO);
			} catch (FeignException e) {
				throw new OFControllerException(e);
			}
		}

	}

	@Override
	public void removeFlow(String dpId, OFFlow ofFlow) throws OFControllerException {
		try {
			FloodlightAPI floodlightAPI = SdnControllerDataHolder.getFloodlightAPI();
			RemoveFlowDTO removeFlowDTO = new RemoveFlowDTO();
			removeFlowDTO.setName(ofFlow.getName());
			floodlightAPI.removeFlow(removeFlowDTO);
		} catch (FeignException e) {
			throw new OFControllerException(e);
		}
	}

	@Override
	public void clearAllFlows(String dpId) throws OFControllerException {
		try {
			FloodlightAPI floodlightAPI = SdnControllerDataHolder.getFloodlightAPI();
			floodlightAPI.clearFlowRules(dpId);
		} catch (FeignException e) {
			throw new OFControllerException(e);
		}
	}

	@Override
	public List<OFFlow> getFlowStats(String dpId) throws OFControllerException {
		try {
			FloodlightAPI floodlightAPI = SdnControllerDataHolder.getFloodlightAPI();
			PerSwitchStatFlowsDTO perSwitchStatFlowsDTO = floodlightAPI.getFlows(dpId);
			return FloodlightUtil.transform(perSwitchStatFlowsDTO);
		} catch (FeignException e) {
			throw new OFControllerException(e);
		}
	}

	@Override
	public Map<String, List<OFFlow>> getFlowStats() throws OFControllerException {
		try {
			Map<String, List<OFFlow>> flowMap = new HashMap<>();
			FloodlightAPI floodlightAPI = SdnControllerDataHolder.getFloodlightAPI();
			String payload = floodlightAPI.getFlows();
			if (payload != null) {
				ObjectMapper mapper = new ObjectMapper();
				Map<String, PerSwitchStatFlowsDTO> map =
						mapper.readValue(payload, new TypeReference<Map<String, PerSwitchStatFlowsDTO>>() {});
				for (String mac : map.keySet()) {
					List<OFFlow> ofFlows = FloodlightUtil.transform(map.get(mac));
					flowMap.put(mac, ofFlows);
				}
			}

			return flowMap;
		} catch (FeignException | IOException e) {
			throw new OFControllerException(e);
		}
	}

	private String getVxLanPortId(String dpId) throws OFControllerException {
		PortInfo portInfo = getSwitchPortInformation(dpId);
		for (PortDesc portDesc : portInfo.getPortDesc()) {
			if (portDesc.getName().equals(VXLAN_INTERFACE_NAME)) {
				return portDesc.getPortNumber();
			}
		}
		throw new OFControllerException("Vxlan is not configured for" + dpId);
	}

	private PortInfo getSwitchPortInformation(String dpId) throws OFControllerException {
		try {
			FloodlightAPI floodlightAPI = SdnControllerDataHolder.getFloodlightAPI();
			return floodlightAPI.getSwitchPortInfo(dpId);
		} catch (FeignException e) {
			throw new OFControllerException(e);
		}
	}
}
