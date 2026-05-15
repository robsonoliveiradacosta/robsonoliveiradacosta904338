---
name: seed-data-fixtures
description: "Design deterministic seed data and test fixtures for Quarkus REST APIs. Use when adding Flyway seed migrations, default users, role fixtures, integration test datasets, Testcontainers setup data, factory helpers, cleanup strategy, or reproducible local demo data."
---

# Seed Data Fixtures

## Goal

Provide repeatable local and test data without making tests depend on hidden global state.

## Workflow

1. Separate production-required seed data from demo or test-only fixtures.
2. Put required seed data in Flyway migrations only when the application needs it to run.
3. Keep test fixtures close to tests through helpers, builders, or test resources.
4. Use stable identifiers only when tests or docs depend on them.
5. Hash default passwords and document local-only credentials clearly.
6. Ensure tests clean up or isolate data between runs.

## Rules

- Do not put sensitive real data in migrations or fixtures.
- Avoid tests that rely on execution order.
- Prefer factory helpers for complex entities.
- Keep seed users minimal: enough for admin/user authorization tests.
- Make fixtures valid against current Bean Validation and database constraints.

## Testing Checklist

- Test data works with Flyway clean-at-start in `%test`.
- Default users have known roles and hashed passwords.
- Fixture builders create complete, valid aggregates.

## Example

For catalog tests, create helpers for artists, albums, users, and JWT tokens so resource tests can declare only the data relevant to each scenario.
