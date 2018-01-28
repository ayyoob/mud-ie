package com.networkseer.user.mgt.exception;

public class UserManagementException extends Exception {

	public UserManagementException(Exception e) {
		super(e);
	}

	public UserManagementException(String msg, Exception e) {
		super(msg, e);
	}

}
