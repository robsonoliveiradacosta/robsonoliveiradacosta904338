---
name: resilience-timeouts-retries
description: "Harden Quarkus REST APIs and external integrations against transient failures. Use when adding timeouts, retries, backoff, circuit breakers, idempotency, fallback behavior, partial failure handling, sync retry safety, or tests for unreliable dependencies."
---

# Resilience Timeouts Retries

## Goal

Make remote dependencies and background sync jobs fail predictably and recover safely.

## Workflow

1. Set explicit connect and read timeouts for every external client.
2. Retry only safe operations or operations made idempotent by keys or unique constraints.
3. Use bounded retries with backoff; avoid retry storms.
4. Consider circuit breakers for repeatedly failing dependencies.
5. Design partial failure responses with created, updated, skipped, and failed counts.
6. Log failure context and expose safe operational status.
7. Test timeout, server error, malformed payload, and retry/idempotency paths.

## Rules

- Do not retry non-idempotent writes unless an idempotency key exists.
- Keep scheduled jobs safe to rerun.
- Fail fast when a dependency is required for request completion.
- Degrade gracefully only when the product can tolerate stale or partial data.
- Make fallback behavior explicit in tests and docs.

## Example

For regional synchronization, use external IDs or natural keys to upsert records, return a `SyncResult`, and ensure repeated scheduler runs do not duplicate rows.
