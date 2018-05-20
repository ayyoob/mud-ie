package com.networkseer.mud.collector.internal;

import com.networkseer.common.config.MUDController;
import com.networkseer.common.config.MudConfig;
import com.networkseer.mud.collector.MudieStatsCollector;
import com.networkseer.sdn.controller.mgt.OFController;
import com.networkseer.seer.mgt.service.SeerMgtService;

import java.util.Map;

public class MUDCollectorDataHolder {
	private static SeerMgtService seerMgtService;
	private static OFController ofController;
	private static Map<String, String> mudDevices;
	private static MudConfig mudConfig;
	private static MudieStatsCollector mudieStatsCollector;

	public static SeerMgtService getSeerMgtService() {
		return seerMgtService;
	}

	public static void setSeerMgtService(SeerMgtService seerMgtService) {
		MUDCollectorDataHolder.seerMgtService = seerMgtService;
	}

	public static OFController getOfController() {
		return ofController;
	}

	public static void setOfController(OFController ofController) {
		MUDCollectorDataHolder.ofController = ofController;
	}


	public static String getMUDControllerValue(String urnId) {
		for (MUDController mudController : mudConfig.getMudControllers()) {
			if (mudController.getId().equals(urnId)) {
				return mudController.getValue();
			}
		}
		return null;
	}

	public static Map<String, String> getMudDevices() {
		return mudDevices;
	}

	public static void setMudDevices(Map<String, String> mudDevices) {
		MUDCollectorDataHolder.mudDevices = mudDevices;
	}

	public static MudConfig getMudConfig() {
		return mudConfig;
	}

	public static void setMudConfig(MudConfig mudConfig) {
		MUDCollectorDataHolder.mudConfig = mudConfig;
	}

	public static MudieStatsCollector getMudieStatsCollector() {
		return mudieStatsCollector;
	}

	public static void setMudieStatsCollector(MudieStatsCollector mudieStatsCollector) {
		MUDCollectorDataHolder.mudieStatsCollector = mudieStatsCollector;
	}
}
