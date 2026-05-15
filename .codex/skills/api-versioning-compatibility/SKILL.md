---
name: api-versioning-compatibility
description: "Evolve REST API versions and contracts safely for Quarkus services. Use when adding /api/v1 or future versions, changing DTO fields, deprecating endpoints, preserving backward compatibility, writing migration notes, or reviewing breaking-change risk."
---

# API Versioning Compatibility

## Goal

Change REST APIs without surprising existing clients.

## Workflow

1. Identify whether the change is additive, behavior-changing, or breaking.
2. Keep existing `/api/v1` contracts stable unless a breaking change is explicitly approved.
3. Prefer additive DTO fields, optional request fields, and new endpoints over changing existing meanings.
4. Deprecate before removal; document replacement endpoints and timelines.
5. Add tests that preserve old behavior while covering new behavior.
6. Update OpenAPI and README examples when public contracts change.

## Compatibility Rules

- Safe: adding optional response fields, adding optional request fields, adding new endpoints, adding new enum values only when clients tolerate them.
- Risky: changing validation, pagination defaults, sort semantics, status codes, or error shapes.
- Breaking: renaming fields, removing fields, changing required fields, changing IDs, changing auth requirements, or changing response types.

## Review Checklist

- Existing clients can still parse responses.
- Error status and error body shape remain stable.
- Pagination, sorting, and filters retain previous defaults.
- OpenAPI examples reflect the active version.

## Example

If album responses need `releaseDate`, add it as an optional response field in `v1`. Do not replace an existing `year` field unless a new API version is introduced.
