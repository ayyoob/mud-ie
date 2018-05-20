package com.mudie.sdn.controller.mgt;

public class HostInfo {

	private String dpId;
	private long portNo;
	private int vlanId;

	public String getDpId() {
		return dpId;
	}

	public void setDpId(String dpId) {
		this.dpId = dpId;
	}

	public long getPortNo() {
		return portNo;
	}

	public void setPortNo(long portNo) {
		this.portNo = portNo;
	}

	public int getVlanId() {
		return vlanId;
	}

	public void setVlanId(int vlanId) {
		this.vlanId = vlanId;
	}
}
