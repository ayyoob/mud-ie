package com.mudie.sdn.controller.mgt.internal;

import com.mudie.common.config.Controller;
import com.mudie.sdn.controller.mgt.impl.NatsClient;
import com.mudie.sdn.controller.mgt.impl.faucet.L2LearnWrapper;

import java.util.HashMap;
import java.util.Map;

public class SdnControllerDataHolder {
	private static Controller controller;
	private static NatsClient natsClient;
	private static Map<String, L2LearnWrapper> l2LearnWrapperMap = new HashMap<>();

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

	public static Map<String, L2LearnWrapper> getL2LearnWrapperMap() {
		return l2LearnWrapperMap;
	}

	public static void setL2LearnWrapperMap(Map<String, L2LearnWrapper> l2LearnWrapperMap) {
		SdnControllerDataHolder.l2LearnWrapperMap = l2LearnWrapperMap;
	}
}
