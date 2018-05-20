package com.mudie.common.stats;

import com.mudie.common.openflow.OFFlow;

import java.util.List;
import java.util.Map;

public interface OFFlowStatsListener {

	/**
	 * Key is the dpId, this is mapped with flowStats
	 * @param ofFlowStats
	 */
	void processFlowStats(Map<String, List<OFFlow>> ofFlowStats);
}
