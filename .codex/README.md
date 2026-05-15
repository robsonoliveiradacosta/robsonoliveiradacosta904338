# Codex Toolkit

This directory holds **41 skills** and **36 agent prompts** for this Java 21 Quarkus REST API. Together they cover the lifecycle of a production-minded backend service: project scaffolding, domain modules, PostgreSQL/Flyway persistence, JWT/RBAC security, MinIO storage, external synchronization, observability, resilience, API governance, testing, CI/CD, and release operations.

## How This Is Organized

- `skills/<name>/SKILL.md` - task-specific Codex instructions. Each skill has YAML frontmatter (`name`, `description`) and a focused workflow for when that capability is needed.
- `skills/<name>/agents/openai.yaml` - UI metadata for the skill in Codex-compatible environments.
- `agents/<name>.md` - reusable specialist role prompts. Agents define mission, use cases, owned areas, process, related skills, quality gates, and example prompts.
- `agents/README.md` - the recommended end-to-end agent flow for projects using this architecture.

The skill and agent files are the source of truth. This README is an index and usage guide.

## How To Use

### Skills

Invoke a skill by naming it in the task, for example `$quarkus-domain-module`, or by asking for work that clearly matches its description. Codex should read the matching `SKILL.md` before changing code.

Use skills when you want concrete implementation guidance, scaffolding, test patterns, migration rules, or operational documentation patterns.

### Agents

Use agents as specialist working modes or review prompts. Name the agent explicitly, for example `architecture-agent`, `security-agent`, or `review-agent`, and give it a bounded task with the files or responsibility it should cover.

Use agents when the work benefits from a focused perspective: architecture, persistence, security, testing, DevOps, release readiness, or final review.

---

## Skills - 41 Capability Guides

### Scaffolding, Architecture, And Domain Modules

| Skill | What it covers |
|---|---|
| [`$quarkus-api-bootstrap`](skills/quarkus-api-bootstrap/SKILL.md) | Scaffold or evolve a Java 21 Quarkus REST API with Maven, PostgreSQL, Flyway, Panache, JWT-ready security, MinIO-ready storage, Docker, health, OpenAPI, and tests. |
| [`$quarkus-domain-module`](skills/quarkus-domain-module/SKILL.md) | Implement a complete aggregate across migration, entity, repository, service, REST resource, DTOs, validation, authorization hooks, and tests. |
| [`$architecture-decision-records`](skills/architecture-decision-records/SKILL.md) | Create and maintain ADRs for significant choices around Quarkus, persistence, storage, JWT, Docker, transactions, testing, security, and operations. |

### Persistence, Schema, And Data Integrity

| Skill | What it covers |
|---|---|
| [`$flyway-postgres-schema`](skills/flyway-postgres-schema/SKILL.md) | Design and update PostgreSQL schemas through Flyway migrations using the repository's `VNN__description.sql` convention. |
| [`$postgres-migration-safety`](skills/postgres-migration-safety/SKILL.md) | Plan safe production migrations, including nullable-to-not-null transitions, constraints, indexes, backfills, rollback notes, and zero-downtime compatibility. |
| [`$data-integrity-constraints`](skills/data-integrity-constraints/SKILL.md) | Decide database-backed integrity rules: not-null, unique, foreign keys, checks, cascades, composite keys, and Bean Validation alignment. |
| [`$panache-orm-mapping-patterns`](skills/panache-orm-mapping-patterns/SKILL.md) | Design JPA/Hibernate ORM with Panache mappings, relationships, fetch strategy, cascades, orphan removal, DTO mapping, and N+1 prevention. |
| [`$postgres-query-patterns`](skills/postgres-query-patterns/SKILL.md) | Implement predictable filters, pagination, sorting, joins, counts, text search, existence checks, uniqueness checks, and dynamic repository queries. |
| [`$database-performance-review`](skills/database-performance-review/SKILL.md) | Review PostgreSQL, Flyway, JPA, and Panache performance, including indexes, query plans, N+1 risk, transaction boundaries, and slow endpoint causes. |
| [`$transaction-boundary-design`](skills/transaction-boundary-design/SKILL.md) | Design service transaction boundaries, rollback behavior, consistency, remote call placement, event publication, and atomicity. |
| [`$concurrency-locking-control`](skills/concurrency-locking-control/SKILL.md) | Control concurrent writes with optimistic locking, pessimistic locking, version columns, idempotency, duplicate prevention, and lost-update tests. |
| [`$audit-soft-delete-history`](skills/audit-soft-delete-history/SKILL.md) | Add audit fields, soft delete, history tracking, retention-aware queries, and related migrations. |
| [`$persistence-test-patterns`](skills/persistence-test-patterns/SKILL.md) | Test repositories, ORM behavior, Flyway migrations, constraints, pagination, sorting, relationships, transactions, and Testcontainers-backed PostgreSQL. |
| [`$seed-data-fixtures`](skills/seed-data-fixtures/SKILL.md) | Design deterministic seed data, default users, role fixtures, integration datasets, factory helpers, cleanup strategy, and local demo data. |

### Authentication, Security, Privacy, And Supply Chain

| Skill | What it covers |
|---|---|
| [`$jwt-rbac-auth`](skills/jwt-rbac-auth/SKILL.md) | Implement or extend JWT authentication, token refresh, BCrypt password checks, user roles, `@RolesAllowed`, JWT key configuration, and auth tests. |
| [`$api-security-testing`](skills/api-security-testing/SKILL.md) | Design tests for JWT, RBAC, authorization bypass, token tampering, rate limits, input validation, injection, uploads, sensitive data, CORS, and OWASP API risks. |
| [`$threat-modeling-api-security`](skills/threat-modeling-api-security/SKILL.md) | Threat model REST APIs with OWASP API Top 10 and STRIDE-style thinking before implementation or release. |
| [`$secrets-config-management`](skills/secrets-config-management/SKILL.md) | Review runtime config, profiles, environment variables, JWT keys, database credentials, MinIO secrets, CORS, `.env` files, and secret rotation. |
| [`$dependency-supply-chain-security`](skills/dependency-supply-chain-security/SKILL.md) | Audit Maven dependencies, transitive packages, CVEs, Docker base images, SBOMs, license risk, pinned versions, and CI security gates. |
| [`$privacy-data-retention-lgpd`](skills/privacy-data-retention-lgpd/SKILL.md) | Review personal data, DTO minimization, PII-safe logs, deletion, anonymization, retention periods, audit data, tokens, backups, and LGPD-sensitive behavior. |

### API Contracts, Documentation, And Error Handling

| Skill | What it covers |
|---|---|
| [`$api-docs-openapi-health`](skills/api-docs-openapi-health/SKILL.md) | Add OpenAPI metadata, Swagger UI examples, response schemas, request examples, error responses, health checks, readiness, and liveness probes. |
| [`$api-error-handling`](skills/api-error-handling/SKILL.md) | Standardize exception mappers, validation responses, domain errors, HTTP status policy, error DTOs, safe logging, correlation IDs, and Problem Details compatibility. |
| [`$api-versioning-compatibility`](skills/api-versioning-compatibility/SKILL.md) | Evolve `/api/v1` and future REST versions safely while preserving compatibility and documenting deprecations or breaking-change risk. |
| [`$contract-testing-openapi`](skills/contract-testing-openapi/SKILL.md) | Keep OpenAPI contracts, REST DTOs, examples, and tests aligned to prevent undocumented API drift. |

### Runtime, Integrations, Resilience, And Operations

| Skill | What it covers |
|---|---|
| [`$dockerized-quarkus-runtime`](skills/dockerized-quarkus-runtime/SKILL.md) | Package and run the API with Docker and Compose, including env vars, PostgreSQL/MinIO dependencies, health checks, JVM/native builds, and runtime docs. |
| [`$minio-image-upload`](skills/minio-image-upload/SKILL.md) | Implement MinIO-backed image/file uploads, multipart validation, object names, bucket setup, metadata persistence, presigned URLs, delete flows, health checks, and tests. |
| [`$external-sync-client`](skills/external-sync-client/SKILL.md) | Add external REST API clients and sync workflows with DTOs, scheduled jobs, idempotent upserts, timeouts, WireMock tests, and sync result responses. |
| [`$resilience-timeouts-retries`](skills/resilience-timeouts-retries/SKILL.md) | Harden integrations with timeouts, retries, backoff, circuit breakers, idempotency, fallback behavior, partial failure handling, and retry-safe tests. |
| [`$observability-logging-tracing`](skills/observability-logging-tracing/SKILL.md) | Add structured logs, correlation/request IDs, safe audit events, metrics, tracing, health diagnostics, dependency visibility, and log-level guidance. |
| [`$sre-incident-runbooks`](skills/sre-incident-runbooks/SKILL.md) | Create incident runbooks for API outages, PostgreSQL, MinIO, JWT/key issues, failed migrations, high latency, external failures, and elevated errors. |
| [`$backup-restore-disaster-recovery`](skills/backup-restore-disaster-recovery/SKILL.md) | Plan backup, restore, rollback, and disaster recovery for PostgreSQL, MinIO, migrations, releases, data loss, RPO, and RTO. |
| [`$release-readiness-checklist`](skills/release-readiness-checklist/SKILL.md) | Prepare releases by checking tests, migrations, config, secrets, Docker images, health checks, API compatibility, rollback notes, and production risks. |

### Testing, QA, And Quality Gates

| Skill | What it covers |
|---|---|
| [`$quarkus-test-patterns`](skills/quarkus-test-patterns/SKILL.md) | Design and implement unit, resource, integration, REST Assured, Mockito, WireMock, auth, and Maven test patterns for this architecture. |
| [`$rest-assured-api-suite`](skills/rest-assured-api-suite/SKILL.md) | Build REST Assured suites for resources, auth, JSON payloads, validation errors, pagination, filters, sorting, uploads, headers, rate limits, and response contracts. |
| [`$testcontainers-integration-lab`](skills/testcontainers-integration-lab/SKILL.md) | Design stable integration tests using Testcontainers for PostgreSQL, Flyway, MinIO, WireMock, lifecycle, isolated data, ports, networks, and CI. |
| [`$api-test-strategy-matrix`](skills/api-test-strategy-matrix/SKILL.md) | Create QA matrices across unit, service, resource, integration, contract, security, performance, smoke, regression, and exploratory tests. |
| [`$flaky-test-triage`](skills/flaky-test-triage/SKILL.md) | Diagnose and fix intermittent tests caused by shared data, ordering, clocks, async work, WebSocket timing, Testcontainers startup, fixed ports, races, or cleanup gaps. |
| [`$mutation-testing-quality`](skills/mutation-testing-quality/SKILL.md) | Assess test effectiveness with mutation testing, survived mutants, stronger assertions, exclusions, and practical quality gates. |
| [`$performance-load-testing`](skills/performance-load-testing/SKILL.md) | Define load, stress, and performance tests with k6, Gatling, JMeter, latency targets, p95/p99, throughput, ramp profiles, data, and reports. |
| [`$ci-quality-gates`](skills/ci-quality-gates/SKILL.md) | Design CI gates for Maven tests, integration tests, Docker builds, dependency scanning, formatting checks, migration validation, OpenAPI checks, and coverage evidence. |
| [`$websocket-realtime-testing`](skills/websocket-realtime-testing/SKILL.md) | Test WebSocket connection lifecycle, authorization, subscriptions, emitted messages, payload schema, ordering, reconnects, timeouts, and non-flaky realtime behavior. |

---

## Agents - 36 Specialist Prompts

### Planning And Implementation

| Agent | What it does |
|---|---|
| [`architecture-agent`](agents/architecture-agent.md) | Turns a product idea into an implementation-ready Quarkus API plan. |
| [`adr-agent`](agents/adr-agent.md) | Creates and maintains concise ADRs for important technical choices. |
| [`scaffold-agent`](agents/scaffold-agent.md) | Creates or normalizes the Java 21 Quarkus project foundation. |
| [`domain-module-agent`](agents/domain-module-agent.md) | Implements a domain module across migration, entity, repository, service, resource, DTOs, and tests. |

### Data, Persistence, And Performance

| Agent | What it does |
|---|---|
| [`data-modeling-agent`](agents/data-modeling-agent.md) | Designs domain models with clear entity boundaries, relationships, constraints, and migration intent. |
| [`orm-mapping-agent`](agents/orm-mapping-agent.md) | Reviews JPA/Panache mappings, loading strategy, cascades, and DTO boundaries. |
| [`migration-safety-agent`](agents/migration-safety-agent.md) | Reviews Flyway migrations for production safety, rollback risk, and entity/schema drift. |
| [`transaction-consistency-agent`](agents/transaction-consistency-agent.md) | Designs service transaction boundaries, rollback behavior, and concurrency controls. |
| [`query-optimization-agent`](agents/query-optimization-agent.md) | Keeps repository queries, indexes, pagination, sorting, and joins predictable as data grows. |
| [`database-performance-agent`](agents/database-performance-agent.md) | Protects PostgreSQL schema and query performance as the API grows. |
| [`persistence-test-agent`](agents/persistence-test-agent.md) | Verifies repositories, migrations, constraints, and transactional behavior. |

### Security, Privacy, And API Governance

| Agent | What it does |
|---|---|
| [`threat-modeling-agent`](agents/threat-modeling-agent.md) | Converts realistic API threats into design controls, tests, and release blockers. |
| [`security-agent`](agents/security-agent.md) | Implements and reviews authentication, authorization, rate limiting, and secret handling. |
| [`secrets-config-agent`](agents/secrets-config-agent.md) | Keeps runtime configuration safe, externalized, environment-aware, and free of secret leakage. |
| [`supply-chain-security-agent`](agents/supply-chain-security-agent.md) | Audits dependencies, build tooling, and container images for known risk. |
| [`privacy-compliance-agent`](agents/privacy-compliance-agent.md) | Reviews PII, LGPD, retention, deletion, minimization, and log-safety concerns. |
| [`api-governance-agent`](agents/api-governance-agent.md) | Keeps REST contracts coherent, versioned, documented, and compatible with existing clients. |
| [`error-handling-agent`](agents/error-handling-agent.md) | Standardizes API error behavior for stable client responses and actionable diagnostics. |

### Operations, Resilience, And Release

| Agent | What it does |
|---|---|
| [`resilience-agent`](agents/resilience-agent.md) | Hardens remote calls, background jobs, uploads, and dependency boundaries against transient and partial failures. |
| [`observability-agent`](agents/observability-agent.md) | Adds logs, correlation IDs, metrics, tracing, health diagnostics, and troubleshooting visibility. |
| [`devops-agent`](agents/devops-agent.md) | Prepares Quarkus APIs for reliable local execution and containerized delivery. |
| [`backup-recovery-agent`](agents/backup-recovery-agent.md) | Designs backup, restore, rollback, and disaster recovery practices. |
| [`sre-runbook-agent`](agents/sre-runbook-agent.md) | Creates incident runbooks for common API and infrastructure failures. |
| [`release-manager-agent`](agents/release-manager-agent.md) | Prepares release evidence, risk notes, rollout steps, and rollback options. |
| [`ci-cd-agent`](agents/ci-cd-agent.md) | Maintains automated quality gates for build, test, packaging, security, and release readiness. |

### QA, Test Automation, And Review

| Agent | What it does |
|---|---|
| [`test-data-agent`](agents/test-data-agent.md) | Designs deterministic seed data and fixtures for local development, tests, and API examples. |
| [`qa-strategy-agent`](agents/qa-strategy-agent.md) | Defines the QA matrix and test scope for each feature. |
| [`api-test-automation-agent`](agents/api-test-automation-agent.md) | Implements maintainable REST Assured resource and contract suites. |
| [`integration-test-agent`](agents/integration-test-agent.md) | Builds stable infrastructure-backed tests for databases, object storage, external APIs, and runtime behavior. |
| [`security-qa-agent`](agents/security-qa-agent.md) | Tests API security boundaries, abuse cases, and OWASP-style risks. |
| [`performance-qa-agent`](agents/performance-qa-agent.md) | Designs load tests, performance thresholds, and performance reports. |
| [`flaky-test-agent`](agents/flaky-test-agent.md) | Diagnoses and removes intermittent test failures by finding deterministic root causes. |
| [`mutation-testing-agent`](agents/mutation-testing-agent.md) | Measures and improves assertion strength through mutation testing. |
| [`realtime-qa-agent`](agents/realtime-qa-agent.md) | Tests WebSocket and realtime notification behavior with deterministic synchronization. |
| [`testing-agent`](agents/testing-agent.md) | Adds focused unit, resource, integration, and security tests for Quarkus changes. |
| [`review-agent`](agents/review-agent.md) | Performs final code, architecture, security, maintainability, and test coverage review. |

---

## Typical Workflows

| Goal | Suggested path |
|---|---|
| Plan a large feature | `architecture-agent` -> `$api-test-strategy-matrix` -> `domain-module-agent` |
| Add a new aggregate | `$quarkus-domain-module` -> `$flyway-postgres-schema` -> `$quarkus-test-patterns` -> `review-agent` |
| Add or change persistence | `data-modeling-agent` -> `$data-integrity-constraints` -> `$postgres-migration-safety` -> `migration-safety-agent` |
| Add authentication or RBAC | `threat-modeling-agent` -> `$jwt-rbac-auth` -> `$api-security-testing` -> `security-qa-agent` |
| Add an external sync | `$external-sync-client` -> `$resilience-timeouts-retries` -> `$observability-logging-tracing` -> `integration-test-agent` |
| Add image or file upload | `$minio-image-upload` -> `$api-security-testing` -> `$testcontainers-integration-lab` -> `security-qa-agent` |
| Improve API contract maturity | `api-governance-agent` -> `$api-docs-openapi-health` -> `$contract-testing-openapi` |
| Prepare a release | `release-manager-agent` -> `$release-readiness-checklist` -> `$ci-quality-gates` -> `review-agent` |
| Investigate flaky tests | `flaky-test-agent` -> `$flaky-test-triage` -> `$testcontainers-integration-lab` |
| Improve operations | `devops-agent` -> `$dockerized-quarkus-runtime` -> `$sre-incident-runbooks` -> `$backup-restore-disaster-recovery` |

## Conventions Used Across Skills

- Prefer the repository's existing Quarkus layering: `resource`, `service`, `repository`, `entity`, `dto`, `security`, `health`, `integration`, `scheduler`, and `websocket`.
- Treat tests as part of the deliverable for generated or changed behavior.
- Keep migrations under `src/main/resources/db/migration` with the `VNN__description.sql` pattern.
- Do not expose JPA entities directly as API contracts.
- Keep secrets out of source control, logs, Docker images, and examples that might be copied to production.
- Use Bean Validation at the API boundary and database constraints for data integrity that must hold under concurrency.
- Prefer deterministic test fixtures and isolated test data over shared mutable state.

## Conventions Used Across Agents

- Keep each agent scoped to a clear responsibility and file ownership.
- Prefer concrete findings, affected files, and smallest viable fixes over broad advice.
- Preserve unrelated user or contributor changes.
- Validate with the smallest useful command first, then broaden when shared behavior changed.
- Record assumptions, unresolved questions, and residual risks in handoffs.
- Use `agents/README.md` for the full recommended sequencing of the 36 agents.

## Contributing A New Skill Or Agent

1. For a skill, create `skills/<kebab-name>/SKILL.md` with frontmatter `name` and `description`. Make the description explicit about when the skill should trigger.
2. Add `skills/<kebab-name>/agents/openai.yaml` when UI metadata is needed.
3. For an agent, create `agents/<kebab-name>.md` with sections matching the existing pattern: `Mission`, `Use When`, `Owned Areas`, `Process`, `Skills To Use`, `Quality Gates`, and `Example Prompt`.
4. Add a one-line entry to the right table in this README.
5. Validate new or changed skills with:

```bash
python3 /home/robson/.codex/skills/.system/skill-creator/scripts/quick_validate.py .codex/skills/<skill-name>
```

Keep skill and agent changes focused, and prefer separate commits when both are updated.
