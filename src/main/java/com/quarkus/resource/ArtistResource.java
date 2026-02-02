package com.quarkus.resource;

import com.quarkus.dto.request.ArtistRequest;
import com.quarkus.dto.response.ArtistResponse;
import com.quarkus.service.ArtistService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.util.List;

@Path("/api/v1/artists")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Artists", description = "Artist management endpoints")
public class ArtistResource {

    @Inject
    ArtistService artistService;

    @GET
    @RolesAllowed({"USER", "ADMIN"})
    @Operation(summary = "List all artists", description = "Returns a list of artists with optional name filter and sorting")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "Artists retrieved successfully")
    })
    public List<ArtistResponse> listArtists(
        @Parameter(description = "Filter by name (partial match, case-insensitive)")
        @QueryParam("name") String name,

        @Parameter(description = "Sort parameter in format 'field:direction' (e.g., 'name:asc' or 'name:desc')", example = "name:asc")
        @QueryParam("sort") String sort
    ) {
        return artistService.listArtists(name, sort);
    }

    @GET
    @Path("/{id}")
    @RolesAllowed({"USER", "ADMIN"})
    @Operation(summary = "Get artist by ID", description = "Returns a single artist by its ID")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "Artist found"),
        @APIResponse(responseCode = "404", description = "Artist not found")
    })
    public ArtistResponse getArtist(
        @Parameter(description = "Artist ID", required = true)
        @PathParam("id") Long id
    ) {
        return artistService.findById(id);
    }

    @POST
    @RolesAllowed("ADMIN")
    @Operation(summary = "Create a new artist", description = "Creates a new artist with the provided information")
    @APIResponses({
        @APIResponse(responseCode = "201", description = "Artist created successfully"),
        @APIResponse(responseCode = "400", description = "Invalid request data")
    })
    public Response createArtist(@Valid ArtistRequest request) {
        ArtistResponse response = artistService.createArtist(request);
        return Response.status(Response.Status.CREATED).entity(response).build();
    }

    @PUT
    @Path("/{id}")
    @RolesAllowed("ADMIN")
    @Operation(summary = "Update an artist", description = "Updates an existing artist by ID")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "Artist updated successfully"),
        @APIResponse(responseCode = "400", description = "Invalid request data"),
        @APIResponse(responseCode = "404", description = "Artist not found")
    })
    public ArtistResponse updateArtist(
        @Parameter(description = "Artist ID", required = true)
        @PathParam("id") Long id,
        @Valid ArtistRequest request
    ) {
        return artistService.updateArtist(id, request);
    }

    @DELETE
    @Path("/{id}")
    @RolesAllowed("ADMIN")
    @Operation(summary = "Delete an artist", description = "Permanently deletes an artist by ID (hard delete)")
    @APIResponses({
        @APIResponse(responseCode = "204", description = "Artist deleted successfully"),
        @APIResponse(responseCode = "404", description = "Artist not found")
    })
    public Response deleteArtist(
        @Parameter(description = "Artist ID", required = true)
        @PathParam("id") Long id
    ) {
        artistService.deleteArtist(id);
        return Response.noContent().build();
    }
}
