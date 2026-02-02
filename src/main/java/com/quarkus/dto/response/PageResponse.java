package com.quarkus.dto.response;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.util.List;

@Schema(description = "Paginated response wrapper")
public record PageResponse<T>(
    @Schema(description = "List of items in the current page")
    List<T> content,

    @Schema(description = "Current page number (0-based)", examples = {"0"})
    int page,

    @Schema(description = "Number of items per page", examples = {"20"})
    int size,

    @Schema(description = "Total number of items across all pages", examples = {"100"})
    long totalElements,

    @Schema(description = "Total number of pages", examples = {"5"})
    int totalPages
) {
    public static <T> PageResponse<T> of(List<T> content, int page, int size, long totalElements) {
        int totalPages = (int) Math.ceil((double) totalElements / size);
        return new PageResponse<>(content, page, size, totalElements, totalPages);
    }
}
