package com.quarkus.dto;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "Regional data from external API")
public record RegionalDto(
    @Schema(description = "Regional identifier from external API", examples = {"1"})
    Integer id,

    @Schema(description = "Regional name", examples = {"São Paulo"})
    String nome
) {}
