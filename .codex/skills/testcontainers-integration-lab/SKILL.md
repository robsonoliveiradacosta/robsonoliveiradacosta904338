---
name: testcontainers-integration-lab
description: "Design stable integration tests with Testcontainers for Quarkus APIs. Use when testing PostgreSQL, Flyway migrations, MinIO object storage, WireMock-backed external APIs, container lifecycle, isolated test data, ports, networks, or CI-ready infrastructure tests."
---

# Testcontainers Integration Lab

## Goal

Create integration tests that exercise real infrastructure while staying deterministic in local and CI runs.

## Workflow

1. Decide which dependency must be real: PostgreSQL, MinIO, external HTTP service, or multiple services together.
2. Centralize container setup in shared test resources such as `PostgresResource` or `MinioTestResource`.
3. Wire Quarkus test configuration through dynamic properties, not hard-coded localhost ports.
4. Keep data isolated with Flyway clean-at-start, explicit cleanup, or per-test identifiers.
5. Use WaitStrategies and health checks instead of sleeps.
6. Keep container logs available when CI failures occur.

## Stability Rules

- Avoid fixed host ports unless the project requires them.
- Do not share mutable test data across unrelated tests.
- Keep images pinned enough for reproducibility.
- Fail fast when Docker is unavailable and document the requirement.
- Use WireMock for remote APIs instead of calling real services.

## Example

For MinIO upload tests, start a MinIO container, create the bucket during test resource startup, inject the endpoint into Quarkus config, upload a small in-memory image, and assert object metadata plus API response.
