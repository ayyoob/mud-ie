package com.networkseer.device.discovery.internal;

import com.networkseer.sdn.controller.mgt.OFController;
import com.networkseer.seer.mgt.service.SeerMgtService;

public class DeviceDiscoveryDataHolder {
	private static SeerMgtService seerMgtService;
	private static OFController ofController;

	public static SeerMgtService getSeerMgtService() {
		return seerMgtService;
	}

	public static void setSeerMgtService(SeerMgtService seerMgtService) {
		DeviceDiscoveryDataHolder.seerMgtService = seerMgtService;
	}

	public static OFController getOfController() {
		return ofController;
	}

	public static void setOfController(OFController ofController) {
		DeviceDiscoveryDataHolder.ofController = ofController;
	}
}
