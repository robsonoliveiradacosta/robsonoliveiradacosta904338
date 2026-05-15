# Claude Code Toolkit

This directory holds **31 skills** and **14 review agents** purpose-built for this Quarkus REST API. Together they cover the lifecycle of a professional Java backend service: scaffolding, persistence, security, observability, resilience, testing, CI/CD, and API maturity.

## How this is organized

- `skills/<name>/SKILL.md` — invokable code-generators / scaffolds. Each `SKILL.md` carries YAML frontmatter (`name`, `description`) plus inline templates and anti-pattern guidance. Invoke as `/<skill-name>` or via the `Skill` tool.
- `agents/<name>.md` — review agents (read-only specialists). Each one targets a specific failure mode. Invoke via the `Agent` tool with `subagent_type: <agent-name>`.
- `commands/*.md` — pre-existing slash commands (`criar-prd`, `criar-tasks`, etc.).
- `rules/*.md` — coding-rule files loaded as project context.

> Every skill description starts with **what it does** and ends with **when to invoke**. Every agent description starts with **what it audits** and ends with **when to use**. Read the frontmatter — that's the source of truth.

---

## Skills — 31 generators / scaffolds

### Scaffolding & CRUD

| Skill | What it does |
|---|---|
| [`/bootstrap-quarkus-rest`](skills/bootstrap-quarkus-rest/SKILL.md) | Scaffolds a new Quarkus 3.x project with this repo's layered structure, Dockerfile, compose, application.properties |
| [`/add-crud-resource`](skills/add-crud-resource/SKILL.md) | Generates a full CRUD slice in one pass: entity, repository, service, resource, DTOs, migration, tests |
| [`/add-flyway-migration`](skills/add-flyway-migration/SKILL.md) | Next-numbered `Vn__*.sql` with templates per change type (create / alter / junction / index / seed) |
| [`/add-testcontainers-resource`](skills/add-testcontainers-resource/SKILL.md) | `QuarkusTestResourceLifecycleManager` for Postgres / MinIO / Redis / Kafka |
| [`/add-pagination`](skills/add-pagination/SKILL.md) | Converts a resource to the `PageResponse<T>` pattern with sort allowlist + size cap |

### Auth & Security

| Skill | What it does |
|---|---|
| [`/add-jwt-auth`](skills/add-jwt-auth/SKILL.md) | SmallRye JWT (RS256) — keys, `TokenService`, `AuthResource`, BCrypt users migration, `TestTokenHelper` |
| [`/add-rate-limit`](skills/add-rate-limit/SKILL.md) | Per-principal `RateLimitFilter` via bucket4j with `app.rate-limit.enabled` flag |
| [`/add-idempotency-key`](skills/add-idempotency-key/SKILL.md) | Stripe-style `Idempotency-Key` header with `IN_FLIGHT` race protection + body fingerprint |

### Persistence & ORM

| Skill | What it does |
|---|---|
| [`/add-optimistic-locking`](skills/add-optimistic-locking/SKILL.md) | `@Version` + 409 mapper + early-detect pattern (no auto-retry of user updates) |
| [`/add-soft-delete`](skills/add-soft-delete/SKILL.md) | `@SoftDelete` (Hibernate 6.4+) with partial unique indexes + restore endpoint |
| [`/add-audit-trail`](skills/add-audit-trail/SKILL.md) | `created_at` / `updated_at` / `created_by` via `AuditableEntity` superclass + listener |
| [`/add-purge-job`](skills/add-purge-job/SKILL.md) | Companion to soft-delete — scheduled `LIMIT`-batched hard delete with retention config |
| [`/add-jsonb-column`](skills/add-jsonb-column/SKILL.md) | PostgreSQL JSONB typed via `@JdbcTypeCode` + GIN with `jsonb_path_ops` |
| [`/add-bulk-operations`](skills/add-bulk-operations/SKILL.md) | Tiered: batched persists, JPQL `@Modifying`, native escape hatch — with lifecycle caveats |
| [`/add-multi-tenancy`](skills/add-multi-tenancy/SKILL.md) | Row-level `@TenantId` via JWT claim with mandatory isolation test |
| [`/add-outbox-pattern`](skills/add-outbox-pattern/SKILL.md) | Transactional outbox: partial index, `FOR UPDATE SKIP LOCKED`, exponential backoff, parking |

### Object Storage & Integration

| Skill | What it does |
|---|---|
| [`/add-minio-storage`](skills/add-minio-storage/SKILL.md) | quarkus-minio extension, bucket auto-create, 30-minute presigned URLs, health check |
| [`/add-scheduled-rest-client`](skills/add-scheduled-rest-client/SKILL.md) | MicroProfile REST Client + `@Scheduled` cron sync + WireMock-stubbed test |
| [`/add-websocket-broadcast`](skills/add-websocket-broadcast/SKILL.md) | Quarkus websockets-next with static `BroadcastProcessor` pattern |

### Observability, Errors & Resilience

| Skill | What it does |
|---|---|
| [`/add-observability`](skills/add-observability/SKILL.md) | Micrometer + Prometheus + OpenTelemetry + JSON logs + `X-Request-Id` filter |
| [`/add-error-handling`](skills/add-error-handling/SKILL.md) | RFC 7807 Problem Details mappers with stack-trace-safe fallback |
| [`/add-fault-tolerance`](skills/add-fault-tolerance/SKILL.md) | `@Retry` + `@CircuitBreaker` + `@Timeout` + `@Fallback` on integrations |
| [`/add-cache`](skills/add-cache/SKILL.md) | `@CacheResult` / `@CacheInvalidate` (Caffeine in-process + Redis distributed) |

### API Maturity

| Skill | What it does |
|---|---|
| [`/add-api-versioning`](skills/add-api-versioning/SKILL.md) | URL-path strategy + RFC 8594 `Sunset` + `Deprecation` headers + policy doc |
| [`/add-openapi-client-gen`](skills/add-openapi-client-gen/SKILL.md) | TypeScript / Java SDK generation from OpenAPI + drift check in CI |

### QA & Testing

| Skill | What it does |
|---|---|
| [`/add-jacoco-coverage`](skills/add-jacoco-coverage/SKILL.md) | JaCoCo with per-package thresholds + argLine fix for existing surefire config |
| [`/add-mutation-testing`](skills/add-mutation-testing/SKILL.md) | Pitest `STRONGER` mutators with incremental history + nightly + per-PR modes |
| [`/add-load-testing`](skills/add-load-testing/SKILL.md) | k6 smoke / load / spike scenarios with baseline-comparison CI job |
| [`/add-test-data-builders`](skills/add-test-data-builders/SKILL.md) | Fluent `<Entity>TestBuilder` with counter-based unique defaults |
| [`/add-pact-contract-tests`](skills/add-pact-contract-tests/SKILL.md) | Provider-side Pact verifier with state handlers (only if consumers publish) |

### CI/CD & Tooling

| Skill | What it does |
|---|---|
| [`/add-ci-pipeline`](skills/add-ci-pipeline/SKILL.md) | GitHub Actions: JVM + native matrix, Trivy SARIF, CycloneDX SBOM, Dependabot |

---

## Agents — 14 specialized reviewers

### Architecture & Implementation

| Agent | What it audits |
|---|---|
| [`quarkus-architect`](agents/quarkus-architect.md) | Designs cross-file implementation plans (file list, migration number, role mapping, skill mapping) before code is written |

### Security & Compliance

| Agent | What it audits |
|---|---|
| [`quarkus-security-reviewer`](agents/quarkus-security-reviewer.md) | Missing `@RolesAllowed`, JWT key handling, SQL injection in Panache, leaked entity fields, CORS |
| [`logging-and-pii-reviewer`](agents/logging-and-pii-reviewer.md) | Password/CPF/token in logs, `printStackTrace`, MDC leakage via missing `try/finally`, hot-loop logging |
| [`dependency-vulnerability-reviewer`](agents/dependency-vulnerability-reviewer.md) | CVE scan (Trivy / OSV) over `pom.xml` + transitives with smallest-safe upgrade recommendations |

### Persistence & Performance

| Agent | What it audits |
|---|---|
| [`flyway-migration-reviewer`](agents/flyway-migration-reviewer.md) | Numbering, idempotency, edits to applied migrations, NOT NULL on populated tables, entity-SQL drift |
| [`database-query-reviewer`](agents/database-query-reviewer.md) | Leading-wildcard LIKE, missing compound indexes, unbounded result sets, JSONB without GIN |
| [`transaction-boundary-reviewer`](agents/transaction-boundary-reviewer.md) | `@Transactional` on private/static (silent no-op), missing on persist, sync HTTP-in-tx, self-call |
| [`quarkus-performance-reviewer`](agents/quarkus-performance-reviewer.md) | N+1 from lazy fetch projected into DTOs, pool sizing, blocking calls in reactive paths |

### Error & Test Quality

| Agent | What it audits |
|---|---|
| [`error-handling-reviewer`](agents/error-handling-reviewer.md) | Swallowed catches returning 200, leaked exception messages, missing `@Valid`, mapper priority collisions |
| [`quarkus-test-coverage`](agents/quarkus-test-coverage.md) | Structural gaps: missing test files, missing 401/403/happy/404 cases per resource, missing WireMock stubs |
| [`test-quality-reviewer`](agents/test-quality-reviewer.md) | Tautological tests, `assertNotNull`-only assertions, `@Disabled` without tickets, status-only REST Assured |
| [`flaky-test-detector`](agents/flaky-test-detector.md) | Parses GitHub Actions JUnit XML history, computes failure rate, categorizes cause (timing / order / env) |

### API Contract & Design

| Agent | What it audits |
|---|---|
| [`api-contract-reviewer`](agents/api-contract-reviewer.md) | Diffs current `/q/openapi` vs `main` to catch breaking changes before merge |
| [`api-design-reviewer`](agents/api-design-reviewer.md) | Wrong verbs / status codes, missing `Location` header, naming inconsistency, missing idempotency on mutations |

---

## How to use

### Discovery
- **As a developer**: read this index, click into the skill/agent SKILL.md for full template and guidance.
- **Inside Claude Code**: skills/agents auto-load; the harness lists them under "available skills" automatically. No CLAUDE.md duplication needed.

### Typical workflows

| Goal | Path |
|---|---|
| New endpoint | `/add-crud-resource` → maybe `/add-pagination` → review with `api-design-reviewer` |
| New external integration | `/add-scheduled-rest-client` → `/add-fault-tolerance` → `/add-outbox-pattern` if events flow back |
| Performance investigation | `quarkus-performance-reviewer` → `database-query-reviewer` → maybe `/add-cache` |
| Pre-release sweep | `quarkus-security-reviewer` + `dependency-vulnerability-reviewer` + `api-contract-reviewer` |
| Test debt cleanup | `quarkus-test-coverage` → `test-quality-reviewer` → `/add-mutation-testing` once green |

### Composability

Most skills are designed to **chain**. Each one's "Post-generation" section recommends the natural next step (e.g. `/add-soft-delete` recommends `/add-purge-job`; `/add-jwt-auth` is the prerequisite for resource role tests). Read the post-generation notes — they save discovering this through trial and error.

---

## Conventions used across all skills

- **Anti-patterns are listed in the skill itself**, with "refuse to do X" rules. A skill won't generate something it considers harmful (e.g. PEM in git, `@Transactional` on private method, password logging).
- **Templates substitute `{{packageRoot}}`, `{{packagePath}}`, `{{artifactId}}`** — substitute at invocation time, not hardcoded.
- **Tests are required output**, not optional. Every skill that generates code generates the corresponding test pattern.
- **`%test.<flag>=false`** is added wherever the new behavior would break tests (rate limiter, cache, scheduler).

## Conventions used across all agents

- **Severity-graded output**: P0 (block merge) / P1 (should fix) / P2 (informational).
- **Confidence percentage** on every finding — below 50% isn't reported.
- **Snippet + line + smallest fix** for every finding — no abstract advice.
- **Hard rules section** in each agent prevents over-stepping (e.g. agents don't rewrite code, don't propose new endpoints, don't edit applied migrations).

---

## Contributing a new skill or agent

The pattern is consistent across all 45 files:

1. **Skill**: create `skills/<kebab-name>/SKILL.md` with frontmatter `name` + `description` (description must be "pushy" — tell Claude exactly when to trigger). Body sections: **When to invoke** / **When NOT to invoke** / **Inputs** / **Files to generate** / **Anti-patterns** / **Post-generation**.
2. **Agent**: create `agents/<kebab-name>.md` with frontmatter `name` + `description` + `tools` + `model` + `color`. Body sections: **Scope** / **Checks (P0/P1/P2)** / **How to find things efficiently** (with grep commands) / **Output format** / **Hard rules**.
3. Add a one-line entry to the appropriate table in this README.
4. Commit.

For Anthropic's `skill-creator` plugin patterns and conventions, see `~/.claude/plugins/marketplaces/claude-plugins-official/plugins/skill-creator/`.
