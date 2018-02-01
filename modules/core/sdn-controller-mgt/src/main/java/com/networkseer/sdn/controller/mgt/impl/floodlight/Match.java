package com.networkseer.sdn.controller.mgt.impl.floodlight;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Match {
	@JsonProperty("eth_src")
	private String ethSrc;
	@JsonProperty("eth_dst")
	private String ethDst;
	@JsonProperty("ip_proto")
	private String ipProto;
	@JsonProperty("eth_type")
	private String ethType;
	@JsonProperty("tp_dst")
	private String dstPort;
	@JsonProperty("tp_src")
	private String srcPort;
	private String actions;
	@JsonProperty("ipv4_src")
	private String ipv4Src;
	@JsonProperty("ipv4_dst")
	private String ipv4Dst;

	public String getEthSrc() {
		return ethSrc;
	}

	public void setEthSrc(String ethSrc) {
		this.ethSrc = ethSrc;
	}

	public String getEthDst() {
		return ethDst;
	}

	public void setEthDst(String ethDst) {
		this.ethDst = ethDst;
	}

	public String getIpProto() {
		return ipProto;
	}

	public void setIpProto(String ipProto) {
		this.ipProto = ipProto;
	}

	public String getEthType() {
		return ethType;
	}

	public void setEthType(String ethType) {
		this.ethType = ethType;
	}

	public String getDstPort() {
		return dstPort;
	}

	public void setDstPort(String dstPort) {
		this.dstPort = dstPort;
	}

	public String getSrcPort() {
		return srcPort;
	}

	public void setSrcPort(String srcPort) {
		this.srcPort = srcPort;
	}

	public String getActions() {
		return actions;
	}

	public void setActions(String actions) {
		this.actions = actions;
	}

	public String getIpv4Src() {
		return ipv4Src;
	}

	public void setIpv4Src(String ipv4Src) {
		this.ipv4Src = ipv4Src;
	}

	public String getIpv4Dst() {
		return ipv4Dst;
	}

	public void setIpv4Dst(String ipv4Dst) {
		this.ipv4Dst = ipv4Dst;
	}
}
