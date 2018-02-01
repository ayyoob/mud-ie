package com.networkseer.sdn.controller.mgt.internal;

import com.networkseer.sdn.controller.mgt.impl.floodlight.FloodlightAPI;

public class SdnControllerDataHolder {

	private static FloodlightAPI floodlightAPI;

	public static FloodlightAPI getFloodlightAPI() {
		return floodlightAPI;
	}

	public static void setFloodlightAPI(FloodlightAPI floodlightAPI) {
		SdnControllerDataHolder.floodlightAPI = floodlightAPI;
	}
}
