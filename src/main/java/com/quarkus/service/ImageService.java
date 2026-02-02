package com.quarkus.service;

import com.quarkus.entity.Album;
import com.quarkus.entity.AlbumImage;
import com.quarkus.repository.AlbumImageRepository;
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
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
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

    @Inject
    AlbumImageRepository albumImageRepository;

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
     * @return Image hash
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

        // Generate hash using new pattern: yyyy/MM/dd/uuid.ext
        String extension = getExtension(filename);
        String hash = generateHash(extension);

        try {
            // Upload to MinIO
            minioClient.putObject(
                PutObjectArgs.builder()
                    .bucket(bucket)
                    .object(hash)
                    .stream(inputStream, size, -1)
                    .contentType(contentType)
                    .build()
            );

            // Create and persist AlbumImage entity
            AlbumImage albumImage = new AlbumImage(
                album,
                bucket,
                hash,
                contentType,
                (int) size
            );
            album.addImage(albumImage);
            albumImageRepository.persist(albumImage);

            LOG.infof("Image uploaded successfully: %s", hash);
            return hash;

        } catch (Exception e) {
            LOG.errorf(e, "Failed to upload image to MinIO: %s", hash);
            throw new InternalServerErrorException("Failed to upload image", e);
        }
    }

    /**
     * Generate a presigned URL for accessing an image.
     *
     * @param albumId Album ID
     * @param hash Image hash
     * @return Presigned URL valid for configured expiry time
     * @throws NotFoundException if album or image not found
     */
    public String getPresignedUrl(Long albumId, String hash) {
        // Validate album exists and owns the image
        AlbumImage albumImage = albumImageRepository.findByAlbumIdAndHash(albumId, hash)
            .orElseThrow(() -> new NotFoundException("Image not found for album: " + hash));

        try {
            String url = minioClient.getPresignedObjectUrl(
                GetPresignedObjectUrlArgs.builder()
                    .bucket(bucket)
                    .object(hash)
                    .method(Method.GET)
                    .expiry(presignedUrlExpiry, TimeUnit.MINUTES)
                    .build()
            );

            LOG.debugf("Generated presigned URL for image: %s", hash);
            return url;

        } catch (Exception e) {
            LOG.errorf(e, "Failed to generate presigned URL for image: %s", hash);
            throw new InternalServerErrorException("Failed to generate presigned URL", e);
        }
    }

    /**
     * Delete an image from both MinIO and the database.
     *
     * @param albumId Album ID
     * @param hash Image hash
     * @throws NotFoundException if album or image not found
     */
    @Transactional
    public void deleteImage(Long albumId, String hash) {
        // Validate album exists and owns the image
        AlbumImage albumImage = albumImageRepository.findByAlbumIdAndHash(albumId, hash)
            .orElseThrow(() -> new NotFoundException("Image not found for album: " + hash));

        try {
            // Remove from MinIO
            minioClient.removeObject(
                RemoveObjectArgs.builder()
                    .bucket(bucket)
                    .object(hash)
                    .build()
            );

            // Remove from database
            albumImageRepository.delete(albumImage);

            LOG.infof("Image deleted successfully: %s", hash);

        } catch (Exception e) {
            LOG.errorf(e, "Failed to delete image from MinIO: %s", hash);
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
     * Extract file extension from filename.
     *
     * @param fileName Original filename
     * @return File extension (including dot) or empty string if no extension
     */
    private String getExtension(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            return "";
        }
        return fileName.substring(fileName.lastIndexOf("."));
    }

    /**
     * Generate hash for MinIO storage using date-based pattern.
     * Format: yyyy/MM/dd/uuid.ext
     *
     * @param extension File extension (including dot)
     * @return Generated hash
     */
    private String generateHash(String extension) {
        LocalDate now = LocalDate.now();
        String datePath = now.format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        String uuid = UUID.randomUUID().toString();
        return String.format("%s/%s%s", datePath, uuid, extension);
    }
}
