package com.mudie.seer.mgt.dto;

import java.sql.Timestamp;
import java.util.Date;

public class Switch {

	private int id;
	private String dpId;
	private Long dateOfCreation;
	private Long dateOfLastUpdate;
	private String owner;
	private long quota;
	private int billingDay;
	private Status status;
	private Timestamp billingDate;

	public enum Status {
		ACTIVE,
		BLOCK
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getDpId() {
		return dpId;
	}

	public void setDpId(String dpId) {
		this.dpId = dpId;
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

	public String getOwner() {
		return owner;
	}

	public void setOwner(String owner) {
		this.owner = owner;
	}

	public long getQuota() {
		return quota;
	}

	public void setQuota(long quota) {
		this.quota = quota;
	}

	public int getBillingDay() {
		return billingDay;
	}

	public void setBillingDay(int billingDay) {
		this.billingDay = billingDay;
	}

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	public Timestamp getBillingDate() {
		return billingDate;
	}

	public void setBillingDate(Timestamp billingDate) {
		this.billingDate = billingDate;
	}
}
