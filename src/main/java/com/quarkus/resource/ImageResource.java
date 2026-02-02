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
            responseCode = "401",
            description = "Unauthorized - Authentication required"
    )
    @APIResponse(
            responseCode = "403",
            description = "Forbidden - Admin role required"
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
            @Parameter(description = "Album ID", required = true)
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
            String hash = imageService.uploadImage(
                    albumId,
                    form.file.fileName(),
                    Files.newInputStream(form.file.filePath()),
                    form.file.size(),
                    form.file.contentType()
            );

            ImageUploadResponse response = ImageUploadResponse.of(hash);
            return Response.status(Response.Status.CREATED).entity(response).build();

        } catch (IOException e) {
            throw new InternalServerErrorException("Failed to read uploaded file", e);
        }
    }

    @GET
    @Path("/{hash:.+}")
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
            responseCode = "401",
            description = "Unauthorized - Authentication required"
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
            @Parameter(description = "Album ID", required = true)
            @PathParam("albumId") Long albumId,

            @Parameter(
                    description = "Image hash identifier (format: yyyy/MM/dd/uuid.ext)",
                    required = true
            )
            @PathParam("hash") String hash
    ) {
        String url = imageService.getPresignedUrl(albumId, hash);
        ImageUrlResponse response = ImageUrlResponse.of(hash, url, presignedUrlExpiry);

        return Response.ok(response).build();
    }

    @DELETE
    @Path("/{hash:.+}")
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
            responseCode = "401",
            description = "Unauthorized - Authentication required"
    )
    @APIResponse(
            responseCode = "403",
            description = "Forbidden - Admin role required"
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
            @Parameter(description = "Album ID", required = true)
            @PathParam("albumId") Long albumId,

            @Parameter(
                    description = "Image hash identifier (format: yyyy/MM/dd/uuid.ext)",
                    required = true
            )
            @PathParam("hash") String hash
    ) {
        imageService.deleteImage(albumId, hash);
        return Response.noContent().build();
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
