# Repository Agents

These agent definitions are reusable role prompts for building projects with the same architecture, structure, and engineering standards as this repository. Use them as specialized working modes, not as isolated documentation.

## Recommended Flow

1. `architecture-agent`: define scope, entities, APIs, integrations, and risks.
2. `scaffold-agent`: create or normalize the Quarkus project foundation.
3. `domain-module-agent`: implement each domain aggregate end to end.
4. `security-agent`: add or review JWT, RBAC, rate limiting, and secret handling.
5. `api-governance-agent`: keep REST contracts, versioning, OpenAPI, and compatibility coherent.
6. `error-handling-agent`: standardize exception mapping and client-safe error responses.
7. `resilience-agent`: harden external calls, retries, timeouts, and idempotency.
8. `observability-agent`: add logs, correlation IDs, metrics, tracing, and diagnostics.
9. `database-performance-agent`: review indexes, queries, pagination, and migration impact.
10. `test-data-agent`: maintain deterministic seed data and fixtures.
11. `qa-strategy-agent`: define the QA matrix and test scope for each feature.
12. `api-test-automation-agent`: implement REST Assured resource and contract suites.
13. `integration-test-agent`: stabilize PostgreSQL, MinIO, WireMock, and container-backed tests.
14. `security-qa-agent`: test JWT, RBAC, rate limits, uploads, and OWASP API risks.
15. `performance-qa-agent`: design load tests, thresholds, and performance reports.
16. `flaky-test-agent`: diagnose and fix intermittent test failures.
17. `mutation-testing-agent`: measure and improve assertion strength with mutation testing.
18. `realtime-qa-agent`: test WebSocket and realtime notification behavior.
19. `testing-agent`: add focused unit, resource, integration, and security tests.
20. `ci-cd-agent`: enforce automated quality gates and pipeline checks.
21. `devops-agent`: package runtime, Compose services, env vars, and health checks.
22. `release-manager-agent`: prepare release notes, rollback notes, and go-live evidence.
23. `review-agent`: perform final code, architecture, security, and test review.

## Operating Rules

- Keep each agent scoped to a clear responsibility and file ownership.
- Prefer the repository's established Quarkus patterns over new abstractions.
- Never revert unrelated changes made by another contributor or agent.
- Validate with the smallest useful command first, then broaden before handoff.
- Record assumptions and unresolved questions in the handoff.
