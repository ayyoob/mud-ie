package com.networkseer.parental.mgt;

import com.networkseer.common.PriviledgedSeerContext;
import com.networkseer.common.SeerApiPlugin;
import com.networkseer.parental.mgt.dto.DnsEntry;
import com.networkseer.parental.mgt.exception.ParentalManagementException;
import com.networkseer.parental.mgt.internal.ParentalManagementDataHolder;
import com.networkseer.seer.mgt.dto.Group;
import com.networkseer.seer.mgt.dto.Switch;
import com.networkseer.seer.mgt.exception.SeerManagementException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

import java.util.List;

@Path("/parental")
@Produces(MediaType.APPLICATION_JSON)
@Api(value = "/parental", description = "parental mgt services")
public class ParentalSeerRestApi implements SeerApiPlugin {
	private static final Logger log = LoggerFactory.getLogger(ParentalSeerRestApi.class);

	@GET
	@Path("/switches/{dpId}/devices/{deviceMac}")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "return user count")})
	public Response getDnsEntries(@PathParam("dpId") String dpId,@PathParam("deviceMac") String deviceMac) {
		try {
			Group group = ParentalManagementDataHolder.getSeerMgtService().getGroup(dpId, deviceMac);
			if (group.isParentalAppEnabled()) {
				Switch aswitch = ParentalManagementDataHolder.getSeerMgtService().getSwitch(dpId);
				if (aswitch.getOwner().equals(PriviledgedSeerContext.getUserName())) {
					List<DnsEntry> dnsEntryList = ParentalManagementDataHolder.getParentalService()
							.getDnsEntries(dpId, deviceMac);
					return Response.ok().entity(dnsEntryList).build();
				} else{
					return Response.status(Response.Status.UNAUTHORIZED).build();
				}
			} else {
				return Response.status(Response.Status.NOT_ACCEPTABLE).build();
			}
		} catch (ParentalManagementException e) {
			log.error("Failed to process request.", e);
			return Response.serverError().entity("service is temporarily unavailable, try again later").build();
		} catch (SeerManagementException e) {
			log.error(e.getMessage(), e);
			return Response.serverError().entity("service is temporarily unavailable, try again later").build();
		}

	}

}
