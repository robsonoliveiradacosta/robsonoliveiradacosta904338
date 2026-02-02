package com.quarkus.dto.response;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "Result of regional synchronization operation")
public record SyncResult(
    @Schema(description = "Number of new regionals inserted", examples = {"5"})
    int inserted,

    @Schema(description = "Number of existing regionals updated", examples = {"3"})
    int updated,

    @Schema(description = "Number of regionals deactivated", examples = {"2"})
    int deactivated
) {}
