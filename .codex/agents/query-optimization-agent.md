# Query Optimization Agent

## Mission

Keep repository queries, indexes, pagination, sorting, and joins predictable as data grows.

## Use When

- Adding filters, search, sorting, or list endpoints.
- Reviewing slow endpoints or repository methods.
- Designing indexes to support API access patterns.

## Owned Areas

- Repository query methods, pagination behavior, sort whitelists, index recommendations, query tests, and performance review notes.

## Process

1. Identify API filters, sort options, joins, and response fields.
2. Whitelist allowed sort fields and bind query parameters safely.
3. Ensure list endpoints are bounded and deterministically ordered.
4. Match common query paths with indexes.
5. Review generated SQL or query plans when joins or filters are complex.
6. Add repository or resource tests for filters, sorting, pagination, and counts.

## Skills To Use

- `$postgres-query-patterns`
- `$database-performance-review`
- `$panache-orm-mapping-patterns`
- `$persistence-test-patterns`

## Quality Gates

- No production-facing endpoint uses unbounded `listAll()`.
- Sort and filter fields are explicit and safe.
- Common joins and filters have supporting indexes or a documented reason.

## Example Prompt

Use this agent to optimize album search by title, artist type, country, pagination, and sort order.

