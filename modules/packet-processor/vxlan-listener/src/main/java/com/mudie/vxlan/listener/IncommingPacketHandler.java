package com.mudie.vxlan.listener;

import com.mudie.vxlan.listener.internal.VxLanListenerDataHolder;
import com.mudie.common.packet.PacketListener;
import com.mudie.common.packet.SeerPacket;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;
import org.pcap4j.packet.*;
import org.pcap4j.packet.namednumber.EtherType;
import org.pcap4j.packet.namednumber.IpNumber;
import org.pcap4j.util.ByteArrays;

public class IncommingPacketHandler extends SimpleChannelInboundHandler<DatagramPacket> {
	@Override
	protected void channelRead0(ChannelHandlerContext channelHandlerContext, DatagramPacket packet) throws Exception {
		ByteBuf byteBuf = packet.content();
		byte[] bytes = ByteBufUtil.getBytes(byteBuf);
		int size = bytes.length-8;
		EthernetPacket ethernetPacket = EthernetPacket.newPacket(bytes, 8, size);
		EthernetPacket.EthernetHeader ethernetHeader = ethernetPacket.getHeader();
		EtherType packetEtherType = ethernetHeader.getType();
		Packet payload = ethernetPacket.getPayload();
		if (packetEtherType == EtherType.DOT1Q_VLAN_TAGGED_FRAMES) {
			Dot1qVlanTagPacket dot1qVlanTagPacket = (Dot1qVlanTagPacket)ethernetPacket.getPayload();
			packetEtherType = dot1qVlanTagPacket.getHeader().getType();
			payload = dot1qVlanTagPacket.getPayload();
			size = size - 4;
		}
		SeerPacket seerPacket = new SeerPacket();
		byte vlanId[] = {bytes[4],bytes[5], bytes[6]};
		seerPacket.setVxlanId(ByteArrays.toHexString(vlanId, ""));
		seerPacket.setSrcMac(ethernetHeader.getSrcAddr().toString());
		seerPacket.setDstMac(ethernetHeader.getDstAddr().toString());
		seerPacket.setEthType(packetEtherType.valueAsString());
		seerPacket.setSize(size);

		if (EtherType.IPV4 == packetEtherType) {
			IpV4Packet ipV4Packet = (IpV4Packet) payload;
			IpV4Packet.IpV4Header ipV4Header = ipV4Packet.getHeader();
			seerPacket.setSrcIp(ipV4Header.getSrcAddr().getHostAddress());
			seerPacket.setDstIp(ipV4Header.getDstAddr().getHostAddress());
			seerPacket.setIpProto(ipV4Header.getProtocol().valueAsString());
			if (ipV4Header.getProtocol().valueAsString().equals(IpNumber.TCP.valueAsString()) ) {
				TcpPacket tcpPacket = (TcpPacket) ipV4Packet.getPayload();
				seerPacket.setSrcPort(tcpPacket.getHeader().getSrcPort().valueAsString());
				seerPacket.setDstPort(tcpPacket.getHeader().getDstPort().valueAsString());
				seerPacket.setTcpFlag(tcpPacket.getHeader().getSyn(),tcpPacket.getHeader().getAck());
				seerPacket.setPayload(tcpPacket.getPayload().getRawData());
			} else if (ipV4Header.getProtocol().valueAsString().equals(IpNumber.UDP.valueAsString()) ) {
				UdpPacket udpPacket = (UdpPacket) ipV4Packet.getPayload();
				seerPacket.setSrcPort(udpPacket.getHeader().getSrcPort().valueAsString());
				seerPacket.setDstPort(udpPacket.getHeader().getDstPort().valueAsString());
				seerPacket.setPayload(udpPacket.getPayload().getRawData());
			}
		} else if (EtherType.IPV6 == packetEtherType) {
			IpV6Packet ipV6Packet = (IpV6Packet) payload;
			IpV6Packet.IpV6Header ipV6Header = ipV6Packet.getHeader();
			seerPacket.setSrcIp(ipV6Header.getSrcAddr().getHostAddress());
			seerPacket.setDstIp(ipV6Header.getDstAddr().getHostAddress());
			seerPacket.setIpProto(ipV6Header.getProtocol().valueAsString());
			if (ipV6Header.getProtocol().valueAsString().equals(IpNumber.TCP.valueAsString()) ) {
				TcpPacket tcpPacket = (TcpPacket) ipV6Packet.getPayload();
				seerPacket.setSrcPort(tcpPacket.getHeader().getSrcPort().valueAsString());
				seerPacket.setDstPort(tcpPacket.getHeader().getDstPort().valueAsString());
				seerPacket.setTcpFlag(tcpPacket.getHeader().getSyn(),tcpPacket.getHeader().getAck());
				seerPacket.setPayload(tcpPacket.getPayload().getRawData());
			} else if (ipV6Header.getProtocol().valueAsString().equals(IpNumber.UDP.valueAsString()) ) {
				UdpPacket udpPacket = (UdpPacket) ipV6Packet.getPayload();
				seerPacket.setSrcPort(udpPacket.getHeader().getSrcPort().valueAsString());
				seerPacket.setDstPort(udpPacket.getHeader().getDstPort().valueAsString());
				seerPacket.setPayload(udpPacket.getPayload().getRawData());
			}
		} else {
			seerPacket.setPayload(payload.getRawData());
		}
		for (PacketListener packetListener : VxLanListenerDataHolder.getPacketListeners()) {
			packetListener.processPacket(seerPacket);
		}
	}
}
