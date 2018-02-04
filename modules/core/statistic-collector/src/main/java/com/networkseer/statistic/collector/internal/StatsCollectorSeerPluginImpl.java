package com.networkseer.statistic.collector.internal;

import com.networkseer.common.*;
import com.networkseer.common.config.SeerConfiguration;
import com.networkseer.common.stats.OFFlowStatsListener;
import com.networkseer.sdn.controller.mgt.OFController;
import com.networkseer.statistic.collector.StatsCollector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class StatsCollectorSeerPluginImpl implements SeerPlugin {

	private static final Logger log = LoggerFactory.getLogger(StatsCollectorSeerPluginImpl.class);
	private static StatsCollector statsCollector;
	@Override
	public void activate(SeerConfiguration seerConfiguration) {
		List<OFFlowStatsListener> ofFlowStatsListeners = new ArrayList<>();
		ServiceLoader<OFFlowStatsListener> serviceLoader = ServiceLoader.load(OFFlowStatsListener.class);
		for (OFFlowStatsListener provider : serviceLoader) {
			ofFlowStatsListeners.add(provider);
		}
		StatsCollectorDataHolder.setOfFlowStatsListeners(ofFlowStatsListeners);

		OFController ofController = null;
		ServiceLoader<OFController> ofControllers = ServiceLoader.load(OFController.class);
		for (OFController provider : ofControllers) {
			ofController = provider;
		}
		StatsCollectorDataHolder.setOfController(ofController);
		ScheduledExecutorService statsExecutor = Executors.newScheduledThreadPool(1);
		statsCollector = new StatsCollector();
		Runnable task = () -> {
			statsCollector.collectStats();
		};
		statsExecutor.scheduleWithFixedDelay(task, 1, 1, TimeUnit.MINUTES);

	}

	@Override
	public void deactivate() {

	}

	@Override
	public List<String> getModuleDependencies() {
		List<String> dependencies = new ArrayList<>();
		dependencies.add("com.networkseer.sdn.controller.mgt.internal.SdnControllerSeerPluginImpl");
		return dependencies;
	}
}
