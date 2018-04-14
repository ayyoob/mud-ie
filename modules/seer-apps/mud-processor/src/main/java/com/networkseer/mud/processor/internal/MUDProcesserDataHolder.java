package com.networkseer.mud.processor.internal;

import com.networkseer.common.config.MUDController;
import com.networkseer.sdn.controller.mgt.OFController;
import com.networkseer.seer.mgt.service.SeerMgtService;

import java.util.List;
import java.util.Map;

public class MUDProcesserDataHolder {
	private static SeerMgtService seerMgtService;
	private static OFController ofController;
	private static List<MUDController> controllers;
	private static Map<String, String> mudDevices;

	public static SeerMgtService getSeerMgtService() {
		return seerMgtService;
	}

	public static void setSeerMgtService(SeerMgtService seerMgtService) {
		MUDProcesserDataHolder.seerMgtService = seerMgtService;
	}

	public static OFController getOfController() {
		return ofController;
	}

	public static void setOfController(OFController ofController) {
		MUDProcesserDataHolder.ofController = ofController;
	}

	public List<MUDController> getControllers() {
		return controllers;
	}

	public void setControllers(List<MUDController> controllers) {
		this.controllers = controllers;
	}

	public static String getMUDControllerValue(String urnId) {
		for (MUDController mudController : controllers) {
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
		MUDProcesserDataHolder.mudDevices = mudDevices;
	}
}
