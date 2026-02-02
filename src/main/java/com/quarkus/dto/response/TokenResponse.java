package com.quarkus.dto.response;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "JWT token response")
public record TokenResponse(
        @Schema(description = "JWT access token", examples = {"eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9..."})
        String accessToken,

        @Schema(description = "Token type", examples = {"Bearer"})
        String tokenType,

        @Schema(description = "Token expiration time in seconds", examples = {"300"})
        long expiresIn
) {
    public TokenResponse(String accessToken, long expiresIn) {
        this(accessToken, "Bearer", expiresIn);
    }
}
