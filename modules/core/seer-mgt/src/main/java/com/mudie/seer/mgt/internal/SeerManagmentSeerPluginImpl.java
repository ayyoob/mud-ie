package com.mudie.seer.mgt.internal;

import com.mudie.common.SeerPlugin;
import com.mudie.common.config.SeerConfiguration;
import com.mudie.seer.mgt.dao.SeerManagementDAOFactory;
import com.mudie.seer.mgt.exception.SeerManagementException;
import com.mudie.seer.mgt.dto.Switch;
import com.mudie.seer.mgt.service.SeerMgtService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

public class SeerManagmentSeerPluginImpl implements SeerPlugin {
	private static final Logger log = LoggerFactory.getLogger(SeerManagmentSeerPluginImpl.class);
	private static final String DEFAULT_OWNER = "admin";

	@Override
	public void activate(SeerConfiguration seerConfiguration) {
		SeerManagementDAOFactory.init();

		SeerMgtService seerMgtService = null;
		ServiceLoader<SeerMgtService> serviceLoader = ServiceLoader.load(SeerMgtService.class);
		for (SeerMgtService provider : serviceLoader) {
			seerMgtService = provider;
		}
		SeerManagementDataHolder.setSeerMgtService(seerMgtService);

		if (seerConfiguration.getSwitches() != null) {
			for (String dpId : seerConfiguration.getSwitches()) {
				try {
					Switch aswitch = seerMgtService.getSwitch(dpId);
					if (aswitch == null) {
						aswitch = new Switch();
						aswitch.setStatus(Switch.Status.ACTIVE);
						aswitch.setDpId(dpId);
						aswitch.setOwner(DEFAULT_OWNER);
						seerMgtService.addSwitch(aswitch);
					}
				} catch (SeerManagementException e) {
					log.error("Failed to add the switch", e);
				}
			}
		}

	}

	@Override
	public void deactivate() {

	}

	@Override
	public List<String> getModuleDependencies() {
		List<String> dependencies = new ArrayList<>();
		dependencies.add("com.mudie.datasource.internal.DatasourceSeerPluginImpl");
		return dependencies;
	}
}
