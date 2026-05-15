# Repository Agents

These agent definitions are reusable role prompts for building projects with the same architecture, structure, and engineering standards as this repository. Use them as specialized working modes, not as isolated documentation.

## Recommended Flow

1. `architecture-agent`: define scope, entities, APIs, integrations, and risks.
2. `scaffold-agent`: create or normalize the Quarkus project foundation.
3. `domain-module-agent`: implement each domain aggregate end to end.
4. `security-agent`: add or review JWT, RBAC, rate limiting, and secret handling.
5. `testing-agent`: add focused unit, resource, integration, and security tests.
6. `devops-agent`: package runtime, Compose services, env vars, and health checks.
7. `review-agent`: perform final code, architecture, security, and test review.

## Operating Rules

- Keep each agent scoped to a clear responsibility and file ownership.
- Prefer the repository's established Quarkus patterns over new abstractions.
- Never revert unrelated changes made by another contributor or agent.
- Validate with the smallest useful command first, then broaden before handoff.
- Record assumptions and unresolved questions in the handoff.

