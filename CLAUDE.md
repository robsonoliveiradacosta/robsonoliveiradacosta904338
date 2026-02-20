# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build and Run Commands

```bash
# Development mode with live reload
./mvnw quarkus:dev

# Run tests
./mvnw test

# Run integration tests
./mvnw verify

# Build package
./mvnw package

# Build native executable (requires GraalVM or container build)
./mvnw package -Dnative
./mvnw package -Dnative -Dquarkus.native.container-build=true
```

## Docker Compose

```bash
# Quick start - Build and run all services (PostgreSQL, MinIO, Application)
./start.sh

# Manual start - Build and run all services
./mvnw package -DskipTests
docker compose up --build -d

# View logs from all services
docker compose logs -f

# View logs from specific service
docker compose logs -f app
docker compose logs -f postgres
docker compose logs -f minio

# Stop all services
docker compose down

# Stop and remove volumes (clean state)
docker compose down -v

# Check services health
curl http://localhost:8080/q/health
curl http://localhost:8080/q/health/live
curl http://localhost:8080/q/health/ready
```

## Docker Builds

```bash
# JVM container
./mvnw package && docker build -f src/main/docker/Dockerfile.jvm -t quarkus/robsonoliveiradacosta904338-jvm .

# Native container (smallest footprint)
./mvnw package -Dnative && docker build -f src/main/docker/Dockerfile.native-micro -t quarkus/robsonoliveiradacosta904338 .
```

## Service URLs (when running with Docker Compose)

- **API**: http://localhost:8080
- **Swagger UI**: http://localhost:8080/q/swagger-ui
- **Health Checks**: http://localhost:8080/q/health
- **MinIO Console**: http://localhost:9001 (credentials: minioadmin/minioadmin)
- **PostgreSQL**: localhost:5432 (database: music_catalog, user: postgres, password: postgres)

## Environment Configuration

Copy `.env.example` to `.env` and customize as needed:

```bash
cp .env.example .env
```

Available environment variables:
- `POSTGRES_DB` - PostgreSQL database name
- `POSTGRES_USER` - PostgreSQL username
- `POSTGRES_PASSWORD` - PostgreSQL password
- `MINIO_ACCESS_KEY` - MinIO access key
- `MINIO_SECRET_KEY` - MinIO secret key
- `MINIO_BUCKET` - MinIO bucket name for album images
- `CORS_ALLOWED_ORIGINS` - Allowed CORS origins
- `REGIONAL_API_URL` - External regional API URL

## Architecture

This is a Quarkus 3.31.1 project using Java 21 with:
- **Quarkus REST** with Jackson for JSON serialization
- **Quarkus ARC** for CDI dependency injection
- **JUnit 5** with Quarkus test extensions

### Project Layout

- `src/main/java/` - Application source code
- `src/main/resources/application.properties` - Configuration
- `src/main/docker/` - Container configurations (JVM, native, native-micro, legacy-jar)
- `src/test/java/` - Test classes

## Quarkus Patterns

- Use `@ApplicationScoped`, `@RequestScoped` for CDI beans
- Use `@Path`, `@GET`, `@POST` etc. for REST endpoints
- Configuration via `application.properties` or `@ConfigProperty`
- Dev mode (`quarkus:dev`) enables live reload and dev services
