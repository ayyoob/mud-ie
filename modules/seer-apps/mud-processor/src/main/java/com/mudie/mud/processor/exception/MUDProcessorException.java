package com.mudie.mud.processor.exception;

public class MUDProcessorException extends Exception {

	public MUDProcessorException(Exception e) {
		super(e);
	}

	public MUDProcessorException(String msg, Exception e) {
		super(msg, e);
	}

}
