package com.networkseer.iot.security.internal;

import com.networkseer.common.SeerPlugin;
import com.networkseer.common.config.SeerConfiguration;
import com.networkseer.seer.mgt.service.SeerMgtService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

public class IoTSecuritySeerPluginImpl implements SeerPlugin {
	private static final Logger log = LoggerFactory.getLogger(IoTSecuritySeerPluginImpl.class);

	@Override
	public void activate(SeerConfiguration seerConfiguration) {
		SeerMgtService seerMgtService = null;
		ServiceLoader<SeerMgtService> serviceLoader = ServiceLoader.load(SeerMgtService.class);
		for (SeerMgtService provider : serviceLoader) {
			seerMgtService = provider;
		}
		IoTSecurityDataHolder.setSeerMgtService(seerMgtService);

	}

	@Override
	public void deactivate() {

	}

	@Override
	public List<String> getModuleDependencies() {
		List<String> dependencies = new ArrayList<>();
		dependencies.add("com.networkseer.seer.mgt.service.impl.SeerMgtServiceImpl");
		dependencies.add("com.networkseer.sdn.controller.mgt.internal.SdnControllerSeerPluginImpl");
		return dependencies;
	}
}
