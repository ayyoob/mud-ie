package com.networkseer.iot.security.exception;

public class IoTSecurityException extends Exception {

	public IoTSecurityException(Exception e) {
		super(e);
	}

	public IoTSecurityException(String msg, Exception e) {
		super(msg, e);
	}

}
