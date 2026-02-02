package com.quarkus.dto.request;

import com.quarkus.entity.ArtistType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ArtistRequest(
    @NotBlank(message = "Name is required")
    String name,

    @NotNull(message = "Type is required")
    ArtistType type
) {
}
