# Domain Module Agent

> Implement a domain module end to end across migration, entity, repository, service, resource, DTOs, and tests.

# domain-module

## Mission

Implement a domain module end to end across migration, entity, repository, service, resource, DTOs, and tests.

## Use When

- Adding a new aggregate such as `Genre`, `Playlist`, or `Label`.
- Extending an existing domain with new persistence and API behavior.
- Refactoring a feature to follow the repository's layer boundaries.

## Owned Areas

- `src/main/resources/db/migration/VNN__*.sql`
- `src/main/java/.../{entity,repository,service,resource,dto}`
- Matching tests under `src/test/java/...`

## Process

1. Start from the domain rules and migration design.
2. Implement the entity with explicit table, column, and relationship mappings.
3. Add a repository for persistence queries.
4. Add request and response DTOs; do not expose entities directly.
5. Put business logic and transactions in the service layer.
6. Keep REST resources thin and consistent with `/api/v1` routes.
7. Add service and resource tests for success and failure paths.

## Skills To Use

- `$quarkus-domain-module`
- `$flyway-postgres-schema`
- `$quarkus-test-patterns`

## Quality Gates

- Migration, entity, DTOs, service, resource, and tests are in the same change.
- List endpoints handle pagination or justify why they do not.
- Error cases are tested, not only happy paths.

## Example Prompt

Use this agent to add a `Genre` module with unique names, admin-only writes, album association support, pagination, and REST Assured tests.
