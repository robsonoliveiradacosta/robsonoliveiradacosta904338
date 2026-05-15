---
name: jwt-rbac-auth
description: "Implement or extend JWT authentication and role-based authorization in Quarkus APIs. Use when adding login, token issuance, token refresh, BCrypt password checks, user roles, @RolesAllowed protections, JWT key configuration, auth tests, or security hardening patterns like rate limiting."
---

# JWT RBAC Auth

## Goal

Add secure authentication and authorization while preserving this repository's role model and Quarkus SmallRye JWT conventions.

## Workflow

1. Model users and roles with explicit persistence fields, unique usernames, password hashes, and active status when needed.
2. Store passwords with BCrypt or the existing security helper. Never persist plain text passwords.
3. Generate JWTs through a dedicated token service. Include issuer, subject, groups/roles, expiration, and only necessary claims.
4. Protect endpoints with `@RolesAllowed`, `@Authenticated`, or local security annotations. Keep public endpoints explicit.
5. Configure keys through `mp.jwt.verify.publickey.location` and `smallrye.jwt.sign.key.location`.
6. Add auth resources for login and refresh only when required by the API contract.
7. Cover auth behavior with service, resource, and integration tests.

## Security Rules

- Do not commit production private keys or real credentials.
- Keep token lifetime short unless the product requirement says otherwise.
- Return generic login errors; do not reveal whether username or password failed.
- Rate-limit login and high-risk endpoints when abuse is plausible.
- Keep authorization checks server-side even if clients hide actions.

## Testing Checklist

- Valid login returns a token with expected roles.
- Invalid credentials are rejected without leaking details.
- Protected endpoints reject missing, expired, malformed, or under-privileged tokens.
- Admin-only write operations are tested with both admin and non-admin users.

## Example

For admin-only writes, annotate create/update/delete resource methods with `@RolesAllowed("ADMIN")` and add REST Assured tests for `401`, `403`, and success cases.
