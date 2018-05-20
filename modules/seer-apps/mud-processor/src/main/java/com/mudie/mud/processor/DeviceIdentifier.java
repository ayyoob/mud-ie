package com.mudie.mud.processor;

public class DeviceIdentifier {

	private String vxlanId;
	private String deviceMac;
	private String mudUrl;
	private String name;

	public DeviceIdentifier(String vlanId, String deviceMac) {
		this.vxlanId = vlanId;
		this.deviceMac = deviceMac;
	}

	public String getMudUrl() {
		return mudUrl;
	}

	public void setMudUrl(String mudUrl) {
		this.mudUrl = mudUrl;
	}

	public String getVxlanId() {
		return vxlanId;
	}

	public void setVxlanId(String vxlanId) {
		this.vxlanId = vxlanId;
	}

	public String getDeviceMac() {
		return deviceMac;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setDeviceMac(String deviceMac) {
		this.deviceMac = deviceMac;
	}

	@Override
	public int hashCode() {
		int result = this.vxlanId.hashCode();
		result = 31 * result + ("@" + this.deviceMac).hashCode();
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		return (obj instanceof DeviceIdentifier) && vxlanId.equals(
				((DeviceIdentifier) obj).vxlanId) && deviceMac.equals(
				((DeviceIdentifier) obj).deviceMac);
	}
}
