---
name: performance-load-testing
description: "Design load, stress, and performance tests for REST APIs. Use when defining k6, Gatling, JMeter, or similar scenarios, latency targets, p95 and p99 thresholds, throughput, ramp profiles, test data, bottleneck analysis, smoke load checks, and performance reports."
---

# Performance Load Testing

## Goal

Design performance tests that answer a concrete operational question instead of producing vanity numbers.

## Workflow

1. Define the scenario: endpoint mix, user role, data volume, request rate, duration, and environment.
2. Set measurable targets: p95, p99, error rate, throughput, CPU, memory, database connections, and external dependency impact.
3. Prepare representative test data and authentication tokens.
4. Run smoke load first, then baseline, load, stress, and soak tests as needed.
5. Capture results, bottlenecks, environment details, and tuning recommendations.

## Scenario Types

- Smoke: low traffic to verify scripts and environment.
- Baseline: expected normal traffic.
- Load: expected peak traffic.
- Stress: beyond expected peak to find failure mode.
- Soak: sustained traffic to find leaks and resource exhaustion.

## Quality Rules

- Do not compare results from different environments without noting the difference.
- Keep external APIs stubbed unless the test explicitly covers them.
- Record dataset size and warm-up behavior.

## Example

For album listing, test authenticated reads with filters and sorting over realistic album and artist counts, and track p95 latency, database connections, and error rate.
