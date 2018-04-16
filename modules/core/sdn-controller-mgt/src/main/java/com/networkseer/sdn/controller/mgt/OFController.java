package com.networkseer.sdn.controller.mgt;

import com.networkseer.common.openflow.OFFlow;
import com.networkseer.sdn.controller.mgt.exception.OFControllerException;

import java.util.*;

public interface OFController {

	void addFlow(String dpId, OFFlow ofFlow) throws OFControllerException;

	void addFlows(String dpId, List<OFFlow> ofFlows) throws OFControllerException;

	void removeFlow(String dpId, OFFlow ofFlow) throws OFControllerException;

	void clearAllFlows(String dpId) throws OFControllerException;

	List<OFFlow> getFlowStats(String dpId) throws OFControllerException;

	Map<String, List<OFFlow>> getFlowStats() throws OFControllerException;

	void removeFlows(String dpId, List<OFFlow> ofFlows) throws OFControllerException;

	void addACLs(String dpId, String deviceMac, List<OFFlow> ofFlows, int vlan) throws OFControllerException;

	HostInfo getHostInfo(String device);

}
