---
name: contract-testing-openapi
description: "Keep OpenAPI contracts, REST DTOs, examples, and tests aligned for Quarkus APIs. Use when adding contract tests, validating Swagger output, reviewing endpoint compatibility, checking examples, documenting errors, or preventing undocumented API drift."
---

# Contract Testing OpenAPI

## Goal

Ensure the API described by OpenAPI is the API clients actually receive.

## Workflow

1. Identify public endpoints, DTOs, status codes, authentication rules, and examples.
2. Verify OpenAPI metadata includes request bodies, response schemas, and error responses.
3. Add REST tests that assert real payload shapes for representative endpoints.
4. Compare contract changes against previous behavior when compatibility matters.
5. Keep README examples synchronized with executable behavior.

## Contract Rules

- Every public endpoint should have documented success and common error responses.
- Examples must be valid JSON and match DTO validation rules.
- Required and optional fields must match runtime validation.
- Security requirements in OpenAPI must match actual annotations and filters.
- Contract tests should catch accidental field renames, status changes, and missing errors.

## Testing Examples

- Assert `POST /api/v1/auth/login` returns the documented token response.
- Assert invalid album creation returns the documented validation error shape.
- Assert protected writes advertise and enforce bearer authentication.

## Review Checklist

OpenAPI, DTOs, REST Assured tests, README examples, and exception mappers all describe the same API.
