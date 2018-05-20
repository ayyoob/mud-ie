package com.networkseer.mud.collector.internal;

import com.networkseer.common.SeerPlugin;
import com.networkseer.common.config.SeerConfiguration;
import com.networkseer.mud.collector.MudieStatsCollector;
import com.networkseer.sdn.controller.mgt.OFController;
import com.networkseer.seer.mgt.service.SeerMgtService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class MUDCollectorSeerPluginImpl implements SeerPlugin {

	private static final Logger log = LoggerFactory.getLogger(MUDCollectorSeerPluginImpl.class);

	@Override
	public void activate(SeerConfiguration seerConfiguration) {
		SeerMgtService seerMgtService = null;
		ServiceLoader<SeerMgtService> serviceLoader = ServiceLoader.load(SeerMgtService.class);
		for (SeerMgtService provider : serviceLoader) {
			seerMgtService = provider;
		}
		MUDCollectorDataHolder.setSeerMgtService(seerMgtService);

		OFController ofControllerProvider = null;
		ServiceLoader<OFController> ofControllerServiceLoader = ServiceLoader.load(OFController.class);
		for (OFController ofController : ofControllerServiceLoader) {
			ofControllerProvider = ofController;
		}
		MUDCollectorDataHolder.setOfController(ofControllerProvider);
		MUDCollectorDataHolder.setMudConfig(seerConfiguration.getMudConfig());

		MudieStatsCollector mudieStatsCollector = new MudieStatsCollector();
		MUDCollectorDataHolder.setMudieStatsCollector(mudieStatsCollector);

	}

	@Override
	public void deactivate() {

	}

	@Override
	public List<String> getModuleDependencies() {
		List<String> dependencies = new ArrayList<>();
		dependencies.add("com.networkseer.seer.mgt.internal.SeerManagmentSeerPluginImpl");
		dependencies.add("com.networkseer.sdn.controller.mgt.internal.SdnControllerSeerPluginImpl");
		return dependencies;
	}
}
