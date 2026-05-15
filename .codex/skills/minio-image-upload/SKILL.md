---
name: minio-image-upload
description: "Implement MinIO-backed image and file upload workflows in Quarkus APIs. Use when adding multipart uploads, file validation, object naming, bucket initialization, metadata persistence, presigned URLs, delete flows, MinIO health checks, Docker Compose services, and upload tests."
---

# MinIO Image Upload

## Goal

Add reliable media storage without coupling API responses to raw storage internals.

## Workflow

1. Define upload limits: allowed MIME types, extensions, maximum size, ownership, and replacement rules.
2. Configure MinIO through environment variables: `MINIO_URL`, `MINIO_ACCESS_KEY`, `MINIO_SECRET_KEY`, `MINIO_SECURE`, and `MINIO_BUCKET`.
3. Create a service that validates the multipart file before storage.
4. Generate stable object names using a collision-resistant strategy, such as a hash or UUID plus date partition.
5. Persist metadata in an entity when files belong to domain objects.
6. Return response DTOs with object metadata and presigned URLs when clients need direct access.
7. Add cleanup behavior for delete and replacement flows.

## Design Rules

- Do not trust client-provided filenames for object keys.
- Validate MIME type and file size before upload.
- Keep bucket creation/startup checks in config or startup components, not resource methods.
- Keep storage exceptions mapped to user-safe API errors.
- Avoid exposing MinIO credentials or internal object paths.

## Testing Checklist

- Unit tests cover validation and object key generation.
- Resource tests cover upload success, invalid type, oversized file, missing parent entity, and delete behavior.
- Integration tests use Testcontainers or a test MinIO resource when storage behavior matters.

## Example

For album covers, store metadata in `AlbumImage`, upload through `ImageResource`, write storage logic in `ImageService`, and return `ImageUploadResponse` or `ImageUrlResponse`.
