package com.mudie.mud.collector.mudflowdto;

import com.mudie.common.openflow.OFFlow;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class MudFeatureWrapper {

	private Map<Integer, OFFlow> staticFlows = new HashMap();
	private Map<Integer, OFFlow> dynamicFlows = new HashMap();
	private Map<Integer, OFFlow> lastStaticFlowRecords;
	private Map<Integer, OFFlow> lastReactiveFlowRecords;
	private int flowOrder[];

	public MudFeatureWrapper(Set<OFFlow> staticOfflows, Set<OFFlow> dynamicOfflows) {
		staticOfflows.forEach(ofFlow -> staticFlows.put(ofFlow.hashCode(), ofFlow));
		dynamicOfflows.forEach(ofFlow -> dynamicFlows.put(ofFlow.hashCode(), ofFlow));
	}

	public Map<Integer, OFFlow> getStaticFlows() {
		return staticFlows;
	}

	public void setStaticFlows(Map<Integer, OFFlow> staticFlows) {
		this.staticFlows = staticFlows;
	}

	public Map<Integer, OFFlow> getDynamicFlows() {
		return dynamicFlows;
	}

	public void setDynamicFlows(Map<Integer, OFFlow> dynamicFlows) {
		this.dynamicFlows = dynamicFlows;
	}

	public void addStaticOFFlow(OFFlow ofFlow) {
		staticFlows.put(ofFlow.hashCode(), ofFlow);
	}

	public void addDynamicOFFlow(OFFlow ofFlow) {
		dynamicFlows.put(ofFlow.hashCode(), ofFlow);
	}

	public Map<Integer, OFFlow> getLastStaticFlowRecords() {
		return lastStaticFlowRecords;
	}

	public void setLastStaticFlowRecords(Map<Integer, OFFlow> lastStaticFlowRecords) {
		this.lastStaticFlowRecords = lastStaticFlowRecords;
	}

	public Map<Integer, OFFlow> getLastReactiveFlowRecords() {
		return lastReactiveFlowRecords;
	}

	public void setLastReactiveFlowRecords(Map<Integer, OFFlow> lastReactiveFlowRecords) {
		this.lastReactiveFlowRecords = lastReactiveFlowRecords;
	}

	public int[] getFlowOrder() {
		return flowOrder;
	}

	public void setFlowOrder(int[] flowOrder) {
		this.flowOrder = flowOrder;
	}

	public void resetDynamicFlowMetrics() {
		dynamicFlows.forEach((integer, ofFlow) -> {
			ofFlow.setPacketCount(0);
			ofFlow.setByteCount(0);
		});
	}
}
