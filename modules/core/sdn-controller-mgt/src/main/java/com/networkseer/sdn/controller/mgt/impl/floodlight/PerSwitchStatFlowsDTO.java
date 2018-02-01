package com.networkseer.sdn.controller.mgt.impl.floodlight;

import java.util.List;

public class PerSwitchStatFlowsDTO {

	private List<StatFlowDTO> flows;

	public List<StatFlowDTO> getFlows() {
		return flows;
	}

	public void setFlows(List<StatFlowDTO> flows) {
		this.flows = flows;
	}
}
