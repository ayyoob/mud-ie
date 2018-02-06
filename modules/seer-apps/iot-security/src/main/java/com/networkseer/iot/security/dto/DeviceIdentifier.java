package com.networkseer.iot.security.dto;

public class DeviceIdentifier {
	private String vlanId;
	private String deviceMac;

	public DeviceIdentifier(String dpId, String deviceMac) {
		this.vlanId = dpId.substring(15);
		this.deviceMac = deviceMac;
	}

	public DeviceIdentifier(String vlanId, String deviceMac, boolean vlan) {
		this.vlanId = vlanId;
		this.deviceMac = deviceMac;
	}

	public String getVlanId() {
		return vlanId;
	}

	public void setVlanId(String vlanId) {
		this.vlanId = vlanId;
	}

	public String getDeviceMac() {
		return deviceMac;
	}

	public void setDeviceMac(String deviceMac) {
		this.deviceMac = deviceMac;
	}

	@Override
	public int hashCode() {
		int result = this.vlanId.hashCode();
		result = 31 * result + ("@" + this.deviceMac).hashCode();
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		return (obj instanceof DeviceIdentifier) && vlanId.equals(
				((DeviceIdentifier) obj).vlanId) && deviceMac.equals(
				((DeviceIdentifier) obj).deviceMac);
	}
}
