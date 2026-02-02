package com.quarkus.dto.response;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public record ErrorResponse(
        int status,
        String message,
        String timestamp,
        String path
) {
    public ErrorResponse(int status, String message, String path) {
        this(status, message, LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME), path);
    }
}
