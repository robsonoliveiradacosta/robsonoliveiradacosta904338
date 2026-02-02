package com.quarkus.dto.response;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "Regional information")
public record RegionalResponse(
    @Schema(description = "Unique identifier of the regional", examples = {"1"})
    Integer id,

    @Schema(description = "Name of the regional", examples = {"São Paulo"})
    String name,

    @Schema(description = "Whether the regional is active", examples = {"true"})
    Boolean active
) {}
