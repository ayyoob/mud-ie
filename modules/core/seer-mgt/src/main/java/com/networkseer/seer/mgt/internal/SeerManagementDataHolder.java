package com.networkseer.seer.mgt.internal;

import com.networkseer.seer.mgt.service.UserStoreService;

public class SeerManagementDataHolder {
	private static UserStoreService userStoreService;

	public static UserStoreService getUserStoreService() {
		return userStoreService;
	}

	public static void setUserStoreService(UserStoreService userStoreService) {
		SeerManagementDataHolder.userStoreService = userStoreService;
	}
}
