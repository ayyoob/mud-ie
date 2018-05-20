package com.mudie.seer.mgt;

import com.mudie.common.SeerApiPlugin;
import com.mudie.seer.mgt.internal.SeerManagementDataHolder;
import com.mudie.seer.mgt.dto.Switch;
import com.mudie.seer.mgt.exception.SeerManagementException;
import com.mudie.seer.mgt.service.SeerMgtService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/admin/seer/")
@Produces(MediaType.APPLICATION_JSON)
@Api(value = "/admin/seer", description = "seer mgt services")
public class SeerMgtAdminRestApi implements SeerApiPlugin {

	private static final Logger log = LoggerFactory.getLogger(SeerMgtAdminRestApi.class);

	@POST
	@Path("/switch")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "switch added.")})
	public Response addSwitch(Switch aswitch) {
		try {
			SeerMgtService seerMgtService = SeerManagementDataHolder.getSeerMgtService();
			int status = seerMgtService.addSwitch(aswitch);
			return Response.ok().entity(true).build();
		} catch (SeerManagementException e) {
			log.error(e.getMessage(), e);
			return Response.serverError().entity("Failed to add the switch").build();
		}
	}
}
