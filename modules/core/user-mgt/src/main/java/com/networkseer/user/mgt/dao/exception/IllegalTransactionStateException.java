package com.networkseer.user.mgt.dao.exception;

public class IllegalTransactionStateException extends RuntimeException {

	private static final long serialVersionUID = -3151279331929070297L;

	public IllegalTransactionStateException(String msg, Exception nestedEx) {
		super(msg, nestedEx);
	}

	public IllegalTransactionStateException(String message, Throwable cause) {
		super(message, cause);
	}

	public IllegalTransactionStateException(String msg) {
		super(msg);
	}

	public IllegalTransactionStateException() {
		super();
	}

	public IllegalTransactionStateException(Throwable cause) {
		super(cause);
	}

}
