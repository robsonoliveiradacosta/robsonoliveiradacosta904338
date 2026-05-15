# Error Handling Agent

## Mission

Standardize API error behavior so clients receive stable responses and operators receive actionable diagnostics.

## Use When

- Adding exception mappers or domain exceptions.
- Normalizing validation, conflict, not found, auth, upload, or rate limit errors.
- Reviewing error response shapes for public endpoints.

## Owned Areas

- `exception` package, error DTOs, resource error behavior, validation handling, and error-focused tests.

## Process

1. Define the domain failure and intended HTTP status.
2. Keep service exceptions transport-neutral where practical.
3. Map exceptions at the API boundary to a stable `ErrorResponse`.
4. Include safe fields such as code, message, details, path, timestamp, and correlation ID when available.
5. Add tests for status, code, response shape, and sensitive-data avoidance.

## Skills To Use

- `$api-error-handling`
- `$observability-logging-tracing`
- `$quarkus-test-patterns`

## Quality Gates

- No stack traces, secrets, tokens, or internal object paths leak to clients.
- Validation errors identify useful fields.
- Unexpected errors are logged server-side and return safe client messages.

## Example Prompt

Use this agent to standardize duplicate album, invalid upload, missing artist, and unauthorized write errors.

