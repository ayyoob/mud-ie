package com.networkseer.parental.mgt.internal;

import com.networkseer.common.SeerPlugin;
import com.networkseer.common.config.SeerConfiguration;
import com.networkseer.parental.mgt.dao.ParentalManagementDAOFactory;
import com.networkseer.parental.mgt.service.ParentalService;
import com.networkseer.parental.mgt.service.impl.ParentalServiceImpl;
import com.networkseer.seer.mgt.service.SeerMgtService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

public class ParentalManagmentSeerPluginImpl implements SeerPlugin {
	private static final Logger log = LoggerFactory.getLogger(ParentalManagmentSeerPluginImpl.class);

	@Override
	public void activate(SeerConfiguration seerConfiguration) {
		ParentalManagementDAOFactory.init();
		SeerMgtService seerMgtService = null;
		ServiceLoader<SeerMgtService> serviceLoader = ServiceLoader.load(SeerMgtService.class);
		for (SeerMgtService provider : serviceLoader) {
			seerMgtService = provider;
		}
		ParentalManagementDataHolder.setSeerMgtService(seerMgtService);
		ParentalService parentalService = new ParentalServiceImpl();
		ParentalManagementDataHolder.setParentalService(parentalService);
	}

	@Override
	public void deactivate() {

	}

	@Override
	public List<String> getModuleDependencies() {
		List<String> dependencies = new ArrayList<>();
		dependencies.add("com.networkseer.datasource.internal.DatasourceSeerPluginImpl");
		dependencies.add("com.networkseer.seer.mgt.service.impl.SeerMgtServiceImpl");
		return dependencies;
	}
}
