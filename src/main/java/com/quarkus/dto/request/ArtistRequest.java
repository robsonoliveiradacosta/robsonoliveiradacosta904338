package com.quarkus.dto.request;

import com.quarkus.entity.ArtistType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "Request body for creating or updating an artist")
public record ArtistRequest(
    @Schema(description = "Name of the artist", examples = {"Queen"}, required = true)
    @NotBlank(message = "Name is required")
    String name,

    @Schema(description = "Type of the artist (SINGER or BAND)", examples = {"BAND"}, required = true)
    @NotNull(message = "Type is required")
    ArtistType type
) {
}
