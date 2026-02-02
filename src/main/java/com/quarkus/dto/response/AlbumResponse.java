package com.quarkus.dto.response;

import com.quarkus.entity.Album;

import java.util.List;

public record AlbumResponse(
    Long id,
    String title,
    Integer year,
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
