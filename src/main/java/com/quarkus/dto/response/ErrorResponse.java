package com.quarkus.dto.response;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Schema(description = "Error response containing details about the error")
public record ErrorResponse(
        @Schema(description = "HTTP status code", examples = {"400"})
        int status,

        @Schema(description = "Error message", examples = {"Invalid request data"})
        String message,

        @Schema(description = "Timestamp when the error occurred", examples = {"2024-01-15T10:30:00"})
        String timestamp,

        @Schema(description = "Request path that caused the error", examples = {"/api/v1/artists"})
        String path
) {
    public ErrorResponse(int status, String message, String path) {
        this(status, message, LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME), path);
    }
}
