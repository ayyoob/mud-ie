package com.networkseer.parental.mgt.exception;

public class ParentalManagementException extends Exception {

	public ParentalManagementException(Exception e) {
		super(e);
	}

	public ParentalManagementException(String msg, Exception e) {
		super(msg, e);
	}

}
