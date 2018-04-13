package com.networkseer.mud.processor.internal;

import com.networkseer.common.config.MUDController;
import com.networkseer.sdn.controller.mgt.OFController;
import com.networkseer.seer.mgt.service.SeerMgtService;

import java.util.List;

public class MUDProcesserDataHolder {
	private static SeerMgtService seerMgtService;
	private static OFController ofController;
	private static List<MUDController> controllers;

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
}
