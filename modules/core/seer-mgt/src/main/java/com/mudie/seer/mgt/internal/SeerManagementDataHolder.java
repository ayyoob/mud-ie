package com.mudie.seer.mgt.internal;

import com.mudie.seer.mgt.service.SeerMgtService;

public class SeerManagementDataHolder {
	private static SeerMgtService seerMgtService;

	public static SeerMgtService getSeerMgtService() {
		return seerMgtService;
	}

	public static void setSeerMgtService(SeerMgtService seerMgtService) {
		SeerManagementDataHolder.seerMgtService = seerMgtService;
	}
}
