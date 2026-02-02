package com.quarkus.resource;

import com.quarkus.dto.request.LoginRequest;
import com.quarkus.dto.response.TokenResponse;
import com.quarkus.service.AuthService;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

@Path("/api/v1/auth")
@Tag(name = "Authentication", description = "Authentication and authorization operations")
public class AuthResource {

    @Inject
    AuthService authService;

    @POST
    @Path("/login")
    @PermitAll
    @Operation(summary = "User login", description = "Authenticates a user and returns a JWT token")
    @APIResponse(
            responseCode = "200",
            description = "Login successful",
            content = @Content(schema = @Schema(implementation = TokenResponse.class))
    )
    @APIResponse(responseCode = "401", description = "Invalid credentials")
    public Response login(
            @Valid @RequestBody(description = "Login credentials") LoginRequest request) {
        TokenResponse response = authService.login(request);
        return Response.ok(response).build();
    }

    @POST
    @Path("/refresh")
    @RolesAllowed({"USER", "ADMIN"})
    @Operation(summary = "Refresh token", description = "Generates a new JWT token for the authenticated user")
    @APIResponse(
            responseCode = "200",
            description = "Token refreshed successfully",
            content = @Content(schema = @Schema(implementation = TokenResponse.class))
    )
    @APIResponse(responseCode = "401", description = "Unauthorized")
    public Response refresh(@Context SecurityContext securityContext) {
        String username = securityContext.getUserPrincipal().getName();
        TokenResponse response = authService.refresh(username);
        return Response.ok(response).build();
    }
}
