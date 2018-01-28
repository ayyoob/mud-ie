package com.networkseer.user.mgt.dto;

import java.sql.Timestamp;
import java.util.List;

public class User {
	private int userId;
	private String username;
	private String password;
	private Timestamp timestamp;
	private List<UserAttribute> attributes;

	public String getUsername() {
		return username;
	}

	public int getUserId() {
		return userId;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public List<UserAttribute> getAttributes() {
		return attributes;
	}

	public void setAttributes(List<UserAttribute> attributes) {
		this.attributes = attributes;
	}

	public Timestamp getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Timestamp timestamp) {
		this.timestamp = timestamp;
	}
}
