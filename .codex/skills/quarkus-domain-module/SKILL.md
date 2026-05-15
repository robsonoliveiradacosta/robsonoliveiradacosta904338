---
name: quarkus-domain-module
description: "Implement a complete domain module in this repository's Quarkus architecture. Use when adding a new aggregate or feature that needs Flyway migration, JPA/Panache entity, repository, service, REST resource, request/response DTOs, validation, pagination/filtering, authorization hooks, and tests."
---

# Quarkus Domain Module

## Goal

Add one coherent domain feature without leaking concerns across layers. Follow the local naming style: `*Resource`, `*Service`, `*Repository`, `*Request`, `*Response`, and singular entity names.

## Workflow

1. Model the aggregate: fields, constraints, relationships, uniqueness rules, lifecycle, and delete behavior.
2. Create the next Flyway migration in `src/main/resources/db/migration` using `V<number>__description.sql`.
3. Add the JPA entity in `entity`; use explicit table/column names and Bean Validation where useful.
4. Add a Panache repository for query composition, pagination, sorting, and relationship lookups.
5. Add request DTOs for writes and response DTOs for reads. Keep API contracts separate from entities.
6. Implement service methods for create, read, update, delete, filtering, and mapping.
7. Add a REST resource under `/api/v1/<plural-resource>` with clear status codes.
8. Add tests before considering the module complete.

## Design Rules

- Keep transaction boundaries in services unless the existing neighboring code does otherwise.
- Validate IDs and relationship existence before persistence changes.
- Use pagination response types for list endpoints when results can grow.
- Return stable response shapes; do not expose lazy entities directly.
- Keep error handling consistent with existing exception mappers and response DTOs.

## Testing Checklist

- Service tests cover business rules, validation paths, and missing references.
- Resource tests cover happy paths, invalid input, not found, authorization, pagination, and sorting.
- Migration changes are exercised by Quarkus tests or Testcontainers-backed integration tests.

## Example

For a `Genre` module, create `Genre`, `GenreRepository`, `GenreService`, `GenreResource`, `GenreRequest`, `GenreResponse`, `V11__create_genres_table.sql`, and tests such as `GenreServiceTest` and `GenreResourceTest`.
