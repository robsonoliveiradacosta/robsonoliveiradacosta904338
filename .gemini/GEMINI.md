# Project context for Gemini CLI

Skills, agents and slash commands for this Quarkus 21 / PostgreSQL / MinIO project are exposed as Gemini custom commands under `.gemini/commands/`.

Invocation:

- `/skills:<name>` — execute a skill (action recipe or governance guide).
- `/agents:<name>` — adopt a specialist agent persona.
- `/commands:<name>` — run an existing slash command (PRD, tasks, techspec, …).

Pass extra request context with the standard `{{args}}` slot — the command prompt already references it.

## Available skills (53)
`/skills:add-api-versioning`, `/skills:add-audit-trail`, `/skills:add-bulk-operations`, `/skills:add-cache`, `/skills:add-ci-pipeline`, `/skills:add-crud-resource`, `/skills:add-error-handling`, `/skills:add-fault-tolerance`, `/skills:add-flyway-migration`, `/skills:add-idempotency-key`, `/skills:add-jsonb-column`, `/skills:add-jwt-auth`, `/skills:add-load-testing`, `/skills:add-minio-storage`, `/skills:add-multi-tenancy`, `/skills:add-mutation-testing`, `/skills:add-observability`, `/skills:add-openapi-client-gen`, `/skills:add-optimistic-locking`, `/skills:add-outbox-pattern`, `/skills:add-pact-contract-tests`, `/skills:add-pagination`, `/skills:add-purge-job`, `/skills:add-rate-limit`, `/skills:add-scheduled-rest-client`, `/skills:add-soft-delete`, `/skills:add-test-data-builders`, `/skills:add-testcontainers-resource`, `/skills:add-websocket-broadcast`, `/skills:api-docs-openapi-health`, `/skills:api-security-testing`, `/skills:api-test-strategy-matrix`, `/skills:architecture-decision-records`, `/skills:backup-restore-disaster-recovery`, `/skills:bootstrap-quarkus-rest`, `/skills:commit`, `/skills:data-integrity-constraints`, `/skills:database-performance-review`, `/skills:dependency-supply-chain-security`, `/skills:dockerized-quarkus-runtime`, `/skills:flaky-test-triage`, `/skills:panache-orm-mapping-patterns`, `/skills:persistence-test-patterns`, `/skills:postgres-migration-safety`, `/skills:postgres-query-patterns`, `/skills:privacy-data-retention-lgpd`, `/skills:quarkus-test-patterns`, `/skills:release-readiness-checklist`, `/skills:rest-assured-api-suite`, `/skills:secrets-config-management`, `/skills:sre-incident-runbooks`, `/skills:threat-modeling-api-security`, `/skills:transaction-boundary-design`

## Available agents (29)
`/agents:adr`, `/agents:api-governance`, `/agents:api-test-automation`, `/agents:architect`, `/agents:backup-recovery`, `/agents:ci-cd`, `/agents:data-modeling`, `/agents:domain-module`, `/agents:error-handling`, `/agents:flaky-test`, `/agents:integration-test`, `/agents:migration-safety`, `/agents:mutation-testing`, `/agents:observability`, `/agents:orm-mapping`, `/agents:performance`, `/agents:privacy-compliance`, `/agents:qa-strategy`, `/agents:query-optimization`, `/agents:realtime-qa`, `/agents:release-manager`, `/agents:resilience`, `/agents:review`, `/agents:security`, `/agents:sre-runbook`, `/agents:supply-chain-security`, `/agents:testing`, `/agents:threat-modeling`, `/agents:transaction-consistency`

## Available commands (0)

