package com.mudie.common;

import java.sql.Connection;

public class PriviledgedSeerContext {

	private static ThreadLocal<String> currentUser = new ThreadLocal<String>();

	public static String getUserName() {
		String user = currentUser.get();
		if (user == null || user.isEmpty()) {
			throw new RuntimeException("User context is not set");
		}
		return currentUser.get();
	}

	public static void setCurrentUser(String user) {
		currentUser.set(user);
	}

}
