package com.networkseer.vxlan.listener;

import com.networkseer.common.packet.PacketConstants;
import com.networkseer.common.packet.PacketListener;
import com.networkseer.common.packet.SeerPacket;
import org.pcap4j.packet.DnsPacket;
import org.pcap4j.packet.DnsQuestion;
import org.pcap4j.packet.IllegalRawDataException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class DNSPacketListenerImpl implements PacketListener {
	private static final Logger log = LoggerFactory.getLogger(DNSPacketListenerImpl.class);

	private static final String DPID_PREFIX = "00:00:";
	@Override
	public void processPacket(SeerPacket seerPacket) {
		if (seerPacket.getIpProto()!= null && seerPacket.getIpProto().equals(PacketConstants.UDP_PROTO)
				&& seerPacket.getDstPort().equals(PacketConstants.DNS_PORT) ) {
//			process DNS packet
			try {
				DnsPacket dnsPacket = DnsPacket.newPacket(seerPacket.getPayload(), 0, seerPacket.getPayload().length);
				List<DnsQuestion> dnsQuestions = dnsPacket.getHeader().getQuestions();
				if (dnsQuestions.size() > 0) {
					String dnsName = dnsQuestions.get(0).getQName().getName();
					String deviceMac = seerPacket.getSrcMac();
					String dpId = DPID_PREFIX + seerPacket.getDstMac();
				}
			} catch (IllegalRawDataException e) {
				e.printStackTrace();
			}
		}
	}
}
