package com.quarkus.resource;

import com.quarkus.dto.response.FavoriteResponse;
import com.quarkus.dto.response.FavoriteStatusResponse;
import com.quarkus.dto.response.PageResponse;
import com.quarkus.service.FavoriteService;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

@Path("/v1/me/favorites")
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "Favorites", description = "Per-user album favorites (owner-scoped via JWT)")
public class FavoriteResource {

    @Inject
    FavoriteService favoriteService;

    @Inject
    SecurityIdentity securityIdentity;

    @POST
    @Path("/{albumId}")
    @RolesAllowed({"USER", "ADMIN"})
    @Operation(
        summary = "Mark album as favorite",
        description = "Idempotent: returns 201 the first time, 200 on subsequent calls."
    )
    @APIResponse(responseCode = "201", description = "Favorite created",
        content = @Content(schema = @Schema(implementation = FavoriteResponse.class)))
    @APIResponse(responseCode = "200", description = "Already favorited (idempotent)",
        content = @Content(schema = @Schema(implementation = FavoriteResponse.class)))
    @APIResponse(responseCode = "401", description = "Authentication required")
    @APIResponse(responseCode = "404", description = "Album does not exist")
    public Response add(
        @Parameter(description = "Album ID", required = true)
        @PathParam("albumId") Long albumId
    ) {
        FavoriteService.AddResult result = favoriteService.add(currentUsername(), albumId);
        Response.Status status = result.created() ? Response.Status.CREATED : Response.Status.OK;
        return Response.status(status).entity(result.response()).build();
    }

    @DELETE
    @Path("/{albumId}")
    @RolesAllowed({"USER", "ADMIN"})
    @Operation(
        summary = "Unmark album as favorite",
        description = "Idempotent: 204 whether or not the favorite existed."
    )
    @APIResponse(responseCode = "204", description = "Favorite removed (or no-op)")
    @APIResponse(responseCode = "401", description = "Authentication required")
    public Response remove(
        @Parameter(description = "Album ID", required = true)
        @PathParam("albumId") Long albumId
    ) {
        favoriteService.remove(currentUsername(), albumId);
        return Response.noContent().build();
    }

    @GET
    @RolesAllowed({"USER", "ADMIN"})
    @Operation(
        summary = "List my favorites",
        description = "Paginated list of the caller's favorited albums, newest first by default."
    )
    @APIResponse(responseCode = "200", description = "Paginated favorites",
        content = @Content(schema = @Schema(implementation = PageResponse.class)))
    @APIResponse(responseCode = "401", description = "Authentication required")
    public Response list(
        @Parameter(description = "Page number (0-based)")
        @QueryParam("page") @DefaultValue("0") int page,

        @Parameter(description = "Page size (max 100)")
        @QueryParam("size") @DefaultValue("20") int size,

        @Parameter(description = "Sort criteria (only 'createdAt:asc' or 'createdAt:desc')")
        @QueryParam("sort") String sort
    ) {
        PageResponse<FavoriteResponse> result = favoriteService.list(currentUsername(), page, size, sort);
        return Response.ok(result).build();
    }

    @GET
    @Path("/{albumId}")
    @RolesAllowed({"USER", "ADMIN"})
    @Operation(
        summary = "Probe favorite status for an album",
        description = "Returns 200 with favoritedAt if the caller has favorited this album, 404 otherwise."
    )
    @APIResponse(responseCode = "200", description = "Album is favorited",
        content = @Content(schema = @Schema(implementation = FavoriteStatusResponse.class)))
    @APIResponse(responseCode = "401", description = "Authentication required")
    @APIResponse(responseCode = "404", description = "Album is not favorited")
    public Response status(
        @Parameter(description = "Album ID", required = true)
        @PathParam("albumId") Long albumId
    ) {
        FavoriteStatusResponse status = favoriteService.get(currentUsername(), albumId);
        return Response.ok(status).build();
    }

    private String currentUsername() {
        return securityIdentity.getPrincipal().getName();
    }
}
