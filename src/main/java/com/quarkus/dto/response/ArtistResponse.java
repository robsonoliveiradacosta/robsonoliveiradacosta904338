package com.quarkus.dto.response;

import com.quarkus.entity.Artist;
import com.quarkus.entity.ArtistType;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "Artist information")
public record ArtistResponse(
    @Schema(description = "Unique identifier of the artist", examples = {"1"})
    Long id,

    @Schema(description = "Name of the artist", examples = {"Queen"})
    String name,

    @Schema(description = "Type of the artist", examples = {"BAND"})
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
