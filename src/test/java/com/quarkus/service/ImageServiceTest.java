package com.quarkus.service;

import com.quarkus.entity.Album;
import com.quarkus.entity.AlbumImage;
import com.quarkus.repository.AlbumImageRepository;
import com.quarkus.repository.AlbumRepository;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.InternalServerErrorException;
import jakarta.ws.rs.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ImageServiceTest {

    @Mock
    MinioClient minioClient;

    @Mock
    AlbumRepository albumRepository;

    @Mock
    AlbumImageRepository albumImageRepository;

    @InjectMocks
    ImageService imageService;

    private Album testAlbum;

    @BeforeEach
    void setUp() {
        // Initialize fields via reflection since we can't use @InjectMocks with @ConfigProperty
        try {
            setField(imageService, "bucket", "test-bucket");
            setField(imageService, "presignedUrlExpiry", 30);
            setField(imageService, "maxFileSize", 52428800L);
        } catch (Exception e) {
            fail("Failed to initialize test fields", e);
        }

        testAlbum = new Album("Test Album", 2024);
        testAlbum.setId(1L);
        testAlbum.setImages(new ArrayList<>());
    }

    @Test
    void shouldUploadImageSuccessfully() throws Exception {
        // Given
        when(albumRepository.findByIdOptional(1L)).thenReturn(Optional.of(testAlbum));

        InputStream inputStream = new ByteArrayInputStream("test image data".getBytes());
        String filename = "cover.jpg";
        long size = 1024L;
        String contentType = "image/jpeg";

        // When
        String hash = imageService.uploadImage(1L, filename, inputStream, size, contentType);

        // Then
        assertNotNull(hash);
        assertTrue(hash.matches("\\d{4}/\\d{2}/\\d{2}/[a-f0-9\\-]+\\.jpg"));
        verify(minioClient).putObject(any(PutObjectArgs.class));
        verify(albumImageRepository).persist(any(AlbumImage.class));
    }

    @Test
    void shouldThrowNotFoundExceptionWhenAlbumNotFound() {
        // Given
        when(albumRepository.findByIdOptional(999L)).thenReturn(Optional.empty());
        InputStream inputStream = new ByteArrayInputStream("test".getBytes());

        // When & Then
        assertThrows(NotFoundException.class, () ->
                imageService.uploadImage(999L, "cover.jpg", inputStream, 1024L, "image/jpeg")
        );
    }

    @Test
    void shouldThrowBadRequestExceptionForInvalidContentType() {
        // Given
        when(albumRepository.findByIdOptional(1L)).thenReturn(Optional.of(testAlbum));
        InputStream inputStream = new ByteArrayInputStream("test".getBytes());

        // When & Then
        assertThrows(BadRequestException.class, () ->
                imageService.uploadImage(1L, "file.pdf", inputStream, 1024L, "application/pdf")
        );
    }

    @Test
    void shouldThrowBadRequestExceptionForFileSizeExceeded() {
        // Given
        when(albumRepository.findByIdOptional(1L)).thenReturn(Optional.of(testAlbum));
        InputStream inputStream = new ByteArrayInputStream("test".getBytes());
        long oversizedFile = 52428801L; // 50MB + 1 byte

        // When & Then
        assertThrows(BadRequestException.class, () ->
                imageService.uploadImage(1L, "large.jpg", inputStream, oversizedFile, "image/jpeg")
        );
    }

    @Test
    void shouldGeneratePresignedUrlSuccessfully() throws Exception {
        // Given
        String hash = "2026/02/02/uuid.jpg";
        AlbumImage albumImage = new AlbumImage(testAlbum, "test-bucket", hash, "image/jpeg", 1024);
        when(albumImageRepository.findByAlbumIdAndHash(1L, hash))
                .thenReturn(Optional.of(albumImage));
        when(minioClient.getPresignedObjectUrl(any(GetPresignedObjectUrlArgs.class)))
                .thenReturn("https://minio.example.com/presigned-url");

        // When
        String url = imageService.getPresignedUrl(1L, hash);

        // Then
        assertNotNull(url);
        assertEquals("https://minio.example.com/presigned-url", url);
        verify(minioClient).getPresignedObjectUrl(any(GetPresignedObjectUrlArgs.class));
    }

    @Test
    void shouldThrowNotFoundWhenImageNotOwnedByAlbum() {
        // Given
        String hash = "2026/02/02/uuid.jpg";
        when(albumImageRepository.findByAlbumIdAndHash(1L, hash))
                .thenReturn(Optional.empty());

        // When & Then
        assertThrows(NotFoundException.class, () ->
                imageService.getPresignedUrl(1L, hash)
        );
    }

    @Test
    void shouldDeleteImageSuccessfully() throws Exception {
        // Given
        String hash = "2026/02/02/uuid.jpg";
        AlbumImage albumImage = new AlbumImage(testAlbum, "test-bucket", hash, "image/jpeg", 1024);
        when(albumImageRepository.findByAlbumIdAndHash(1L, hash))
                .thenReturn(Optional.of(albumImage));

        // When
        imageService.deleteImage(1L, hash);

        // Then
        verify(minioClient).removeObject(any(RemoveObjectArgs.class));
        verify(albumImageRepository).delete(albumImage);
    }

    @Test
    void shouldThrowNotFoundWhenDeletingNonExistentImage() {
        // Given
        String hash = "2026/02/02/nonexistent.jpg";
        when(albumImageRepository.findByAlbumIdAndHash(1L, hash))
                .thenReturn(Optional.empty());

        // When & Then
        assertThrows(NotFoundException.class, () ->
                imageService.deleteImage(1L, hash)
        );
    }

    @Test
    void shouldThrowInternalServerErrorWhenMinioFails() throws Exception {
        // Given
        when(albumRepository.findByIdOptional(1L)).thenReturn(Optional.of(testAlbum));
        when(minioClient.putObject(any(PutObjectArgs.class)))
                .thenThrow(new RuntimeException("MinIO connection failed"));

        InputStream inputStream = new ByteArrayInputStream("test".getBytes());

        // When & Then
        assertThrows(InternalServerErrorException.class, () ->
                imageService.uploadImage(1L, "cover.jpg", inputStream, 1024L, "image/jpeg")
        );
    }

    @Test
    void shouldAcceptAllValidImageFormats() {
        // Given
        when(albumRepository.findByIdOptional(1L)).thenReturn(Optional.of(testAlbum));

        // Test JPEG
        assertDoesNotThrow(() -> {
            InputStream stream = new ByteArrayInputStream("test".getBytes());
            imageService.uploadImage(1L, "image.jpg", stream, 1024L, "image/jpeg");
        });

        // Test PNG
        assertDoesNotThrow(() -> {
            InputStream stream = new ByteArrayInputStream("test".getBytes());
            imageService.uploadImage(1L, "image.png", stream, 1024L, "image/png");
        });

        // Test WebP
        assertDoesNotThrow(() -> {
            InputStream stream = new ByteArrayInputStream("test".getBytes());
            imageService.uploadImage(1L, "image.webp", stream, 1024L, "image/webp");
        });
    }

    /**
     * Helper method to set private fields via reflection for testing.
     */
    private void setField(Object target, String fieldName, Object value) throws Exception {
        var field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}
