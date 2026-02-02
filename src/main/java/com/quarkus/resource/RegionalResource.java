package com.quarkus.resource;

import com.quarkus.dto.response.RegionalResponse;
import com.quarkus.dto.response.SyncResult;
import com.quarkus.repository.RegionalRepository;
import com.quarkus.service.RegionalSyncService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.util.List;
import java.util.stream.Collectors;

@Path("/api/v1/regionals")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Regionals", description = "Regional management endpoints")
public class RegionalResource {

    @Inject
    RegionalRepository repository;

    @Inject
    RegionalSyncService syncService;

    @GET
    @RolesAllowed({"USER", "ADMIN"})
    @Operation(summary = "List all active regionals", description = "Returns a list of all active regionals")
    @APIResponse(responseCode = "200", description = "List of active regionals")
    @APIResponse(responseCode = "401", description = "Unauthorized - Authentication required")
    public List<RegionalResponse> list() {
        return repository.findAllActive().stream()
            .map(r -> new RegionalResponse(r.getId(), r.getName(), r.getActive()))
            .collect(Collectors.toList());
    }

    @POST
    @Path("/sync")
    @RolesAllowed("ADMIN")
    @Operation(summary = "Trigger manual regional synchronization", description = "Manually triggers the synchronization with the external regional API")
    @APIResponse(responseCode = "200", description = "Synchronization completed successfully")
    @APIResponse(responseCode = "401", description = "Unauthorized - Authentication required")
    @APIResponse(responseCode = "403", description = "Forbidden - Admin role required")
    @APIResponse(responseCode = "500", description = "Synchronization failed")
    public SyncResult sync() {
        return syncService.sync();
    }
}
