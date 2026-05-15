---
name: concurrency-locking-control
description: "Control concurrent database writes in Quarkus ORM applications. Use when designing optimistic locking, pessimistic locking, version columns, race-condition prevention, idempotency, duplicate creation protection, lost-update prevention, and concurrent persistence tests."
---

# Concurrency Locking Control

## Goal

Prevent duplicate creation, lost updates, and inconsistent state under concurrent requests.

## Workflow

1. Identify shared resources and operations that can race.
2. Prefer database constraints for duplicate prevention.
3. Use optimistic locking with a version column for user-edited records that can be updated concurrently.
4. Use pessimistic locking only for short critical sections where conflicts are expected and correctness requires serialization.
5. Make retry behavior explicit and safe.
6. Add concurrent tests for important race conditions.

## Design Rules

- Do not rely on "check then insert" without a unique constraint.
- Keep locks short and avoid remote calls while locks are held.
- Return conflict responses for stale updates or duplicate races.
- Use idempotency keys for repeated client submissions when appropriate.
- Document isolation assumptions when behavior depends on them.

## Example

For concurrent artist creation with the same name, enforce a unique constraint and map the database violation to `409`, even if the service also checks for an existing artist first.
