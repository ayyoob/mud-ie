package com.mudie.sdn.controller.mgt.internal;

import com.mudie.common.SeerPlugin;
import com.mudie.common.config.Controller;
import com.mudie.common.config.SeerConfiguration;
import com.mudie.sdn.controller.mgt.impl.NatsClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class SdnControllerSeerPluginImpl implements SeerPlugin {

	private static final Logger log = LoggerFactory.getLogger(SdnControllerSeerPluginImpl.class);

	public SdnControllerSeerPluginImpl() { };

	@Override
	public void activate(SeerConfiguration seerConfiguration) {
		log.debug("SdnControllerSeerPlugin module is activated");
		Controller controller = seerConfiguration.getController();
		SdnControllerDataHolder.setController(controller);

		if (controller.getProperties() == null) {
			setupNATS();
		} else {
			String value = controller.getProperties().get("mode");
			if (value == null || value.length() == 0) {
				setupNATS();
			}
		}
	}

	private void setupNATS() {
		NatsClient natsClient = new NatsClient();
		SdnControllerDataHolder.setNatsClient(natsClient);
		Thread thread = new Thread(natsClient);
		thread.start();
	}

	@Override
	public void deactivate() {
		if (SdnControllerDataHolder.getController().getProperties() == null) {
			SdnControllerDataHolder.getNatsClient().disconnect();
		} else {
			String value = SdnControllerDataHolder.getController().getProperties().get("mode");
			if (value == null || value.length() == 0) {
				SdnControllerDataHolder.getNatsClient().disconnect();
			}
		}

	}

	@Override
	public List<String> getModuleDependencies() {
		return null;
	}
}
