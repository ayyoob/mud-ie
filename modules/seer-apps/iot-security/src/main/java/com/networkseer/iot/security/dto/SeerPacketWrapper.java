package com.networkseer.iot.security.dto;

import com.networkseer.common.packet.SeerPacket;

public class SeerPacketWrapper {

	private SeerPacket seerPacket;
	private String deviceMac;
	private String vlanId;
	public SeerPacketWrapper(SeerPacket seerPacket, String deviceMac, String vlanId) {
		this.seerPacket = seerPacket;
		this.deviceMac = deviceMac;
		this.vlanId = vlanId;
	}

	public SeerPacket getSeerPacket() {
		return seerPacket;
	}

	public void setSeerPacket(SeerPacket seerPacket) {
		this.seerPacket = seerPacket;
	}

	public String getDeviceMac() {
		return deviceMac;
	}

	public void setDeviceMac(String deviceMac) {
		this.deviceMac = deviceMac;
	}

	public String getVlanId() {
		return vlanId;
	}

	public void setVlanId(String vlanId) {
		this.vlanId = vlanId;
	}
}
