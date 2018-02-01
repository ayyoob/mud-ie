package com.networkseer.sdn.controller.mgt.impl.floodlight;

public class PortDesc {

	private String portNumber;
	private String hardwareAddress;
	private String name;
	private String config;
	private String state;
	private String currentFeatures;
	private String advertisedFeatures;
	private String supportedFeatures;
	private String peerFeatures;
	private String currSpeed;
	private String maxSpeed;

	public String getPortNumber() {
		return portNumber;
	}

	public void setPortNumber(String portNumber) {
		this.portNumber = portNumber;
	}

	public String getHardwareAddress() {
		return hardwareAddress;
	}

	public void setHardwareAddress(String hardwareAddress) {
		this.hardwareAddress = hardwareAddress;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getConfig() {
		return config;
	}

	public void setConfig(String config) {
		this.config = config;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public String getCurrentFeatures() {
		return currentFeatures;
	}

	public void setCurrentFeatures(String currentFeatures) {
		this.currentFeatures = currentFeatures;
	}

	public String getAdvertisedFeatures() {
		return advertisedFeatures;
	}

	public void setAdvertisedFeatures(String advertisedFeatures) {
		this.advertisedFeatures = advertisedFeatures;
	}

	public String getSupportedFeatures() {
		return supportedFeatures;
	}

	public void setSupportedFeatures(String supportedFeatures) {
		this.supportedFeatures = supportedFeatures;
	}

	public String getPeerFeatures() {
		return peerFeatures;
	}

	public void setPeerFeatures(String peerFeatures) {
		this.peerFeatures = peerFeatures;
	}

	public String getCurrSpeed() {
		return currSpeed;
	}

	public void setCurrSpeed(String currSpeed) {
		this.currSpeed = currSpeed;
	}

	public String getMaxSpeed() {
		return maxSpeed;
	}

	public void setMaxSpeed(String maxSpeed) {
		this.maxSpeed = maxSpeed;
	}
}
