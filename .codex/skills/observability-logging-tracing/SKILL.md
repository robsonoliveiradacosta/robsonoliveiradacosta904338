---
name: observability-logging-tracing
description: "Improve production observability for Quarkus REST APIs. Use when adding structured logs, correlation or request IDs, safe audit events, metrics, tracing, health diagnostics, dependency visibility, log levels, or troubleshooting documentation."
---

# Observability Logging Tracing

## Goal

Make production behavior diagnosable without exposing secrets or overwhelming logs.

## Workflow

1. Add or preserve a correlation ID for every request and include it in logs and error responses when practical.
2. Log request outcomes at useful boundaries: authentication, mutations, external calls, sync jobs, uploads, and unexpected errors.
3. Use structured fields where available: method, path, status, duration, user ID, role, correlation ID, external service, and entity ID.
4. Add metrics or counters for high-value operations and failure modes.
5. Keep health checks dependency-focused and troubleshooting docs current.

## Logging Rules

- Never log passwords, tokens, private keys, presigned URLs, or raw uploaded file contents.
- Use `INFO` for business-relevant lifecycle events, `WARN` for recoverable unusual conditions, and `ERROR` for failures requiring attention.
- Include enough context to debug, but not full request bodies by default.
- Keep noisy SQL/debug logs disabled in production.

## Testing Checklist

- Filters preserve or generate correlation IDs.
- Error responses can be matched to server logs.
- External call failures include service name and safe context.

## Example

For regional sync, log sync start, external API outcome, created/updated/skipped counts, failures, duration, and correlation ID.
