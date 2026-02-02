package com.quarkus.dto.request;

import jakarta.validation.constraints.*;

import java.util.List;

public record AlbumRequest(
    @NotBlank(message = "Title is required")
    String title,

    @NotNull(message = "Year is required")
    @Min(value = 1900, message = "Year must be greater than or equal to 1900")
    @Max(value = 2100, message = "Year must be less than or equal to 2100")
    Integer year,

    @NotEmpty(message = "At least one artist is required")
    List<Long> artistIds
) {
}
