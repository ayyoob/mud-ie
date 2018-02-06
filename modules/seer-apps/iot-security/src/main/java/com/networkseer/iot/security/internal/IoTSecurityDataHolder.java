package com.networkseer.iot.security.internal;

import com.google.common.cache.LoadingCache;
import com.networkseer.common.openflow.OFFlow;
import com.networkseer.iot.security.dto.DeviceIdentifier;
import com.networkseer.sdn.controller.mgt.OFController;
import com.networkseer.seer.mgt.service.SeerMgtService;

import java.util.List;

public class IoTSecurityDataHolder {
	private static SeerMgtService seerMgtService;
	private static LoadingCache<DeviceIdentifier, List<OFFlow>> statsCache;
	private static OFController ofController;

	public static SeerMgtService getSeerMgtService() {
		return seerMgtService;
	}

	public static void setSeerMgtService(SeerMgtService seerMgtService) {
		IoTSecurityDataHolder.seerMgtService = seerMgtService;
	}

	public static LoadingCache<DeviceIdentifier, List<OFFlow>> getStatsCache() {
		return statsCache;
	}

	public static void setStatsCache(LoadingCache<DeviceIdentifier, List<OFFlow>> statsCache) {
		IoTSecurityDataHolder.statsCache = statsCache;
	}

	public static OFController getOfController() {
		return ofController;
	}

	public static void setOfController(OFController ofController) {
		IoTSecurityDataHolder.ofController = ofController;
	}
}
