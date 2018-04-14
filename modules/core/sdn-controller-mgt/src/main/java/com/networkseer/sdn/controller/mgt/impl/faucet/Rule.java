package com.networkseer.sdn.controller.mgt.impl.faucet;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.networkseer.common.openflow.OFFlow;
import com.networkseer.common.packet.PacketConstants;
import com.networkseer.sdn.controller.mgt.impl.FaucetOFControllerImpl;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "eth_src", "eth_dst", "eth_type", "ip_proto", "ipv4_src", "ipv4_dst"
		, "ipv6_src", "ipv6_dst", "udp_src", "udp_dst","tcp_src", "tcp_dst", "icmpv4_type", "icmpv4_code"
		,"icmpv6_type", "icmpv6_code", "actions" })
public class Rule {

	@JsonProperty("eth_src")
	private String ethSrc;

	@JsonProperty("eth_dst")
	private String ethDst;

	@JsonProperty("eth_type")
	private Integer ethType;

	@JsonProperty("ip_proto")
	private Integer ipProto;

	@JsonProperty("ipv4_src")
	private String ipv4Src;

	@JsonProperty("ipv4_dst")
	private String ipv4Dst;

	@JsonProperty("ipv6_src")
	private String ipv6Src;

	@JsonProperty("ipv6_dst")
	private String ipv6Dst;

	@JsonProperty("udp_src")
	private Integer udpSrc;

	@JsonProperty("udp_dst")
	private Integer udpDst;

	@JsonProperty("tcp_src")
	private Integer tcpSrc;

	@JsonProperty("tcp_dst")
	private Integer tcpDst;

	@JsonProperty("icmpv4_type")
	private Integer icmpv4Type;

	@JsonProperty("icmpv4_code")
	private Integer icmpv4Code;

	@JsonProperty("icmpv6_type")
	private Integer icmpv6Type;

	@JsonProperty("icmpv6_code")
	private Integer icmpv6Code;


	private Actions actions;

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

	public Integer getEthType() {
		return ethType;
	}

	public void setEthType(Integer ethType) {
		this.ethType = ethType;
	}

	public Integer getIpProto() {
		return ipProto;
	}

	public void setIpProto(Integer ipProto) {
		this.ipProto = ipProto;
	}

	public Integer getUdpSrc() {
		return udpSrc;
	}

	public void setUdpSrc(Integer udpSrc) {
		this.udpSrc = udpSrc;
	}

	public Integer getUdpDst() {
		return udpDst;
	}

	public void setUdpDst(Integer udpDst) {
		this.udpDst = udpDst;
	}

	public Integer getTcpSrc() {
		return tcpSrc;
	}

	public void setTcpSrc(Integer tcpSrc) {
		this.tcpSrc = tcpSrc;
	}

	public Integer getTcpDst() {
		return tcpDst;
	}

	public void setTcpDst(Integer tcpDst) {
		this.tcpDst = tcpDst;
	}

	public Integer getIcmpv4Type() {
		return icmpv4Type;
	}

	public void setIcmpv4Type(Integer icmpv4Type) {
		this.icmpv4Type = icmpv4Type;
	}

	public Integer getIcmpv4Code() {
		return icmpv4Code;
	}

	public void setIcmpv4Code(Integer icmpv4Code) {
		this.icmpv4Code = icmpv4Code;
	}

	public Integer getIcmpv6Type() {
		return icmpv6Type;
	}

	public void setIcmpv6Type(Integer icmpv6Type) {
		this.icmpv6Type = icmpv6Type;
	}

	public Integer getIcmpv6Code() {
		return icmpv6Code;
	}

	public void setIcmpv6Code(Integer icmpv6Code) {
		this.icmpv6Code = icmpv6Code;
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

	public String getIpv6Src() {
		return ipv6Src;
	}

	public void setIpv6Src(String ipv6Src) {
		this.ipv6Src = ipv6Src;
	}

	public String getIpv6Dst() {
		return ipv6Dst;
	}

	public void setIpv6Dst(String ipv6Dst) {
		this.ipv6Dst = ipv6Dst;
	}

	public Actions getActions() {
		return actions;
	}

	public void setActions(Actions actions) {
		this.actions = actions;
	}

	public void setOFFlow(OFFlow ofFlow) {
		ethSrc = getValue(ofFlow.getSrcMac());
		ethDst = getValue(ofFlow.getDstMac());
		ethType = Integer.decode(ofFlow.getEthType());
		ipProto = getIntegerValue(ofFlow.getIpProto());
		if (ethType != null && ethType.equals(Integer.decode(PacketConstants.ETH_TYPE_IPV4))) {
			ipv4Src = getValue(ofFlow.getSrcIp());
			ipv4Dst = getValue(ofFlow.getDstIp());
			icmpv4Code = getIntegerValue(ofFlow.getIcmpCode());
			icmpv4Type = getIntegerValue(ofFlow.getIcmpType());
		} else if (ethType != null && ethType.equals(Integer.decode(PacketConstants.ETH_TYPE_IPV6))) {
			ipv6Src = getValue(ofFlow.getSrcIp());
			ipv6Dst = getValue(ofFlow.getDstIp());
			icmpv6Code = getIntegerValue(ofFlow.getIcmpCode());
			icmpv6Type = getIntegerValue(ofFlow.getIcmpType());
		}

		if (ipProto != null && ipProto.equals(getIntegerValue(PacketConstants.TCP_PROTO))) {
			tcpSrc = getIntegerValue(ofFlow.getSrcPort());
			tcpDst = getIntegerValue(ofFlow.getDstPort());
		} else if (ipProto != null && ipProto.equals(getIntegerValue(PacketConstants.UDP_PROTO))) {
			udpSrc = getIntegerValue(ofFlow.getSrcPort());
			udpDst = getIntegerValue(ofFlow.getDstPort());
		}
		actions = new Actions();
		if (ofFlow.getOfAction() == OFFlow.OFAction.DROP) {
			actions.setAllow(0);
		} else if (ofFlow.getOfAction() == OFFlow.OFAction.MIRROR_TO_VXLAN) {
			actions.setAllow(1);
			actions.setMirror(FaucetOFControllerImpl.DEFAULT_MIRROR_PORT);
		} else if (ofFlow.getOfAction() == OFFlow.OFAction.NORMAL) {
			actions.setAllow(1);
		}

	}

	private String getValue(String value) {
		if (value == null || value.equals("*")) {
			return null;
		}
		return value;
	}

	private Integer getIntegerValue(String value) {
		if (value == null || value.equals("*")) {
			return null;
		}
		return Integer.parseInt(value);
	}


}
