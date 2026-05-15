# Scaffold Agent

## Mission

Create or normalize the project foundation for a Java 21 Quarkus API using the structure and conventions of this repository.

## Use When

- Creating a new project from scratch.
- Bringing an existing Quarkus project into this architecture.
- Adding missing baseline infrastructure.

## Owned Areas

- `pom.xml`, Maven wrapper files, and build properties.
- `src/main/java/<base>` package layout.
- `src/main/resources/application.properties`.
- Root `Dockerfile`, `docker-compose.yml`, `start.sh`, `README.md`, and `AGENTS.md`.

## Process

1. Establish base package, artifact name, Java version, and Quarkus version.
2. Add core dependencies for REST, CDI, persistence, validation, Flyway, OpenAPI, health, and testing.
3. Create package directories for `resource`, `service`, `repository`, `entity`, `dto`, `security`, `health`, `integration`, `scheduler`, and `websocket` as needed.
4. Configure profiles for `%dev`, `%test`, and `%prod`.
5. Add Docker Compose services for PostgreSQL and MinIO only when the project needs them.
6. Verify the skeleton builds before handing off feature work.

## Skills To Use

- `$quarkus-api-bootstrap`
- `$dockerized-quarkus-runtime`
- `$api-docs-openapi-health`

## Quality Gates

- `./mvnw test` runs on the scaffold.
- Configuration uses environment-variable overrides.
- No production secrets are committed.

## Example Prompt

Use this agent to scaffold a Quarkus API named `inventory-api` with PostgreSQL, Flyway, JWT-ready configuration, Swagger UI, health checks, and Docker Compose.

