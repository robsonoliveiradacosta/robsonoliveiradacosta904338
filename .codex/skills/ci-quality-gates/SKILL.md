---
name: ci-quality-gates
description: "Design CI quality gates for professional Quarkus REST APIs. Use when adding or reviewing pipelines for Maven tests, integration tests, Docker builds, dependency scanning, formatting checks, migration validation, OpenAPI checks, coverage evidence, or pull request automation."
---

# CI Quality Gates

## Goal

Make every pull request prove that the API still builds, tests, packages, and meets baseline quality expectations.

## Recommended Gates

1. Compile and unit tests: `./mvnw test`.
2. Integration verification: `./mvnw verify` when Testcontainers or external stubs are involved.
3. Docker build or `docker compose config` for runtime changes.
4. OpenAPI or contract check when public endpoints change.
5. Dependency/security scan when project policy supports it.
6. Artifact collection for test reports and logs on failure.

## Pipeline Rules

- Run fast checks before expensive integration or Docker jobs.
- Cache Maven dependencies without caching generated target output as truth.
- Keep secrets in CI secret storage, not repository files.
- Use service containers or Testcontainers consistently.
- Fail the pipeline on test failure, compilation warnings elevated by policy, or invalid Compose config.

## Pull Request Evidence

Ask contributors to include commands run, key test output, migration impact, API changes, and deployment considerations.

## Example

For a PR changing MinIO upload behavior, require targeted upload tests, `./mvnw test`, Compose validation, and evidence that oversized and invalid MIME uploads still fail.
