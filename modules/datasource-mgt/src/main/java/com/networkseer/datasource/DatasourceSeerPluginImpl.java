package com.networkseer.datasource;

import com.networkseer.common.SeerPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class DatasourceSeerPluginImpl implements SeerPlugin {

	private static final Logger log = LoggerFactory.getLogger(DataSourceFactory.class);

	public DatasourceSeerPluginImpl() { };

	@Override
	public void activate() {
		DataSourceFactory.registerDatasources();
		log.debug("Datasource module is activated");
	}

	@Override
	public void deactivate() {

	}

	@Override
	public List<String> getModuleDependencies() {
		return null;
	}
}
