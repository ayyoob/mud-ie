package com.networkseer.mud.processor;

import com.networkseer.mud.processor.mud.MudSpec;

public class DeviceMudWrapper {

	private String mudProfile;
	private int vlan;
	private static String SEPERATOR = "&@&";

	public DeviceMudWrapper() {

	}

	public DeviceMudWrapper(String payload) {
		String[] data = payload.split(SEPERATOR);
		mudProfile = data[0];
		vlan = Integer.parseInt(data[1]);
	}

	public String getMudProfile() {
		return mudProfile;
	}

	public void setMudProfile(String mudProfile) {
		this.mudProfile = mudProfile;
	}

	public int getVlan() {
		return vlan;
	}

	public void setVlan(int vlan) {
		this.vlan = vlan;
	}

	@Override
	public String toString() {
		return mudProfile + "&@&" + vlan + "&@&";
	}
}
