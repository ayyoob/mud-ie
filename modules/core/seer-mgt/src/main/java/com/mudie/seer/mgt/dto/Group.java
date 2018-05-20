package com.mudie.seer.mgt.dto;

public class Group {

	private int id;
	private String groupName;
	private int switchId;
	private Long dateOfCreation;
	private Long dateOfLastUpdate;
	private boolean quotoAppEnabled;
	private boolean parentalAppEnabled;
	private boolean securityAppEnabled;
	private long quota;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getGroupName() {
		return groupName;
	}

	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}

	public int getSwitchId() {
		return switchId;
	}

	public void setSwitchId(int switchId) {
		this.switchId = switchId;
	}

	public Long getDateOfCreation() {
		return dateOfCreation;
	}

	public void setDateOfCreation(Long dateOfCreation) {
		this.dateOfCreation = dateOfCreation;
	}

	public Long getDateOfLastUpdate() {
		return dateOfLastUpdate;
	}

	public void setDateOfLastUpdate(Long dateOfLastUpdate) {
		this.dateOfLastUpdate = dateOfLastUpdate;
	}

	public long getQuota() {
		return quota;
	}

	public void setQuota(long quota) {
		this.quota = quota;
	}

	public boolean isQuotoAppEnabled() {
		return quotoAppEnabled;
	}

	public void setQuotoAppEnabled(boolean quotoAppEnabled) {
		this.quotoAppEnabled = quotoAppEnabled;
	}

	public boolean isParentalAppEnabled() {
		return parentalAppEnabled;
	}

	public void setParentalAppEnabled(boolean parentalAppEnabled) {
		this.parentalAppEnabled = parentalAppEnabled;
	}

	public boolean isSecurityAppEnabled() {
		return securityAppEnabled;
	}

	public void setSecurityAppEnabled(boolean securityAppEnabled) {
		this.securityAppEnabled = securityAppEnabled;
	}
}
