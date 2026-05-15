package com.quarkus.dto.response;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.time.Instant;

@Schema(description = "Probe response for whether an album is favorited by the caller")
public record FavoriteStatusResponse(
    @Schema(description = "Album identifier", examples = {"42"})
    Long albumId,

    @Schema(description = "When the album was favorited", examples = {"2026-05-15T10:30:00Z"})
    Instant favoritedAt
) {
}
