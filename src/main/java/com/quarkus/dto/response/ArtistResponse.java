package com.quarkus.dto.response;

import com.quarkus.entity.Artist;
import com.quarkus.entity.ArtistType;

public record ArtistResponse(
    Long id,
    String name,
    ArtistType type
) {
    public static ArtistResponse from(Artist artist) {
        return new ArtistResponse(
            artist.getId(),
            artist.getName(),
            artist.getType()
        );
    }
}
