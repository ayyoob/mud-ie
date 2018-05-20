package com.mudie.seer.mgt;

import com.mudie.common.PriviledgedSeerContext;
import com.mudie.common.SeerApiPlugin;
import com.mudie.seer.mgt.internal.SeerManagementDataHolder;
import com.mudie.seer.mgt.dto.Device;
import com.mudie.seer.mgt.dto.Switch;
import com.mudie.seer.mgt.exception.SeerManagementException;
import com.mudie.seer.mgt.service.SeerMgtService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

@Path("/seer/")
@Produces(MediaType.APPLICATION_JSON)
@Api(value = "/seer", description = "seer mgt services")
public class SeerMgtRestApi implements SeerApiPlugin {

	private static final Logger log = LoggerFactory.getLogger(SeerMgtRestApi.class);

	@GET
	@Path("/switches")
	@ApiOperation(
			value = "switch details",
			notes = "Returns switch details",
			response = List.class)
	@ApiResponses(value = { @ApiResponse(code = 200, message = "return switch details")})
	public Response getSwitches() {
		try {
			SeerMgtService seerMgtService = SeerManagementDataHolder.getSeerMgtService();
			List<Switch> aswitchs = seerMgtService.getSwitches(PriviledgedSeerContext.getUserName());
			return Response.ok().entity(aswitchs).build();

		} catch (SeerManagementException e) {
			log.error(e.getMessage(), e);
			return Response.serverError().entity("login service is temporarily unavailable, try again later").build();
		}
	}

	@GET
	@Path("/switch/{dpId}/devices")
	@ApiOperation(
			value = "device details",
			notes = "Returns device details",
			response = List.class)
	@ApiResponses(value = { @ApiResponse(code = 200, message = "return device details")})
	public Response getDevices(@PathParam("dpId") String dpId) {
		try {
			SeerMgtService seerMgtService = SeerManagementDataHolder.getSeerMgtService();
			Switch aswitch = seerMgtService.getSwitch(dpId);
			if (aswitch != null) {
				if (aswitch.getOwner().equals(PriviledgedSeerContext.getUserName())) {
					List<Device> users = seerMgtService.getDevices(dpId);
					return Response.ok().entity(users).build();
				}
				return Response.status(Response.Status.UNAUTHORIZED).build();
			}
			return Response.noContent().build();

		} catch (SeerManagementException e) {
			log.error(e.getMessage(), e);
			return Response.serverError().entity("login service is temporarily unavailable, try again later").build();
		}

	}
}
