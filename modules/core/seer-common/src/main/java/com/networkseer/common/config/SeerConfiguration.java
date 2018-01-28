package com.networkseer.common.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;

import javax.validation.constraints.NotNull;

public class SeerConfiguration extends Configuration {
	@NotNull
	private UserMgt userMgt;


	@JsonProperty
	public UserMgt getUserMgt() {
		return userMgt;
	}

	@JsonProperty
	public void setUserMgt(UserMgt userMgt) {
		this.userMgt = userMgt;
	}
}
