package com.networkseer.statistic.collector;

import com.networkseer.common.openflow.OFFlow;
import com.networkseer.common.stats.OFFlowStatsListener;
import com.networkseer.sdn.controller.mgt.OFController;
import com.networkseer.sdn.controller.mgt.exception.OFControllerException;
import com.networkseer.statistic.collector.internal.StatsCollectorDataHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;


public class StatsCollector {

	private static final Logger log = LoggerFactory.getLogger(StatsCollector.class);

	public void collectStats() {
		List<OFFlowStatsListener> ofFlowStatsListeners = StatsCollectorDataHolder.getOfFlowStatsListeners();
		OFController ofController = StatsCollectorDataHolder.getOfController();
		try {
			Map<String, List<OFFlow>> ofFlowStats =  ofController.getFlowStats();
			for (OFFlowStatsListener ofFlowStatsListener : ofFlowStatsListeners) {
				ofFlowStatsListener.processFlowStats(ofFlowStats);
			}
		} catch (OFControllerException e) {
			log.error("Failed to collect of flow stats", e);
		}
	}

}
