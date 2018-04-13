package com.networkseer.sdn.controller.mgt.impl;

import com.networkseer.sdn.controller.mgt.OFController;
import com.networkseer.common.openflow.OFFlow;
import com.networkseer.sdn.controller.mgt.exception.OFControllerException;
import feign.FeignException;

import java.util.List;
import java.util.Map;

public class FaucetOFControllerImpl implements OFController {
	private static final String DEFAULT_FLOW_ACTION = "output=normal";
	private static final String VXLAN_FLOW_ACTION_PREFIX = ",output=";
	private static final String VXLAN_INTERFACE_NAME = "tun-vxlan";

	@Override
	public void addFlow(String dpId, OFFlow ofFlow) throws OFControllerException {
		try {

		} catch (FeignException e) {
			throw new OFControllerException(e);
		}
	}

	@Override
	public void addFlows(String dpId, List<OFFlow> ofFlows) throws OFControllerException {


	}

	@Override
	public void removeFlow(String dpId, OFFlow ofFlow) throws OFControllerException {

	}

	@Override
	public void removeFlows(String dpId, List<OFFlow> ofFlows) throws OFControllerException {

	}

	@Override
	public void addACLs(String dpId, List<OFFlow> ofFlows) throws OFControllerException {

	}

	@Override
	public void clearAllFlows(String dpId) throws OFControllerException {

	}

	@Override
	public List<OFFlow> getFlowStats(String dpId) throws OFControllerException {
		return null;
	}

	@Override
	public Map<String, List<OFFlow>> getFlowStats() throws OFControllerException {
		return null;
	}




}
