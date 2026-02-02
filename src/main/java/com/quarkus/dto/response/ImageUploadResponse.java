package com.quarkus.dto.response;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "Image upload response containing the image hash and metadata")
public record ImageUploadResponse(
    @Schema(description = "Image hash identifier for the uploaded image", examples = {"2026/02/02/550e8400-e29b-41d4-a716-446655440000.jpg"})
    String hash,

    @Schema(description = "Message confirming successful upload", examples = {"Image uploaded successfully"})
    String message
) {
    public static ImageUploadResponse of(String hash) {
        return new ImageUploadResponse(hash, "Image uploaded successfully");
    }
}
