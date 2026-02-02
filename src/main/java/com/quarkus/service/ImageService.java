package com.quarkus.service;

import com.quarkus.entity.Album;
import com.quarkus.repository.AlbumRepository;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.http.Method;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.InternalServerErrorException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import java.io.InputStream;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@ApplicationScoped
public class ImageService {

    private static final Logger LOG = Logger.getLogger(ImageService.class);

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
        "image/jpeg",
        "image/png",
        "image/webp"
    );

    @Inject
    MinioClient minioClient;

    @Inject
    AlbumRepository albumRepository;

    @ConfigProperty(name = "app.minio.bucket")
    String bucket;

    @ConfigProperty(name = "app.minio.presigned-url.expiry")
    int presignedUrlExpiry;

    @ConfigProperty(name = "app.minio.max-file-size")
    long maxFileSize;

    /**
     * Upload an image for an album.
     *
     * @param albumId Album ID
     * @param filename Original filename
     * @param inputStream Image data stream
     * @param size File size in bytes
     * @param contentType MIME type
     * @return MinIO object key
     * @throws NotFoundException if album not found
     * @throws BadRequestException if file validation fails
     */
    @Transactional
    public String uploadImage(Long albumId, String filename, InputStream inputStream,
                             long size, String contentType) {
        // Validate album exists
        Album album = albumRepository.findByIdOptional(albumId)
            .orElseThrow(() -> new NotFoundException("Album not found with id: " + albumId));

        // Validate content type
        validateContentType(contentType);

        // Validate file size
        validateFileSize(size);

        // Generate unique object name: albumId/uuid_filename
        String objectName = generateObjectName(albumId, filename);

        try {
            // Upload to MinIO
            minioClient.putObject(
                PutObjectArgs.builder()
                    .bucket(bucket)
                    .object(objectName)
                    .stream(inputStream, size, -1)
                    .contentType(contentType)
                    .build()
            );

            // Add image key to album
            album.getImageKeys().add(objectName);
            albumRepository.persist(album);

            LOG.infof("Image uploaded successfully: %s", objectName);
            return objectName;

        } catch (Exception e) {
            LOG.errorf(e, "Failed to upload image to MinIO: %s", objectName);
            throw new InternalServerErrorException("Failed to upload image", e);
        }
    }

    /**
     * Generate a presigned URL for accessing an image.
     *
     * @param albumId Album ID
     * @param imageKey Image object key
     * @return Presigned URL valid for configured expiry time
     * @throws NotFoundException if album or image not found
     */
    public String getPresignedUrl(Long albumId, String imageKey) {
        // Validate album exists and owns the image
        Album album = albumRepository.findByIdOptional(albumId)
            .orElseThrow(() -> new NotFoundException("Album not found with id: " + albumId));

        if (!album.getImageKeys().contains(imageKey)) {
            throw new NotFoundException("Image not found for album: " + imageKey);
        }

        try {
            String url = minioClient.getPresignedObjectUrl(
                GetPresignedObjectUrlArgs.builder()
                    .bucket(bucket)
                    .object(imageKey)
                    .method(Method.GET)
                    .expiry(presignedUrlExpiry, TimeUnit.MINUTES)
                    .build()
            );

            LOG.debugf("Generated presigned URL for image: %s", imageKey);
            return url;

        } catch (Exception e) {
            LOG.errorf(e, "Failed to generate presigned URL for image: %s", imageKey);
            throw new InternalServerErrorException("Failed to generate presigned URL", e);
        }
    }

    /**
     * Delete an image from both MinIO and the album's image list.
     *
     * @param albumId Album ID
     * @param imageKey Image object key
     * @throws NotFoundException if album or image not found
     */
    @Transactional
    public void deleteImage(Long albumId, String imageKey) {
        // Validate album exists and owns the image
        Album album = albumRepository.findByIdOptional(albumId)
            .orElseThrow(() -> new NotFoundException("Album not found with id: " + albumId));

        if (!album.getImageKeys().contains(imageKey)) {
            throw new NotFoundException("Image not found for album: " + imageKey);
        }

        try {
            // Remove from MinIO
            minioClient.removeObject(
                RemoveObjectArgs.builder()
                    .bucket(bucket)
                    .object(imageKey)
                    .build()
            );

            // Remove from album
            album.getImageKeys().remove(imageKey);
            albumRepository.persist(album);

            LOG.infof("Image deleted successfully: %s", imageKey);

        } catch (Exception e) {
            LOG.errorf(e, "Failed to delete image from MinIO: %s", imageKey);
            throw new InternalServerErrorException("Failed to delete image", e);
        }
    }

    /**
     * Validate content type is allowed.
     *
     * @param contentType MIME type to validate
     * @throws BadRequestException if content type not allowed
     */
    private void validateContentType(String contentType) {
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase())) {
            throw new BadRequestException(
                "Invalid content type. Allowed types: " + String.join(", ", ALLOWED_CONTENT_TYPES)
            );
        }
    }

    /**
     * Validate file size is within limits.
     *
     * @param size File size in bytes
     * @throws BadRequestException if file size exceeds limit
     */
    private void validateFileSize(long size) {
        if (size > maxFileSize) {
            throw new BadRequestException(
                String.format("File size exceeds maximum allowed size of %d bytes (%.2f MB)",
                    maxFileSize, maxFileSize / (1024.0 * 1024.0))
            );
        }
        if (size <= 0) {
            throw new BadRequestException("File size must be greater than 0");
        }
    }

    /**
     * Generate a unique object name for MinIO storage.
     * Format: albumId/uuid_filename
     *
     * @param albumId Album ID
     * @param filename Original filename
     * @return Generated object name
     */
    private String generateObjectName(Long albumId, String filename) {
        String uuid = UUID.randomUUID().toString();
        return String.format("%d/%s_%s", albumId, uuid, filename);
    }
}
