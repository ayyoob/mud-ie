package com.networkseer.sdn.controller.mgt.internal;

import com.networkseer.common.SeerPlugin;
import com.networkseer.common.config.Controller;
import com.networkseer.common.config.SeerConfiguration;
import com.networkseer.sdn.controller.mgt.OFController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

public class SdnControllerSeerPluginImpl implements SeerPlugin {

	private static final Logger log = LoggerFactory.getLogger(SdnControllerSeerPluginImpl.class);
	private static final String FLOODLIGHT = "floodlight";

	public SdnControllerSeerPluginImpl() { };

	@Override
	public void activate(SeerConfiguration seerConfiguration) {
		log.debug("SdnControllerSeerPlugin module is activated");
		Controller controller = seerConfiguration.getController();

	}

	@Override
	public void deactivate() {

	}

	@Override
	public List<String> getModuleDependencies() {
		return null;
	}
}
