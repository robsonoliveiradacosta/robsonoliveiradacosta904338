# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build and Run Commands

```bash
# Development mode with live reload
./mvnw quarkus:dev

# Run all tests (uses Testcontainers — requires Docker)
./mvnw test

# Run a single test class
./mvnw test -Dtest=AlbumResourceTest

# Run integration tests
./mvnw verify

# Build package
./mvnw package -DskipTests
```

## Docker Compose

```bash
# Quick start — builds and runs all services (PostgreSQL, MinIO, Application)
./start.sh

# Manual start
./mvnw package -DskipTests && docker compose up --build -d

# View logs
docker compose logs -f [app|postgres|minio]

# Stop and remove volumes (clean state)
docker compose down -v
```

## Service URLs (when running with Docker Compose)

- **API**: http://localhost:8080
- **Swagger UI**: http://localhost:8080/q/swagger-ui
- **Health Checks**: http://localhost:8080/q/health
- **MinIO Console**: http://localhost:9001 (credentials: minioadmin/minioadmin)
- **PostgreSQL**: localhost:5432 (database: music_catalog, user: postgres, password: postgres)

## Environment Configuration

```bash
cp .env.example .env
```

Key environment variables: `POSTGRES_DB`, `POSTGRES_USER`, `POSTGRES_PASSWORD`, `MINIO_ACCESS_KEY`, `MINIO_SECRET_KEY`, `MINIO_BUCKET`, `CORS_ALLOWED_ORIGINS`, `REGIONAL_API_URL`.

## Architecture

Quarkus 3.31.1 REST API (Java 21) for a music catalog — artists, albums, images, and regional data.

### Package structure (`com.quarkus`)

| Package | Responsibility |
|---|---|
| `resource` | JAX-RS endpoints (`/api/v1/*`) |
| `service` | Business logic, `@Transactional` methods |
| `repository` | Panache repositories extending `PanacheRepository<T>` |
| `entity` | JPA entities (`Artist`, `Album`, `AlbumImage`, `User`, `Regional`) |
| `dto` | `request/` for inputs, `response/` for outputs |
| `security` | `TokenService` (JWT generation), `RateLimitFilter` (Bucket4j) |
| `scheduler` | `RegionalSyncScheduler` — daily cron at 04:00 syncing regional data from external API |
| `integration` | `RegionalApiClient` — MicroProfile REST Client for external regional API |
| `websocket` | `AlbumNotificationSocket` — notifies connected clients on album create via WebSocket |
| `health` | Custom MicroProfile Health checks for DB and MinIO |
| `config` | `MinioStartup` — creates MinIO bucket on startup if missing |

### Key domain relationships

- `Artist` has a `type` (`SINGER` or `BAND`) and belongs to a `Regional`
- `Album` has a many-to-many with `Artist` (junction table `album_artist`)
- `AlbumImage` stores image metadata (key, content type, size) referencing an `Album`; actual files are in MinIO
- `User` has a `UserRole` (`USER` or `ADMIN`); passwords stored as BCrypt hashes

### Security model

- JWT authentication via `quarkus-smallrye-jwt`; tokens signed with RSA keys (`privateKey.pem`/`publicKey.pem`) in resources
- `@RolesAllowed("USER")` — read endpoints; `@RolesAllowed("ADMIN")` — write/delete endpoints
- Rate limiting applied via a `ContainerRequestFilter` using Bucket4j (disabled in test profile via `%test.app.rate-limit.enabled=false`)

### Database migrations

Flyway migrations in `src/main/resources/db/migration/`, versioned V1–V10:
- V1–V3: core schema (artists, albums, junction table)
- V4–V5: album images and users tables
- V6: regionals table
- V7–V9: sample data inserts
- V10: alters album_images table

### Testing

Tests use `@QuarkusTest` with Testcontainers (real PostgreSQL via `PostgresResource`, real MinIO via `MinioTestResource`) and WireMock for the external regional API. Use `TestTokenHelper` to generate signed JWT tokens in tests:

```java
.auth().oauth2(TestTokenHelper.generateAdminToken())
.auth().oauth2(TestTokenHelper.generateUserToken())
```

Unit tests (service layer) use Mockito via `@InjectMock`.
