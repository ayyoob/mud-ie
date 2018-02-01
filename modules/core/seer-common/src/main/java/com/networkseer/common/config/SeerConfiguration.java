package com.networkseer.common.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;

import javax.validation.constraints.NotNull;

public class SeerConfiguration extends Configuration {
	@NotNull
	private UserMgt userMgt;
	private Controller controller;
	private String swagger;


	@JsonProperty
	public UserMgt getUserMgt() {
		return userMgt;
	}

	@JsonProperty
	public void setUserMgt(UserMgt userMgt) {
		this.userMgt = userMgt;
	}

	public Controller getController() {
		return controller;
	}

	public void setController(Controller controller) {
		this.controller = controller;
	}

	public String getSwagger() {
		return swagger;
	}

	public void setSwagger(String swagger) {
		this.swagger = swagger;
	}
}
