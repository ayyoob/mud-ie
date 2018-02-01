package com.networkseer.sdn.controller.mgt.impl.floodlight;

import java.util.List;

public class PortInfo {

	private String version;
	private List<PortDesc> portDesc;

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public List<PortDesc> getPortDesc() {
		return portDesc;
	}

	public void setPortDesc(List<PortDesc> portDesc) {
		this.portDesc = portDesc;
	}
}
