package com.quarkus.dto.response;

public record RegionalResponse(
    Integer id,
    String name,
    Boolean active
) {}
