package com.networkseer.sdn.controller.mgt.impl.faucet;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown=true)
public class L2LearnWrapper {

	@JsonProperty("dp_id")
	private long dpId;

	@JsonProperty("L2_LEARN")
	private L2Learn l2Learn;

	public long getDpId() {
		return dpId;
	}

	public void setDpId(long dpId) {
		this.dpId = dpId;
	}

	public L2Learn getL2Learn() {
		return l2Learn;
	}

	public void setL2Learn(L2Learn l2Learn) {
		this.l2Learn = l2Learn;
	}

}
