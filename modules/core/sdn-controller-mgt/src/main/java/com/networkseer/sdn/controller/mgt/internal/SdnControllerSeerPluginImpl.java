package com.networkseer.sdn.controller.mgt.internal;

import com.networkseer.common.SeerPlugin;
import com.networkseer.common.config.Controller;
import com.networkseer.common.config.SeerConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class SdnControllerSeerPluginImpl implements SeerPlugin {

	private static final Logger log = LoggerFactory.getLogger(SdnControllerSeerPluginImpl.class);
	private static final String FLOODLIGHT = "floodlight";

	public SdnControllerSeerPluginImpl() { };

	@Override
	public void activate(SeerConfiguration seerConfiguration) {
		log.debug("SdnControllerSeerPlugin module is activated");
		Controller controller = seerConfiguration.getController();
//		FloodlightAPI floodlightAPI;
//		if (controller.getType().equals(FLOODLIGHT)) {
//			Feign.Builder builder = Feign.builder().logger(new Slf4jLogger())
//					.logLevel(feign.Logger.Level.FULL).encoder(new JacksonEncoder()).decoder(new JacksonDecoder());
//			String basePath = "http://" + controller.getHostname() + ":" + controller.getType();
//			floodlightAPI = floodlightAPI = builder.target(FloodlightAPI.class, basePath);
//		}

	}

	@Override
	public void deactivate() {

	}

	@Override
	public List<String> getModuleDependencies() {
		return null;
	}
}
