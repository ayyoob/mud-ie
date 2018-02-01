package com.networkseer.vxlan.listener.internal;

import com.networkseer.common.packet.PacketListener;

import java.util.List;

public class VxLanListenerDataHolder {

	private static List<PacketListener> packetListeners;

	public static List<PacketListener> getPacketListeners() {
		return packetListeners;
	}

	public static void setPacketListeners(List<PacketListener> packetListeners) {
		VxLanListenerDataHolder.packetListeners = packetListeners;
	}
}
