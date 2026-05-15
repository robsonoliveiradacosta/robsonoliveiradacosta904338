# Repository Guidelines

## Project Structure & Module Organization

This is a Java 21 Quarkus API for a music catalog. Application code lives under `src/main/java/com/quarkus`, organized by layer: `resource` for REST endpoints, `service` for business logic, `repository` for Panache persistence, `entity` for JPA models, `dto` for request/response contracts, plus `security`, `health`, `integration`, `scheduler`, and `websocket`. Runtime configuration is in `src/main/resources/application.properties`; Flyway migrations are in `src/main/resources/db/migration` using `V<number>__description.sql`. Tests mirror the main packages in `src/test/java/com/quarkus`. Docker assets are in `Dockerfile`, `docker-compose.yml`, and `src/main/docker/`.

## Build, Test, and Development Commands

- `docker compose up -d postgres minio`: start local infrastructure.
- `./mvnw quarkus:dev`: run the API with Quarkus live reload at `http://localhost:8080`.
- `./mvnw test`: run unit and Quarkus tests.
- `./mvnw verify`: run the full Maven verification lifecycle, including integration tests.
- `./mvnw test -Dtest=AlbumResourceTest`: run one test class.
- `./mvnw package`: build the JVM artifact in `target/quarkus-app/`.
- `./start.sh`: build and start the full Docker Compose stack.

## Coding Style & Naming Conventions

Use UTF-8 Java source with 4-space indentation and standard Quarkus/Jakarta conventions. Keep package names lowercase under `com.quarkus`. Name REST classes `*Resource`, services `*Service`, repositories `*Repository`, DTOs `*Request` or `*Response`, and entities as singular domain nouns. Prefer local CDI injection patterns, Bean Validation for input constraints, and Panache repository APIs.

## Testing Guidelines

Tests use JUnit 5, Quarkus test support, REST Assured, Mockito, Testcontainers, and WireMock. Name test classes `*Test` and place shared helpers in `src/test/java/com/quarkus/common` or `util`. Cover new endpoints at the resource level and business rules at the service level. No coverage threshold is enforced, but meaningful tests are expected for new behavior and fixes.

## Security & Configuration Tips

Do not commit real credentials or production keys. Local defaults are configured through environment variables such as `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `MINIO_URL`, `MINIO_ACCESS_KEY`, `MINIO_SECRET_KEY`, and `REGIONAL_API_URL`. JWT keys are read from `src/main/resources/privateKey.pem` and `publicKey.pem`; regenerate local keys when needed instead of sharing secrets.

## Commit & Pull Request Guidelines

Recent history uses concise imperative subjects, for example `Add Docker Compose setup`, `Update project name`, and `Implement regional synchronization feature`. Follow that style and keep commits focused. Pull requests should include a short purpose statement, linked issue or task when applicable, test evidence such as `./mvnw test` or `./mvnw verify`, and API examples or screenshots when endpoint behavior changes.
