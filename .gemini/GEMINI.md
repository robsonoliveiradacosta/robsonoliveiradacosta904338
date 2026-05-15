# Project context for Gemini CLI

Skills, agents and slash commands for this Quarkus 21 / PostgreSQL / MinIO project are exposed as Gemini custom commands under `.gemini/commands/`.

Invocation:

- `/skills:<name>` — execute a skill (action recipe or governance guide).
- `/agents:<name>` — adopt a specialist agent persona.
- `/commands:<name>` — run an existing slash command (PRD, tasks, techspec, …).

Pass extra request context with the standard `{{args}}` slot — the command prompt already references it.

## Available skills (52)
`/skills:bootstrap-quarkus-rest`, `/skills:add-crud-resource`, `/skills:add-flyway-migration`, `/skills:postgres-migration-safety`, `/skills:data-integrity-constraints`, `/skills:panache-orm-mapping-patterns`, `/skills:postgres-query-patterns`, `/skills:database-performance-review`, `/skills:transaction-boundary-design`, `/skills:add-optimistic-locking`, `/skills:add-idempotency-key`, `/skills:add-soft-delete`, `/skills:add-audit-trail`, `/skills:add-pagination`, `/skills:add-jsonb-column`, `/skills:add-bulk-operations`, `/skills:add-multi-tenancy`, `/skills:persistence-test-patterns`, `/skills:add-jwt-auth`, `/skills:add-rate-limit`, `/skills:api-security-testing`, `/skills:threat-modeling-api-security`, `/skills:secrets-config-management`, `/skills:dependency-supply-chain-security`, `/skills:privacy-data-retention-lgpd`, `/skills:add-purge-job`, `/skills:api-docs-openapi-health`, `/skills:add-error-handling`, `/skills:add-api-versioning`, `/skills:add-pact-contract-tests`, `/skills:add-openapi-client-gen`, `/skills:dockerized-quarkus-runtime`, `/skills:add-minio-storage`, `/skills:add-scheduled-rest-client`, `/skills:add-fault-tolerance`, `/skills:add-observability`, `/skills:add-cache`, `/skills:add-outbox-pattern`, `/skills:add-websocket-broadcast`, `/skills:sre-incident-runbooks`, `/skills:backup-restore-disaster-recovery`, `/skills:release-readiness-checklist`, `/skills:quarkus-test-patterns`, `/skills:rest-assured-api-suite`, `/skills:add-testcontainers-resource`, `/skills:add-test-data-builders`, `/skills:api-test-strategy-matrix`, `/skills:flaky-test-triage`, `/skills:add-mutation-testing`, `/skills:add-load-testing`, `/skills:add-ci-pipeline`, `/skills:architecture-decision-records`

## Available agents (29)
`/agents:architect`, `/agents:adr`, `/agents:data-modeling`, `/agents:domain-module`, `/agents:orm-mapping`, `/agents:migration-safety`, `/agents:transaction-consistency`, `/agents:query-optimization`, `/agents:performance`, `/agents:security`, `/agents:threat-modeling`, `/agents:supply-chain-security`, `/agents:privacy-compliance`, `/agents:api-governance`, `/agents:error-handling`, `/agents:resilience`, `/agents:observability`, `/agents:backup-recovery`, `/agents:sre-runbook`, `/agents:release-manager`, `/agents:ci-cd`, `/agents:testing`, `/agents:integration-test`, `/agents:api-test-automation`, `/agents:realtime-qa`, `/agents:mutation-testing`, `/agents:flaky-test`, `/agents:qa-strategy`, `/agents:review`

## Available commands (0)

