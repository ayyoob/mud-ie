package com.networkseer.common.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.validator.constraints.NotEmpty;

import java.util.List;

public class Controller {

	@NotEmpty
	@JsonProperty
	private String hostname;

	@NotEmpty
	@JsonProperty
	private int port;

	@NotEmpty
	@JsonProperty
	private String type;

	public String getHostname() {
		return hostname;
	}

	public void setHostname(String hostname) {
		this.hostname = hostname;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
}
