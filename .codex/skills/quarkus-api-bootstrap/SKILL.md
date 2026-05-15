---
name: quarkus-api-bootstrap
description: "Scaffold and evolve Java 21 Quarkus REST APIs that follow this repository's architecture. Use when creating a new backend project with Quarkus, Maven wrapper, PostgreSQL, Flyway, Panache, JWT-ready security, MinIO-ready storage, Docker Compose, health checks, OpenAPI, and test foundations."
---

# Quarkus API Bootstrap

## Goal

Create a production-oriented Quarkus API skeleton that matches this repository's structure and defaults. Prefer the existing architecture over generic Quarkus examples.

## Workflow

1. Confirm the domain name, base package, database name, and required integrations.
2. Create a Maven wrapper project targeting Java 21 and Quarkus 3.x.
3. Add core dependencies: REST Jackson, Arc, Hibernate ORM Panache, PostgreSQL JDBC, Flyway, Hibernate Validator, SmallRye OpenAPI, SmallRye Health, and JUnit 5 test support.
4. Add optional integrations only when needed: SmallRye JWT, MinIO, REST Client Jackson, Scheduler, WebSocket, Bucket4j, Testcontainers, Mockito, REST Assured, and WireMock.
5. Create the standard package layout:

```text
src/main/java/<base>/{config,dto/request,dto/response,entity,exception,health,integration,repository,resource,scheduler,security,service,websocket}
src/main/resources/db/migration
src/test/java/<base>/{common,resource,security,service,util}
```

6. Add `application.properties` with environment-variable overrides for database, CORS, JWT, MinIO, external APIs, OpenAPI, health, logging, and `%dev`, `%test`, `%prod` profiles.
7. Add Docker assets: root `Dockerfile`, `docker-compose.yml`, and Quarkus Dockerfiles under `src/main/docker`.
8. Add `README.md`, `AGENTS.md`, and a small `start.sh` when the stack needs multiple services.

## Baseline Conventions

- Use package names in lowercase and avoid framework code outside the base package.
- Keep REST resources thin; move business rules into services.
- Use Flyway for schema changes. Do not rely on Hibernate auto-DDL outside throwaway experiments.
- Make configuration externalized with safe local defaults.
- Ensure `/q/swagger-ui` and `/q/health` work in local development.

## Validation

Run `./mvnw test` for the generated project. Run `docker compose config` and `docker compose up -d postgres minio` when Docker services are included.

## Example

User request: "Create a catalog API for books and authors with auth, PostgreSQL, Docker Compose, Flyway, OpenAPI, and tests."

Expected output: a Quarkus project with `BookResource`, `BookService`, `BookRepository`, `Book`, request/response DTOs, Flyway migrations, test scaffolding, Docker services, and documented local commands.
