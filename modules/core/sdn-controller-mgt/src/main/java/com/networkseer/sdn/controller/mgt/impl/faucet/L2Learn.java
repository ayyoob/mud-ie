package com.networkseer.sdn.controller.mgt.impl.faucet;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown=true)
public class L2Learn {
	@JsonProperty("port_no")
	private long portNumber;
	@JsonProperty("vid")
	private int vlanId;
	@JsonProperty("eth_src")
	private String ethSrc;

	public long getPortNumber() {
		return portNumber;
	}

	public void setPortNumber(long portNumber) {
		this.portNumber = portNumber;
	}

	public int getVlanId() {
		return vlanId;
	}

	public void setVlanId(int vlanId) {
		this.vlanId = vlanId;
	}

	public String getEthSrc() {
		return ethSrc;
	}

	public void setEthSrc(String ethSrc) {
		this.ethSrc = ethSrc;
	}
}
