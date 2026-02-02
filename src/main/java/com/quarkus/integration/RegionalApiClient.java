package com.quarkus.integration;

import com.quarkus.dto.RegionalDto;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import java.util.List;

@RegisterRestClient(configKey = "regional-api")
@Path("/v1")
public interface RegionalApiClient {

    @GET
    @Path("/regionais")
    @Produces(MediaType.APPLICATION_JSON)
    List<RegionalDto> getRegionais();
}
