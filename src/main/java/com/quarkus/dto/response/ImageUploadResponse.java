package com.quarkus.dto.response;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "Image upload response containing the image key")
public record ImageUploadResponse(
    @Schema(description = "Image key identifier for the uploaded image", examples = {"1/550e8400-e29b-41d4-a716-446655440000_cover.jpg"})
    String imageKey,

    @Schema(description = "Message confirming successful upload", examples = {"Image uploaded successfully"})
    String message
) {
    public static ImageUploadResponse of(String imageKey) {
        return new ImageUploadResponse(imageKey, "Image uploaded successfully");
    }
}
