package com.networkseer.core.health.monitor;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/health")
@Produces(MediaType.APPLICATION_JSON)
public class HealthRestApiController {
    @GET
    public boolean health() {
        return true;
    }
}
