package com.quarkus.resource;

import com.quarkus.dto.response.ImageUploadResponse;
import com.quarkus.dto.response.ImageUrlResponse;
import com.quarkus.service.ImageService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.jboss.resteasy.reactive.PartType;
import org.jboss.resteasy.reactive.RestForm;
import org.jboss.resteasy.reactive.multipart.FileUpload;

import java.io.IOException;
import java.nio.file.Files;
import java.util.Map;
import java.util.Set;

@Path("/api/v1/albums/{albumId}/images")
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "Images", description = "Album image management endpoints")
public class ImageResource {

    @Inject
    ImageService imageService;

    @ConfigProperty(name = "app.minio.presigned-url.expiry")
    int presignedUrlExpiry;

    @ConfigProperty(name = "app.minio.max-file-size")
    long maxFileSize;

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/jpeg",
            "image/png",
            "image/webp"
    );

    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @RolesAllowed("ADMIN")
    @Operation(
            summary = "Upload album image",
            description = "Upload an image for an album (JPEG, PNG, or WebP). Maximum file size: 50MB"
    )
    @APIResponse(
            responseCode = "201",
            description = "Image uploaded successfully",
            content = @Content(schema = @Schema(implementation = ImageUploadResponse.class))
    )
    @APIResponse(
            responseCode = "400",
            description = "Invalid file format or size exceeds limit"
    )
    @APIResponse(
            responseCode = "404",
            description = "Album not found"
    )
    @APIResponse(
            responseCode = "503",
            description = "MinIO service unavailable"
    )
    public Response uploadImage(
            @Parameter(description = "Album ID", required = true, example = "1")
            @PathParam("albumId") Long albumId,

            @RequestBody(
                    description = "Image file to upload",
                    required = true,
                    content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA)
            )
            ImageUploadForm form
    ) {
        if (form == null || form.file == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .type(MediaType.APPLICATION_JSON)
                    .entity(Map.of("message", "File is required"))
                    .build();
        }
        String contentType = form.file.contentType();
        long size = form.file.size();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType)) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .type(MediaType.APPLICATION_JSON)
                    .entity(Map.of("message", "Invalid content type"))
                    .build();
        }
        if (size > maxFileSize) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .type(MediaType.APPLICATION_JSON)
                    .entity(Map.of("message", "File size exceeds limit"))
                    .build();
        }

        try {
            String imageKey = imageService.uploadImage(
                    albumId,
                    form.file.fileName(),
                    Files.newInputStream(form.file.filePath()),
                    form.file.size(),
                    form.file.contentType()
            );

            ImageUploadResponse response = ImageUploadResponse.of(imageKey);
            return Response.status(Response.Status.CREATED).entity(response).build();

        } catch (IOException e) {
            throw new InternalServerErrorException("Failed to read uploaded file", e);
        }
    }

    @GET
    @Path("/{imageKey}")
    @RolesAllowed({"USER", "ADMIN"})
    @Operation(
            summary = "Get image URL",
            description = "Get a presigned URL to access an album image. URL is valid for 30 minutes"
    )
    @APIResponse(
            responseCode = "200",
            description = "Presigned URL generated successfully",
            content = @Content(schema = @Schema(implementation = ImageUrlResponse.class))
    )
    @APIResponse(
            responseCode = "404",
            description = "Album or image not found"
    )
    @APIResponse(
            responseCode = "503",
            description = "MinIO service unavailable"
    )
    public Response getImageUrl(
            @Parameter(description = "Album ID", required = true, example = "1")
            @PathParam("albumId") Long albumId,

            @Parameter(
                    description = "Image key identifier",
                    required = true,
                    example = "1/550e8400-e29b-41d4-a716-446655440000_cover.jpg"
            )
            @PathParam("imageKey") String imageKey
    ) {
        // Handle URL-encoded path parameters
        String decodedImageKey = decodeImageKey(albumId, imageKey);

        String url = imageService.getPresignedUrl(albumId, decodedImageKey);
        ImageUrlResponse response = ImageUrlResponse.of(decodedImageKey, url, presignedUrlExpiry);

        return Response.ok(response).build();
    }

    @DELETE
    @Path("/{imageKey}")
    @RolesAllowed("ADMIN")
    @Operation(
            summary = "Delete album image",
            description = "Delete an image from an album and remove it from MinIO storage"
    )
    @APIResponse(
            responseCode = "204",
            description = "Image deleted successfully"
    )
    @APIResponse(
            responseCode = "404",
            description = "Album or image not found"
    )
    @APIResponse(
            responseCode = "503",
            description = "MinIO service unavailable"
    )
    public Response deleteImage(
            @Parameter(description = "Album ID", required = true, example = "1")
            @PathParam("albumId") Long albumId,

            @Parameter(
                    description = "Image key identifier",
                    required = true,
                    example = "1/550e8400-e29b-41d4-a716-446655440000_cover.jpg"
            )
            @PathParam("imageKey") String imageKey
    ) {
        // Handle URL-encoded path parameters
        String decodedImageKey = decodeImageKey(albumId, imageKey);

        imageService.deleteImage(albumId, decodedImageKey);
        return Response.noContent().build();
    }

    /**
     * Decode image key from path parameter.
     * The imageKey path param only contains the UUID and filename part,
     * so we need to reconstruct the full key as "albumId/imageKey".
     *
     * @param albumId  Album ID
     * @param imageKey Image key from path parameter
     * @return Full image key
     */
    private String decodeImageKey(Long albumId, String imageKey) {
        // If imageKey doesn't start with albumId, prepend it
        if (!imageKey.startsWith(albumId + "/")) {
            return albumId + "/" + imageKey;
        }
        return imageKey;
    }

    /**
     * Form for multipart file upload.
     */
    public static class ImageUploadForm {
        @RestForm("file")
        @PartType(MediaType.APPLICATION_OCTET_STREAM)
        public FileUpload file;
    }
}
