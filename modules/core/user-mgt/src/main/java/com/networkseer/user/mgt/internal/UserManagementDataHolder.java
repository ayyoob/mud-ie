package com.networkseer.user.mgt.internal;

import com.networkseer.user.mgt.service.UserStoreService;

public class UserManagementDataHolder {
	private static UserStoreService userStoreService;

	public static UserStoreService getUserStoreService() {
		return userStoreService;
	}

	public static void setUserStoreService(UserStoreService userStoreService) {
		UserManagementDataHolder.userStoreService = userStoreService;
	}
}
