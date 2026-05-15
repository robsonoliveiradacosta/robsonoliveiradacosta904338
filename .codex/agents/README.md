# Repository Agents

These agent definitions are reusable role prompts for building projects with the same architecture, structure, and engineering standards as this repository. Use them as specialized working modes, not as isolated documentation.

## Recommended Flow

1. `architecture-agent`: define scope, entities, APIs, integrations, and risks.
2. `adr-agent`: capture major architecture decisions and tradeoffs.
3. `scaffold-agent`: create or normalize the Quarkus project foundation.
4. `domain-module-agent`: implement each domain aggregate end to end.
5. `data-modeling-agent`: design entities, relationships, constraints, and schema intent.
6. `orm-mapping-agent`: review JPA/Panache mappings, loading, cascades, and DTO boundaries.
7. `migration-safety-agent`: review Flyway migrations for production safety and rollback risk.
8. `transaction-consistency-agent`: design transaction boundaries, consistency, and concurrency controls.
9. `query-optimization-agent`: review repository queries, indexes, pagination, and search paths.
10. `persistence-test-agent`: verify repositories, migrations, constraints, and transactional behavior.
11. `threat-modeling-agent`: identify API threats and required mitigations before implementation.
12. `security-agent`: add or review JWT, RBAC, rate limiting, and secret handling.
13. `secrets-config-agent`: review runtime configuration, profiles, keys, and secret exposure.
14. `supply-chain-security-agent`: audit Maven dependencies, Docker images, CVEs, SBOMs, and licenses.
15. `privacy-compliance-agent`: review PII, LGPD, retention, deletion, and log minimization.
16. `api-governance-agent`: keep REST contracts, versioning, OpenAPI, and compatibility coherent.
17. `error-handling-agent`: standardize exception mapping and client-safe error responses.
18. `resilience-agent`: harden external calls, retries, timeouts, and idempotency.
19. `observability-agent`: add logs, correlation IDs, metrics, tracing, and diagnostics.
20. `database-performance-agent`: review indexes, queries, pagination, and migration impact.
21. `test-data-agent`: maintain deterministic seed data and fixtures.
22. `qa-strategy-agent`: define the QA matrix and test scope for each feature.
23. `api-test-automation-agent`: implement REST Assured resource and contract suites.
24. `integration-test-agent`: stabilize PostgreSQL, MinIO, WireMock, and container-backed tests.
25. `security-qa-agent`: test JWT, RBAC, rate limits, uploads, and OWASP API risks.
26. `performance-qa-agent`: design load tests, thresholds, and performance reports.
27. `flaky-test-agent`: diagnose and fix intermittent test failures.
28. `mutation-testing-agent`: measure and improve assertion strength with mutation testing.
29. `realtime-qa-agent`: test WebSocket and realtime notification behavior.
30. `testing-agent`: add focused unit, resource, integration, and security tests.
31. `ci-cd-agent`: enforce automated quality gates and pipeline checks.
32. `devops-agent`: package runtime, Compose services, env vars, and health checks.
33. `backup-recovery-agent`: plan and verify backup, restore, rollback, and disaster recovery.
34. `sre-runbook-agent`: create incident runbooks and response checklists.
35. `release-manager-agent`: prepare release notes, rollback notes, and go-live evidence.
36. `review-agent`: perform final code, architecture, security, and test review.

## Operating Rules

- Keep each agent scoped to a clear responsibility and file ownership.
- Prefer the repository's established Quarkus patterns over new abstractions.
- Never revert unrelated changes made by another contributor or agent.
- Validate with the smallest useful command first, then broaden before handoff.
- Record assumptions and unresolved questions in the handoff.
