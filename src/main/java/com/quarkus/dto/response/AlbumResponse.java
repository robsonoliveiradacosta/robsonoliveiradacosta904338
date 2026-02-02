package com.quarkus.dto.response;

import com.quarkus.entity.Album;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.util.List;

@Schema(description = "Album information with associated artists")
public record AlbumResponse(
    @Schema(description = "Unique identifier of the album", examples = {"1"})
    Long id,

    @Schema(description = "Title of the album", examples = {"A Night at the Opera"})
    String title,

    @Schema(description = "Release year of the album", examples = {"1975"})
    Integer year,

    @Schema(description = "List of artists associated with this album")
    List<ArtistResponse> artists
) {
    public static AlbumResponse from(Album album) {
        List<ArtistResponse> artistResponses = album.getArtists().stream()
            .map(ArtistResponse::from)
            .toList();

        return new AlbumResponse(
            album.getId(),
            album.getTitle(),
            album.getYear(),
            artistResponses
        );
    }
}
