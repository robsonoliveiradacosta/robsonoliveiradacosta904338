package com.quarkus.dto.response;

public record SyncResult(
    int inserted,
    int updated,
    int deactivated
) {}
