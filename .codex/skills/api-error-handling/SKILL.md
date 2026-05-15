---
name: api-error-handling
description: "Standardize REST API error handling for professional Quarkus services. Use when adding exception mappers, validation responses, domain errors, HTTP status code policy, ErrorResponse fields, safe logging, correlation IDs, Problem Details compatibility, or tests for error behavior."
---

# API Error Handling

## Goal

Make failures predictable for clients and useful for operators without leaking internals.

## Workflow

1. Define a stable error DTO with fields such as `code`, `message`, `details`, `path`, `timestamp`, and optional `correlationId`.
2. Map validation, authentication, authorization, not found, conflict, unsupported media type, rate limit, and unexpected errors explicitly.
3. Keep domain exceptions meaningful and transport-neutral; map them at the resource boundary.
4. Log unexpected errors with correlation context, but return safe client messages.
5. Document error responses in OpenAPI and cover them with resource tests.

## HTTP Policy

- Use `400` for malformed or invalid client input.
- Use `401` for missing or invalid authentication.
- Use `403` for authenticated users without permission.
- Use `404` for missing resources.
- Use `409` for unique constraint or state conflicts.
- Use `413` for oversized uploads.
- Use `429` for rate limiting.
- Use `500` only for unhandled server failures.

## Testing Checklist

- REST tests assert status code, stable error code, and response shape.
- Validation tests cover field-level details.
- Unexpected exception tests verify safe output and server-side logging.

## Example

For duplicate artist names, throw a domain conflict such as `DuplicateArtistException` from the service and map it to `409` with code `ARTIST_ALREADY_EXISTS`.
