package com.networkseer.sdn.controller.mgt.internal;

import com.networkseer.common.config.Controller;
import com.networkseer.sdn.controller.mgt.impl.NatsClient;

public class SdnControllerDataHolder {
	private static Controller controller;
	private static NatsClient natsClient;

	public static Controller getController() {
		return controller;
	}

	public static void setController(Controller controller) {
		SdnControllerDataHolder.controller = controller;
	}

	public static NatsClient getNatsClient() {
		return natsClient;
	}

	public static void setNatsClient(NatsClient natsClient) {
		SdnControllerDataHolder.natsClient = natsClient;
	}
}
