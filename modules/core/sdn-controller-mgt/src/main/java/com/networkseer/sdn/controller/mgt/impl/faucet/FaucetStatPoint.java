package com.networkseer.sdn.controller.mgt.impl.faucet;

import org.influxdb.annotation.Column;
import org.influxdb.annotation.Measurement;

@Measurement(name = "memory")
public class FaucetStatPoint {

	@Column(name = "eth_src")
	private String ethSrc;

	@Column(name = "eth_dst")
	private String ethDst;

	@Column(name = "eth_type")
	private Integer ethType;

	@Column(name = "ip_proto")
	private Integer ipProto;

	@Column(name = "ipv4_src")
	private String ipv4Src;

	@Column(name = "ipv4_dst")
	private String ipv4Dst;

	@Column(name = "ipv6_src")
	private String ipv6Src;

	@Column(name = "ipv6_dst")
	private String ipv6Dst;

	@Column(name = "udp_src")
	private Integer udpSrc;

	@Column(name = "udp_dst")
	private Integer udpDst;

	@Column(name = "tcp_src")
	private Integer tcpSrc;

	@Column(name = "tcp_dst")
	private Integer tcpDst;

	@Column(name = "value")
	private Long value;




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
}
