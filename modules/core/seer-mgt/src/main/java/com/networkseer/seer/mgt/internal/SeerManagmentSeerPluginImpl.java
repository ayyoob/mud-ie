package com.networkseer.seer.mgt.internal;

import com.networkseer.common.SeerPlugin;
import com.networkseer.common.config.SeerConfiguration;
import com.networkseer.seer.mgt.dao.SeerManagementDAOFactory;
import com.networkseer.seer.mgt.dto.Group;
import com.networkseer.seer.mgt.dto.Switch;
import com.networkseer.seer.mgt.exception.SeerManagementException;
import com.networkseer.seer.mgt.service.SeerMgtService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
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
				int switchId = -1;
				try {
					Switch aswitch = seerMgtService.getSwitch(dpId);
					if ( aswitch == null) {
						aswitch = new Switch();
						aswitch.setStatus(Switch.Status.ACTIVE);
						aswitch.setDpId(dpId);
						aswitch.setBillingDay(0);
						aswitch.setQuota(10);
						aswitch.setOwner(DEFAULT_OWNER);
						aswitch.setBillingDate(new Timestamp(new Date().getTime()));
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
		dependencies.add("com.networkseer.datasource.internal.DatasourceSeerPluginImpl");
		return dependencies;
	}
}
