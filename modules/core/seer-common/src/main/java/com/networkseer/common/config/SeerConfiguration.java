package com.networkseer.common.config;

import io.dropwizard.Configuration;

import java.util.List;

public class SeerConfiguration extends Configuration {

	private Controller controller;
	private String swagger;
	private List<String> switches;
	private MudConfig mudConfig;

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

	public List<String> getSwitches() {
		return switches;
	}

	public void setSwitches(List<String> switches) {
		this.switches = switches;
	}

	public MudConfig getMudConfig() {
		return mudConfig;
	}

	public void setMudConfig(MudConfig mudConfig) {
		this.mudConfig = mudConfig;
	}
}
