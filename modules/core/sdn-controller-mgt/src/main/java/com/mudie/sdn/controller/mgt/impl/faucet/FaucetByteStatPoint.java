package com.mudie.sdn.controller.mgt.impl.faucet;

import com.mudie.common.openflow.OFFlow;
import com.mudie.common.packet.PacketConstants;
import org.influxdb.annotation.Column;
import org.influxdb.annotation.Measurement;

@Measurement(name = "flow_byte_count")
public class FaucetByteStatPoint {

	@Column(name = "eth_src")
	private String ethSrc = "*";

	@Column(name = "eth_dst")
	private String ethDst  = "*";

	@Column(name = "eth_type")
	private String ethType = "*";

	@Column(name = "ip_proto")
	private String ipProto = "*";

	@Column(name = "ipv4_src")
	private String ipv4Src = "*";

	@Column(name = "ipv4_dst")
	private String ipv4Dst = "*";

	@Column(name = "ipv6_src")
	private String ipv6Src = "*";

	@Column(name = "ipv6_dst")
	private String ipv6Dst = "*";

	@Column(name = "udp_src")
	private String udpSrc = "*";

	@Column(name = "udp_dst")
	private String udpDst = "*";

	@Column(name = "tcp_src")
	private String tcpSrc = "*";

	@Column(name = "tcp_dst")
	private String tcpDst = "*";

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

	public String getEthType() {
		return ethType;
	}

	public void setEthType(String ethType) {
		this.ethType = ethType;
	}

	public String getIpProto() {
		return ipProto;
	}

	public void setIpProto(String ipProto) {
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

	public String getUdpSrc() {
		return udpSrc;
	}

	public void setUdpSrc(String udpSrc) {
		this.udpSrc = udpSrc;
	}

	public String getUdpDst() {
		return udpDst;
	}

	public void setUdpDst(String udpDst) {
		this.udpDst = udpDst;
	}

	public String getTcpSrc() {
		return tcpSrc;
	}

	public void setTcpSrc(String tcpSrc) {
		this.tcpSrc = tcpSrc;
	}

	public String getTcpDst() {
		return tcpDst;
	}

	public void setTcpDst(String tcpDst) {
		this.tcpDst = tcpDst;
	}

	public Long getValue() {
		return value;
	}

	public void setValue(Long value) {
		this.value = value;
	}

	public OFFlow getOFFlow() {
		OFFlow ofFlow = new OFFlow();
		ofFlow.setSrcMac(ethSrc);
		ofFlow.setDstMac(ethDst);
		String hex = "0x" + Integer.toHexString(0x10000 |Integer.parseInt(ethType)).substring(1);
		ofFlow.setEthType(hex);
		ofFlow.setIpProto(ipProto);
		if (ethType.equals("" + Integer.decode(PacketConstants.ETH_TYPE_IPV4))) {
			ofFlow.setSrcIp(ipv4Src);
			ofFlow.setDstIp(ipv4Dst);
		} else if (ethType != null && ethType.equals("" + Integer.decode(PacketConstants.ETH_TYPE_IPV6))) {
			ofFlow.setSrcIp(ipv6Src);
			ofFlow.setDstIp(ipv6Dst);
		}

		if (ipProto.equals("" + getIntegerValue(PacketConstants.TCP_PROTO))) {
			ofFlow.setSrcPort(tcpSrc);
			ofFlow.setDstPort(tcpDst);
		} else if (ipProto.equals("" + getIntegerValue(PacketConstants.UDP_PROTO))) {
			ofFlow.setSrcPort(udpSrc);
			ofFlow.setDstPort(udpDst);
		}
		ofFlow.setByteCount(value);
		return ofFlow;

	}

	private Integer getIntegerValue(String value) {
		if (value == null || value.equals("*")) {
			return null;
		}
		return Integer.parseInt(value);
	}
}
