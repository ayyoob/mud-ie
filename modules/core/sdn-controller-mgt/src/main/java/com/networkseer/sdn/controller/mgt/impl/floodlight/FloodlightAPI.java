package com.networkseer.sdn.controller.mgt.impl.floodlight;


import javax.ws.rs.*;

@Path("/wm")
public interface FloodlightAPI {

	@POST
	@Path("/staticentrypusher/json")
	void addFlow(FlowDTO flowDTO);

	@GET
	@Path("/core/switch/{dpId}/port-desc/json")
	PortInfo getSwitchPortInfo(@PathParam("dpId") String dpId);

	@DELETE
	@Path("/staticentrypusher/json")
	void removeFlow(RemoveFlowDTO removeFlowDTO);

	@GET
	@Path("/staticentrypusher/clear/{dpId}/json")
	String clearFlowRules(@PathParam("dpId") String dpId);

	@GET
	@Path("/core/switch/{dpId}/flow/json")
	PerSwitchStatFlowsDTO getFlows(@PathParam("dpId") String dpId);

	@GET
	@Path("/core/switch/all/flow/json")
	String getFlows();
}
