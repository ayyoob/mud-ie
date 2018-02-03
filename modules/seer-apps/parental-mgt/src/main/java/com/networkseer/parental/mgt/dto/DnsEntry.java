package com.networkseer.parental.mgt.dto;

public class DnsEntry {

	private String dnsDomain;
	private String dpId;
	private String deviceMac;
	private Long timestamp;
	private String tags;

	public String getDnsDomain() {
		return dnsDomain;
	}

	public void setDnsDomain(String dnsDomain) {
		this.dnsDomain = dnsDomain;
	}

	public String getDpId() {
		return dpId;
	}

	public void setDpId(String dpId) {
		this.dpId = dpId;
	}

	public String getDeviceMac() {
		return deviceMac;
	}

	public void setDeviceMac(String deviceMac) {
		this.deviceMac = deviceMac;
	}

	public Long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Long timestamp) {
		this.timestamp = timestamp;
	}

	public String getTags() {
		return tags;
	}

	public void setTags(String tags) {
		this.tags = tags;
	}
}
