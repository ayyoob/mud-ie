package com.networkseer.common.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.validator.constraints.NotEmpty;
import java.util.List;

public class UserMgt {

	@NotEmpty
	@JsonProperty
	private String serverAdminUserName;

	@NotEmpty
	@JsonProperty
	private String serverAdminPassword;

	@NotEmpty
	@JsonProperty
	private List<String> serverRoles;

	public String getServerAdminUserName() {
		return serverAdminUserName;
	}

	public void setServerAdminUserName(String serverAdminUserName) {
		this.serverAdminUserName = serverAdminUserName;
	}

	public String getServerAdminPassword() {
		return serverAdminPassword;
	}

	public void setServerAdminPassword(String serverAdminPassword) {
		this.serverAdminPassword = serverAdminPassword;
	}

	public List<String> getServerRoles() {
		return serverRoles;
	}

	public void setServerRoles(List<String> serverRoles) {
		this.serverRoles = serverRoles;
	}
}
