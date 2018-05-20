package com.mudie.sdn.controller.mgt.exception;

public class OFControllerException extends Exception {

	public OFControllerException(Exception e) {
		super(e);
	}

	public OFControllerException(String msg) {
		super(msg);
	}


	public OFControllerException(String msg, Exception e) {
		super(msg, e);
	}

}
