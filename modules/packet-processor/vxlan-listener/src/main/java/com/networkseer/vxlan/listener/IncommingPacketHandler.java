package com.networkseer.vxlan.listener;

import com.networkseer.vxlan.listener.internal.VxLanListenerDataHolder;
import com.networkseer.common.packet.PacketConstants;
import com.networkseer.common.packet.PacketListener;
import com.networkseer.common.packet.SeerPacket;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;
import org.pcap4j.packet.EthernetPacket;
import org.pcap4j.packet.IpV4Packet;
import org.pcap4j.packet.TcpPacket;
import org.pcap4j.packet.UdpPacket;
import org.pcap4j.packet.namednumber.EtherType;
import org.pcap4j.packet.namednumber.IpNumber;
import org.pcap4j.util.ByteArrays;

import java.nio.ByteBuffer;

public class IncommingPacketHandler extends SimpleChannelInboundHandler<DatagramPacket> {
	@Override
	protected void channelRead0(ChannelHandlerContext channelHandlerContext, DatagramPacket packet) throws Exception {
		ByteBuf byteBuf = packet.content();
		byte[] bytes = ByteBufUtil.getBytes(byteBuf);
		EthernetPacket ethernetPacket = EthernetPacket.newPacket(bytes, 8, bytes.length-8);
		EthernetPacket.EthernetHeader ethernetHeader = ethernetPacket.getHeader();
		SeerPacket seerPacket = new SeerPacket();
		byte vlanId[] = {bytes[4],bytes[5], bytes[6]};
		seerPacket.setVxlanId(ByteArrays.toHexString(vlanId, ""));
		seerPacket.setSrcMac(ethernetHeader.getSrcAddr().toString());
		seerPacket.setDstMac(ethernetHeader.getDstAddr().toString());
		seerPacket.setEthType(ethernetPacket.getHeader().getType().toString());

		if (EtherType.IPV4 == (ethernetHeader.getType())) {
			IpV4Packet ipV4Packet = (IpV4Packet) ethernetPacket.getPayload();
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
		}
		for (PacketListener packetListener : VxLanListenerDataHolder.getPacketListeners()) {
			packetListener.processPacket(seerPacket);
		}
	}

	private boolean isBroadcast(long rawValue) {
		return rawValue == PacketConstants.BROADCAST_DECIMAL;
	}

	public boolean isMulticast(long rawValue) {
		return (rawValue & PacketConstants.MULTICAST_MULTIPLIER) != 0L;
	}

	public long bytesToLong(byte[] bytes) {
		ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
		buffer.put(bytes);
		buffer.flip();//need flip
		return buffer.getLong();
	}
}
