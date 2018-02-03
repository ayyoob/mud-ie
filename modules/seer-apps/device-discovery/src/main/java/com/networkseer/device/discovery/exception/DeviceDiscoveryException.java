package com.networkseer.device.discovery.exception;

public class DeviceDiscoveryException extends Exception {

	public DeviceDiscoveryException(Exception e) {
		super(e);
	}

	public DeviceDiscoveryException(String msg, Exception e) {
		super(msg, e);
	}

}
