package com.networkseer.sdn.controller.mgt.impl;

import com.networkseer.sdn.controller.mgt.impl.faucet.Rule;

public class AddFlowMsg {

	private String dpId;
	private Rule rule;

	public String getDpId() {
		return dpId;
	}

	public void setDpId(String dpId) {
		this.dpId = dpId;
	}

	public Rule getRule() {
		return rule;
	}

	public void setRule(Rule rule) {
		this.rule = rule;
	}
}
