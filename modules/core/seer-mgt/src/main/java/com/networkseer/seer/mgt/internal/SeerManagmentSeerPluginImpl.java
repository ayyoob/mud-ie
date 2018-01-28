package com.networkseer.seer.mgt.internal;

import com.networkseer.common.SeerPlugin;
import com.networkseer.common.config.SeerConfiguration;
import com.networkseer.seer.mgt.dao.SeerManagementDAOFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

public class SeerManagmentSeerPluginImpl implements SeerPlugin {
	private static final Logger log = LoggerFactory.getLogger(SeerManagmentSeerPluginImpl.class);

	@Override
	public void activate(SeerConfiguration seerConfiguration) {
		SeerManagementDAOFactory.init();
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
