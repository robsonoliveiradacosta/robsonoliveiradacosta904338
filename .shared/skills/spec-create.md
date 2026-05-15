---
name: spec-create
description: "Start a spec-driven feature by writing specs/NNN-feature-slug/spec.md — captures problem, goals, non-goals, user-facing behavior, acceptance criteria, and open questions. Use whenever the user says \"create a spec for X\", \"start a feature X\", \"criar uma spec\", \"vamos planejar X\", or wants to begin work on a non-trivial change before code is written. First step of the spec-create → spec-plan → spec-tasks → spec-implement flow."
---

# spec-create

Capture **what** to build and **why** in a single source-of-truth file before any architecture or code. The spec is consumed by `spec-plan` (architecture), `spec-tasks` (work breakdown), and `spec-implement` (execution).

## When to invoke

- "Create a spec for adding playlists"
- "Vamos começar uma feature de favoritos"
- "Start a spec for the bulk import endpoint"
- Any non-trivial change touching ≥2 layers — refuse for one-file edits and tell the user to make the change directly.

## Inputs to collect

Ask in **one** consolidated message:

| Input | Notes |
|---|---|
| Feature name | Short, kebab-case-friendly (e.g. `user-favorites`) |
| Problem statement | What pain or opportunity drives this? Why now? |
| Goals | 1-3 measurable outcomes |
| Non-goals | What you explicitly will NOT do in this iteration |
| User-facing behavior | API surface (endpoints, payloads), or domain behavior change |
| Constraints | Existing entities to reuse, roles required, perf/SLA, deadline |
| Out of scope | Adjacent work that's tempting but separate |

If the user gives a one-liner, ask follow-ups before writing the file. A vague spec leads to a vague plan.

## Workflow

1. List existing `specs/` directory. Pick the **next** zero-padded 3-digit number (`001`, `002`, …).
2. Slugify the feature name (lowercase, hyphens, no diacritics).
3. Create `specs/NNN-<slug>/spec.md` using the template below.
4. Echo the path back to the user and remind them the next step is `spec-plan`.

## Numbering

- Find max `NNN` across `specs/NNN-*/` directories. Use **decimal**, not lexical, comparison.
- Never reuse a number. If a spec was abandoned, leave the directory or move it under `specs/_archive/`.

## Template

```markdown
# Spec NNN — <Feature Title>

- **Status:** draft
- **Created:** YYYY-MM-DD
- **Owner:** <user>
- **Slug:** `NNN-<slug>`

## Problem

<2-4 sentences. What's broken or missing today? Who feels the pain? What's
the cost of not doing this?>

## Goals

- <Outcome 1, measurable if possible>
- <Outcome 2>

## Non-goals

- <Thing this spec deliberately doesn't address>

## User-facing behavior

<API endpoints, request/response shapes, domain events, UI changes — whatever
the consumer of this feature actually sees. Keep it concrete.>

### Example interaction

```http
POST /api/v1/<resource>
Authorization: Bearer <jwt>
Content-Type: application/json

{ "field": "value" }

→ 201 Created
{ "id": 42, "field": "value" }
```

## Acceptance criteria

- [ ] <Concrete, testable condition 1>
- [ ] <Concrete, testable condition 2>
- [ ] All new endpoints documented in OpenAPI (`@Operation`, `@APIResponse`).
- [ ] Tests cover happy path + at least one auth/validation failure path.

## Constraints & assumptions

- **Reuses:** <existing entities/services/skills>
- **Roles:** read = `<USER|ADMIN>`, write = `<ADMIN>`
- **Performance:** <p95 latency, throughput, payload size — or "no specific budget">
- **Deadline:** <date or "none">

## Out of scope

- <Adjacent feature that should be a separate spec>

## Open questions

- [ ] <Anything still ambiguous — flag for plan stage>

## References

- Related specs: <NNN, NNN>
- Tickets / chat threads: <links>
```

## After writing

Tell the user:
- Path to the new spec.
- Any open questions you couldn't resolve from inputs.
- Next step: `spec-plan NNN` to produce the architectural plan.

## Anti-patterns to refuse

- Writing a spec without acceptance criteria — refuse and ask the user for them.
- Writing a "spec" that's actually a plan (file lists, package names) — those belong in `plan.md`. Push back and capture only intent here.
- Editing a spec that's already in `status: implemented` — start a new spec for follow-up work, link to it via References.
- Creating spec files outside `specs/NNN-<slug>/` — the directory layout is what `spec-plan` and `spec-tasks` rely on to find sibling files.

## Notes for this project

- The `specs/` directory is committed to the repo so the spec lives next to the code that implements it. Don't add it to `.gitignore`.
- For Quarkus features, the typical scope hint to capture is which layer is touched: resource only, full slice (resource + service + repository + entity + migration), background job, integration with external API, or cross-cutting concern (auth, observability, rate limit).
