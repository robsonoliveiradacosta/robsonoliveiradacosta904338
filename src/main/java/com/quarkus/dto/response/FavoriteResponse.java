package com.quarkus.dto.response;

import com.quarkus.entity.Favorite;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.time.Instant;

@Schema(description = "A user's favorited album, with the album payload denormalized")
public record FavoriteResponse(
    @Schema(description = "Album identifier", examples = {"42"})
    Long albumId,

    @Schema(description = "When the album was favorited", examples = {"2026-05-15T10:30:00Z"})
    Instant favoritedAt,

    @Schema(description = "Album payload (title, year, artists)")
    AlbumResponse album
) {
    public static FavoriteResponse from(Favorite favorite) {
        return new FavoriteResponse(
            favorite.getAlbum().getId(),
            favorite.getCreatedAt(),
            AlbumResponse.from(favorite.getAlbum())
        );
    }
}
