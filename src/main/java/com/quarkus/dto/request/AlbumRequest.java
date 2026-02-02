package com.quarkus.dto.request;

import jakarta.validation.constraints.*;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.util.List;

@Schema(description = "Request body for creating or updating an album")
public record AlbumRequest(
    @Schema(description = "Title of the album", examples = {"A Night at the Opera"}, required = true)
    @NotBlank(message = "Title is required")
    String title,

    @Schema(description = "Release year of the album", examples = {"1975"}, required = true, minimum = "1900", maximum = "2100")
    @NotNull(message = "Year is required")
    @Min(value = 1900, message = "Year must be greater than or equal to 1900")
    @Max(value = 2100, message = "Year must be less than or equal to 2100")
    Integer year,

    @Schema(description = "List of artist IDs associated with this album", examples = {"[1, 2]"}, required = true)
    @NotEmpty(message = "At least one artist is required")
    List<Long> artistIds
) {
}
