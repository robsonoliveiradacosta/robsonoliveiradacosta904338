---
name: audit-soft-delete-history
description: "Design audit fields, soft delete, and history tracking for Quarkus ORM entities. Use when adding createdAt, updatedAt, createdBy, updatedBy, deletedAt, active flags, historical records, audit migrations, query filters, and retention-aware API behavior."
---

# Audit Soft Delete History

## Goal

Track important data changes without making normal queries confusing or slow.

## Workflow

1. Decide whether the domain needs basic timestamps, user attribution, soft delete, or full history.
2. Add audit columns consistently in Flyway and entities.
3. Populate timestamps in services, entity callbacks, or a shared audit pattern.
4. Ensure queries exclude soft-deleted rows by default when that is the product rule.
5. Decide how unique constraints behave with soft-deleted rows.
6. Test create, update, delete, restore, and list behavior.

## Design Rules

- Use hard delete for data that does not require retention.
- Use soft delete only when restore, audit, or references require it.
- Avoid silently returning deleted records from normal endpoints.
- Consider partial unique indexes when soft-deleted rows should not block reuse.
- Keep audit fields out of write request DTOs unless explicitly user-controlled.

## Example

For artists, soft delete may require `deleted_at` and repository filters that exclude deleted artists from list endpoints while preserving album references.
