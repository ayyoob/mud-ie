package com.mudie.seer.mgt.exception;

public class SeerManagementException extends Exception {

	public SeerManagementException(Exception e) {
		super(e);
	}

	public SeerManagementException(String msg, Exception e) {
		super(msg, e);
	}

}
