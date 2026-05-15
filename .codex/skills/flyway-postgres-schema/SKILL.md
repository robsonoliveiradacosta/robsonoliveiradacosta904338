---
name: flyway-postgres-schema
description: "Design and update PostgreSQL schemas through Flyway migrations for Quarkus APIs. Use when creating tables, relationships, indexes, constraints, seed data, or schema refactors under src/main/resources/db/migration using this repository's VNN__description.sql convention."
---

# Flyway Postgres Schema

## Goal

Create deterministic, reviewable database migrations that align with JPA entities and service behavior.

## Workflow

1. Inspect existing `V*.sql` files and choose the next integer version.
2. Name the migration with a short action phrase, for example `V12__create_genres_table.sql`.
3. Define tables with explicit primary keys, foreign keys, `not null` constraints, uniqueness rules, and useful indexes.
4. Match Java entity names to database tables intentionally; use snake_case for table and column names.
5. Add seed data only when the application or tests require deterministic defaults.
6. Update entities, repositories, and tests in the same change when schema behavior changes.

## PostgreSQL Practices

- Prefer `bigserial` or identity columns consistently with nearby migrations.
- Use `timestamp` or `timestamp with time zone` intentionally; do not mix without a reason.
- Add indexes for foreign keys and common filters such as status, type, slug, or external ID.
- Use junction tables for many-to-many relationships and enforce composite uniqueness.
- Avoid destructive migrations unless the user explicitly accepts data loss.

## Review Checklist

- Migration version is unique and ordered.
- Constraints express the real domain rules.
- Roll-forward behavior is clear even without a down migration.
- Existing tests do not depend on stale schema assumptions.

## Example

```sql
create table genres (
    id bigserial primary key,
    name varchar(120) not null unique,
    created_at timestamp not null default current_timestamp
);
```
