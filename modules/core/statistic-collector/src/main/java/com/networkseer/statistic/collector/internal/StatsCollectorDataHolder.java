package com.networkseer.statistic.collector.internal;

import com.networkseer.common.stats.OFFlowStatsListener;
import com.networkseer.sdn.controller.mgt.OFController;

import java.util.List;

public class StatsCollectorDataHolder {

	private static List<OFFlowStatsListener> ofFlowStatsListeners;
	private static OFController ofController;

	public static List<OFFlowStatsListener> getOfFlowStatsListeners() {
		return ofFlowStatsListeners;
	}

	public static void setOfFlowStatsListeners(List<OFFlowStatsListener> ofFlowStatsListeners) {
		StatsCollectorDataHolder.ofFlowStatsListeners = ofFlowStatsListeners;
	}

	public static OFController getOfController() {
		return ofController;
	}

	public static void setOfController(OFController ofController) {
		StatsCollectorDataHolder.ofController = ofController;
	}
}
