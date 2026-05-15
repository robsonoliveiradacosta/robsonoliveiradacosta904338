---
name: mutation-testing-quality
description: "Assess Java test effectiveness with mutation testing. Use when configuring PIT or similar tools, reviewing survived mutants, finding weak assertions, improving service and domain tests, excluding noisy generated code, and setting practical mutation quality gates."
---

# Mutation Testing Quality

## Goal

Use mutation testing to reveal tests that execute code but do not prove behavior.

## Workflow

1. Target service and domain logic first; avoid starting with resource glue or generated code.
2. Configure PIT or the project's chosen mutation tool for focused packages.
3. Run mutation tests on a small scope before broadening.
4. Review survived mutants and classify them as weak assertion, missing branch, equivalent mutant, or low-value target.
5. Improve tests by asserting outcomes and side effects, not implementation details.
6. Set pragmatic gates only after the suite is stable.

## Review Rules

- Prioritize business rules, validation, authorization decisions, and mapping logic.
- Do not chase equivalent mutants indefinitely; document exclusions when justified.
- Mutation score is a signal, not the goal. Better tests are the goal.
- Keep mutation runs out of the fastest PR gate unless runtime is acceptable.

## Example

If changing an album year validation from `>= 1900` to `> 1900` survives, add a boundary test for year `1900` and the first valid year.
