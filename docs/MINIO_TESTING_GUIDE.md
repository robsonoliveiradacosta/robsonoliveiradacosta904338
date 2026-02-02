# MinIO Integration - Manual Testing Guide

This guide provides instructions for manually testing the MinIO integration for album image uploads.

## Prerequisites

1. Start the application with MinIO:
```bash
docker-compose up -d minio postgres
./mvnw quarkus:dev
```

2. Get authentication token:
```bash
# Admin token
ADMIN_TOKEN=$(curl -s -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}' | jq -r '.accessToken')

# User token
USER_TOKEN=$(curl -s -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"user","password":"user123"}' | jq -r '.accessToken')
```

## Test Scenarios

### 1. Upload Image to Album

```bash
# Create a test image file
echo "test image content" > test-image.jpg

# Upload image (requires ADMIN role)
curl -X POST http://localhost:8080/api/v1/albums/1/images \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -F "file=@test-image.jpg"

# Expected response:
# {
#   "imageKey": "1/uuid_test-image.jpg",
#   "message": "Image uploaded successfully"
# }
```

### 2. Get Presigned URL for Image

```bash
# Replace IMAGE_KEY with the imageKey from upload response (just the UUID_filename part)
IMAGE_KEY="uuid_test-image.jpg"

# Get presigned URL (available to USER and ADMIN)
curl -X GET "http://localhost:8080/api/v1/albums/1/images/$IMAGE_KEY" \
  -H "Authorization: Bearer $USER_TOKEN"

# Expected response:
# {
#   "imageKey": "1/uuid_test-image.jpg",
#   "url": "http://localhost:9000/album-images/1/uuid_test-image.jpg?X-Amz-...",
#   "expiresInMinutes": 30
# }
```

### 3. Access Image via Presigned URL

```bash
# Copy the URL from the previous response and access it in a browser or curl
curl "http://localhost:9000/album-images/1/uuid_test-image.jpg?X-Amz-..." \
  -o downloaded-image.jpg

# The image should download without authentication
```

### 4. Delete Image

```bash
# Delete image (requires ADMIN role)
IMAGE_KEY="uuid_test-image.jpg"

curl -X DELETE "http://localhost:8080/api/v1/albums/1/images/$IMAGE_KEY" \
  -H "Authorization: Bearer $ADMIN_TOKEN"

# Expected response: 204 No Content
```

### 5. Validation Tests

#### Test Invalid Content Type

```bash
# Create a PDF file
echo "fake pdf" > test-file.pdf

# Try to upload (should fail with 400)
curl -X POST http://localhost:8080/api/v1/albums/1/images \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -F "file=@test-file.pdf"

# Expected: 400 Bad Request with error message about invalid content type
```

#### Test Large File (>50MB)

```bash
# Create a large file (51MB)
dd if=/dev/zero of=large-file.jpg bs=1M count=51

# Try to upload (should fail with 400)
curl -X POST http://localhost:8080/api/v1/albums/1/images \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -F "file=@large-file.jpg"

# Expected: 400 Bad Request with error message about file size
```

#### Test Authorization

```bash
# Try to upload as USER (should fail with 403)
curl -X POST http://localhost:8080/api/v1/albums/1/images \
  -H "Authorization: Bearer $USER_TOKEN" \
  -F "file=@test-image.jpg"

# Expected: 403 Forbidden

# Try to delete as USER (should fail with 403)
curl -X DELETE "http://localhost:8080/api/v1/albums/1/images/$IMAGE_KEY" \
  -H "Authorization: Bearer $USER_TOKEN"

# Expected: 403 Forbidden
```

### 6. Multiple Images per Album

```bash
# Upload multiple images to the same album
curl -X POST http://localhost:8080/api/v1/albums/1/images \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -F "file=@image1.jpg"

curl -X POST http://localhost:8080/api/v1/albums/1/images \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -F "file=@image2.png"

curl -X POST http://localhost:8080/api/v1/albums/1/images \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -F "file=@image3.webp"

# Verify all images are stored
curl -X GET http://localhost:8080/api/v1/albums/1 \
  -H "Authorization: Bearer $USER_TOKEN"

# The album response should include all imageKeys
```

### 7. Verify MinIO Bucket

Access MinIO Console:
- URL: http://localhost:9001
- Username: minioadmin
- Password: minioadmin

Navigate to the `album-images` bucket and verify:
- Images are organized by album ID (folder structure: `1/`, `2/`, etc.)
- Each image has a unique UUID prefix
- Files are stored with correct content types

## OpenAPI/Swagger UI

Access the interactive API documentation:
- URL: http://localhost:8080/swagger-ui
- Navigate to "Images" tag
- Test endpoints directly from the UI

## Health Check

Verify MinIO connectivity:
```bash
curl http://localhost:8080/health
```

The health check should show MinIO as healthy if the connection is working.

## Troubleshooting

### MinIO Connection Issues

If you see "MinIO service unavailable" errors:

1. Check if MinIO is running:
```bash
docker ps | grep minio
```

2. Verify MinIO configuration in `application.properties`:
```properties
quarkus.minio.url=http://localhost:9000
quarkus.minio.access-key=minioadmin
quarkus.minio.secret-key=minioadmin
```

3. Check MinIO logs:
```bash
docker logs minio
```

### Bucket Not Found

If the bucket doesn't exist, restart the application. The `MinioStartup` class automatically creates the bucket on startup.

### Presigned URL Expired

Presigned URLs are valid for 30 minutes. If expired, generate a new URL by calling the GET endpoint again.
