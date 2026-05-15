---
name: external-sync-client
description: "Add external REST API integration and synchronization workflows to Quarkus services. Use when creating REST Client interfaces, integration DTOs, sync services, scheduled jobs, idempotent upserts, external ID tracking, timeout configuration, WireMock tests, and sync result responses."
---

# External Sync Client

## Goal

Integrate external APIs in a way that is observable, testable, and safe to retry.

## Workflow

1. Define the external contract separately from internal domain DTOs.
2. Create a `@RegisterRestClient` interface in `integration`.
3. Configure URL, scope, connect timeout, and read timeout in `application.properties`.
4. Implement a sync service that maps external DTOs to local entities.
5. Make imports idempotent through external IDs, natural keys, or unique constraints.
6. Return a sync result DTO with counts for created, updated, skipped, and failed records.
7. Add a scheduler only when automatic sync is required.
8. Cover failures with WireMock and service tests.

## Design Rules

- Do not let external DTOs leak into public API responses unless they are the product contract.
- Treat remote calls as unreliable: handle timeouts, malformed data, partial failures, and retries carefully.
- Keep manual sync endpoints protected when they can mutate data.
- Log enough context to debug failed syncs without logging secrets.

## Testing Checklist

- WireMock covers success, empty results, malformed payload, timeout, and server error.
- Service tests verify idempotency and update behavior.
- Scheduler tests verify the job delegates to the service and does not duplicate logic.

## Example

A regional integration should include `RegionalApiClient`, `RegionalDto`, `RegionalSyncService`, `RegionalSyncScheduler`, `SyncResult`, and tests for manual and scheduled sync paths.
