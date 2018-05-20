package com.mudie.framework.health.monitor;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/health")
@Produces(MediaType.APPLICATION_JSON)
@Api(value = "/health", description = "Health test for services")
public class HealthRestApiController {

    @GET
    @ApiResponses(value = { @ApiResponse(code = 200, message = "server is running")})
    public Response health() {
        return Response.ok().entity(true).build();
    }
}
