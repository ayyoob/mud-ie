package com.mudie.seer.mgt.dto;

public class DeviceRecord {
	private Device device;
	private Switch aSwitch;
	private Group group;

	public Device getDevice() {
		return device;
	}

	public void setDevice(Device device) {
		this.device = device;
	}

	public Switch getaSwitch() {
		return aSwitch;
	}

	public void setaSwitch(Switch aSwitch) {
		this.aSwitch = aSwitch;
	}

	public Group getGroup() {
		return group;
	}

	public void setGroup(Group group) {
		this.group = group;
	}
}
