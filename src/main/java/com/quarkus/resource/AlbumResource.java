package com.quarkus.resource;

import com.quarkus.dto.request.AlbumRequest;
import com.quarkus.dto.response.AlbumResponse;
import com.quarkus.dto.response.PageResponse;
import com.quarkus.entity.ArtistType;
import com.quarkus.service.AlbumService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

@Path("/api/v1/albums")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Albums", description = "Album management endpoints")
public class AlbumResource {

    @Inject
    AlbumService albumService;

    @GET
    @RolesAllowed({"USER", "ADMIN"})
    @Operation(
        summary = "List all albums",
        description = "Get a paginated list of albums with optional filtering by artist type and sorting"
    )
    @APIResponse(
        responseCode = "200",
        description = "Success",
        content = @Content(schema = @Schema(implementation = PageResponse.class))
    )
    @APIResponse(
        responseCode = "401",
        description = "Unauthorized - Authentication required"
    )
    public Response listAlbums(
        @Parameter(description = "Page number (0-based)")
        @QueryParam("page") @DefaultValue("0") int page,

        @Parameter(description = "Page size (max 100)")
        @QueryParam("size") @DefaultValue("20") int size,

        @Parameter(description = "Sort criteria (e.g., 'title:asc', 'year:desc')")
        @QueryParam("sort") String sort,

        @Parameter(description = "Filter by artist type (SINGER or BAND)")
        @QueryParam("artistType") ArtistType artistType
    ) {
        PageResponse<AlbumResponse> result = albumService.findAll(page, size, sort, artistType);
        return Response.ok(result).build();
    }

    @GET
    @Path("/{id}")
    @RolesAllowed({"USER", "ADMIN"})
    @Operation(
        summary = "Get album by ID",
        description = "Retrieve a single album by its ID"
    )
    @APIResponse(
        responseCode = "200",
        description = "Album found",
        content = @Content(schema = @Schema(implementation = AlbumResponse.class))
    )
    @APIResponse(
        responseCode = "401",
        description = "Unauthorized - Authentication required"
    )
    @APIResponse(
        responseCode = "404",
        description = "Album not found"
    )
    public Response getAlbum(
        @Parameter(description = "Album ID", required = true)
        @PathParam("id") Long id
    ) {
        AlbumResponse album = albumService.findById(id);
        return Response.ok(album).build();
    }

    @POST
    @RolesAllowed("ADMIN")
    @Operation(
        summary = "Create new album",
        description = "Create a new album with linked artists"
    )
    @APIResponse(
        responseCode = "201",
        description = "Album created successfully",
        content = @Content(schema = @Schema(implementation = AlbumResponse.class))
    )
    @APIResponse(
        responseCode = "400",
        description = "Invalid request data"
    )
    @APIResponse(
        responseCode = "401",
        description = "Unauthorized - Authentication required"
    )
    @APIResponse(
        responseCode = "403",
        description = "Forbidden - Admin role required"
    )
    @APIResponse(
        responseCode = "404",
        description = "One or more artist IDs not found"
    )
    public Response createAlbum(@Valid AlbumRequest request) {
        AlbumResponse album = albumService.create(request);
        return Response.status(Response.Status.CREATED).entity(album).build();
    }

    @PUT
    @Path("/{id}")
    @RolesAllowed("ADMIN")
    @Operation(
        summary = "Update album",
        description = "Update an existing album by ID"
    )
    @APIResponse(
        responseCode = "200",
        description = "Album updated successfully",
        content = @Content(schema = @Schema(implementation = AlbumResponse.class))
    )
    @APIResponse(
        responseCode = "400",
        description = "Invalid request data"
    )
    @APIResponse(
        responseCode = "401",
        description = "Unauthorized - Authentication required"
    )
    @APIResponse(
        responseCode = "403",
        description = "Forbidden - Admin role required"
    )
    @APIResponse(
        responseCode = "404",
        description = "Album or one of the artists not found"
    )
    public Response updateAlbum(
        @Parameter(description = "Album ID", required = true)
        @PathParam("id") Long id,
        @Valid AlbumRequest request
    ) {
        AlbumResponse album = albumService.update(id, request);
        return Response.ok(album).build();
    }

    @DELETE
    @Path("/{id}")
    @RolesAllowed("ADMIN")
    @Operation(
        summary = "Delete album",
        description = "Permanently delete an album by ID"
    )
    @APIResponse(
        responseCode = "204",
        description = "Album deleted successfully"
    )
    @APIResponse(
        responseCode = "401",
        description = "Unauthorized - Authentication required"
    )
    @APIResponse(
        responseCode = "403",
        description = "Forbidden - Admin role required"
    )
    @APIResponse(
        responseCode = "404",
        description = "Album not found"
    )
    public Response deleteAlbum(
        @Parameter(description = "Album ID", required = true)
        @PathParam("id") Long id
    ) {
        albumService.delete(id);
        return Response.noContent().build();
    }
}
