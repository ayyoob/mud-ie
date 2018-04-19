package com.networkseer.mud.processor.internal;

import com.networkseer.common.SeerDirectory;
import com.networkseer.common.SeerPlugin;
import com.networkseer.common.config.SeerConfiguration;
import com.networkseer.sdn.controller.mgt.OFController;
import com.networkseer.seer.mgt.service.SeerMgtService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.*;

public class MUDProcessorSeerPluginImpl implements SeerPlugin {

	private static final Logger log = LoggerFactory.getLogger(MUDProcessorSeerPluginImpl.class);

	@Override
	public void activate(SeerConfiguration seerConfiguration) {
		SeerMgtService seerMgtService = null;
		ServiceLoader<SeerMgtService> serviceLoader = ServiceLoader.load(SeerMgtService.class);
		for (SeerMgtService provider : serviceLoader) {
			seerMgtService = provider;
		}
		MUDProcesserDataHolder.setSeerMgtService(seerMgtService);

		OFController ofControllerProvider = null;
		ServiceLoader<OFController> ofControllerServiceLoader = ServiceLoader.load(OFController.class);
		for (OFController ofController : ofControllerServiceLoader) {
			ofControllerProvider = ofController;
		}
		MUDProcesserDataHolder.setOfController(ofControllerProvider);
		MUDProcesserDataHolder.setMudConfig(seerConfiguration.getMudConfig());

		String mudDevicePath = SeerDirectory.getConfigDirectory() + File.separator + "mud-device-list.csv";

		BufferedReader br = null;
		String line = "";
		String cvsSplitBy = ",";
		Map<String, String> mudDevices = new HashMap<>();
		try {

			br = new BufferedReader(new FileReader(mudDevicePath));
			while ((line = br.readLine()) != null) {
				// use comma as separator
				String[] entries = line.split(cvsSplitBy);
				if (entries.length == 2) {
					mudDevices.put(entries[0], entries[1]);
				}
			}

		} catch (IOException e) {
			log.error("Failed to process mud-device-list.csv file", e);
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					log.error("Failed to close process mud-device-list.csv file", e);
				}
			}
		}
		MUDProcesserDataHolder.setMudDevices(mudDevices);
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
