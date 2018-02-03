package com.networkseer.parental.mgt.dao.exception;

public class TransactionManagementException extends Exception {

	private static final long serialVersionUID = -3151279321929070297L;

	public TransactionManagementException(String msg, Exception nestedEx) {
		super(msg, nestedEx);
	}

	public TransactionManagementException(String message, Throwable cause) {
		super(message, cause);
	}

	public TransactionManagementException(String msg) {
		super(msg);
	}

	public TransactionManagementException() {
		super();
	}

	public TransactionManagementException(Throwable cause) {
		super(cause);
	}

}
