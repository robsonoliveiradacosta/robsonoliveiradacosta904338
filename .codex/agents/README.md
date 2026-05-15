# Repository Agents

These agent definitions are reusable role prompts for building projects with the same architecture, structure, and engineering standards as this repository. Use them as specialized working modes, not as isolated documentation.

## Recommended Flow

1. `architecture-agent`: define scope, entities, APIs, integrations, and risks.
2. `scaffold-agent`: create or normalize the Quarkus project foundation.
3. `domain-module-agent`: implement each domain aggregate end to end.
4. `data-modeling-agent`: design entities, relationships, constraints, and schema intent.
5. `orm-mapping-agent`: review JPA/Panache mappings, loading, cascades, and DTO boundaries.
6. `migration-safety-agent`: review Flyway migrations for production safety and rollback risk.
7. `transaction-consistency-agent`: design transaction boundaries, consistency, and concurrency controls.
8. `query-optimization-agent`: review repository queries, indexes, pagination, and search paths.
9. `persistence-test-agent`: verify repositories, migrations, constraints, and transactional behavior.
10. `security-agent`: add or review JWT, RBAC, rate limiting, and secret handling.
11. `api-governance-agent`: keep REST contracts, versioning, OpenAPI, and compatibility coherent.
12. `error-handling-agent`: standardize exception mapping and client-safe error responses.
13. `resilience-agent`: harden external calls, retries, timeouts, and idempotency.
14. `observability-agent`: add logs, correlation IDs, metrics, tracing, and diagnostics.
15. `database-performance-agent`: review indexes, queries, pagination, and migration impact.
16. `test-data-agent`: maintain deterministic seed data and fixtures.
17. `qa-strategy-agent`: define the QA matrix and test scope for each feature.
18. `api-test-automation-agent`: implement REST Assured resource and contract suites.
19. `integration-test-agent`: stabilize PostgreSQL, MinIO, WireMock, and container-backed tests.
20. `security-qa-agent`: test JWT, RBAC, rate limits, uploads, and OWASP API risks.
21. `performance-qa-agent`: design load tests, thresholds, and performance reports.
22. `flaky-test-agent`: diagnose and fix intermittent test failures.
23. `mutation-testing-agent`: measure and improve assertion strength with mutation testing.
24. `realtime-qa-agent`: test WebSocket and realtime notification behavior.
25. `testing-agent`: add focused unit, resource, integration, and security tests.
26. `ci-cd-agent`: enforce automated quality gates and pipeline checks.
27. `devops-agent`: package runtime, Compose services, env vars, and health checks.
28. `release-manager-agent`: prepare release notes, rollback notes, and go-live evidence.
29. `review-agent`: perform final code, architecture, security, and test review.

## Operating Rules

- Keep each agent scoped to a clear responsibility and file ownership.
- Prefer the repository's established Quarkus patterns over new abstractions.
- Never revert unrelated changes made by another contributor or agent.
- Validate with the smallest useful command first, then broaden before handoff.
- Record assumptions and unresolved questions in the handoff.
