package com.quarkus.dto.response;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "Image URL response containing presigned URL")
public record ImageUrlResponse(
    @Schema(description = "Image key identifier", examples = {"1/550e8400-e29b-41d4-a716-446655440000_cover.jpg"})
    String imageKey,

    @Schema(description = "Presigned URL for accessing the image (valid for 30 minutes)")
    String url,

    @Schema(description = "URL expiry time in minutes", examples = {"30"})
    int expiresInMinutes
) {
    public static ImageUrlResponse of(String imageKey, String url, int expiresInMinutes) {
        return new ImageUrlResponse(imageKey, url, expiresInMinutes);
    }
}
