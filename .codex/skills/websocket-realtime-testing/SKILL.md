---
name: websocket-realtime-testing
description: "Test realtime WebSocket behavior in Quarkus APIs. Use when verifying connection lifecycle, authorization, subscriptions, emitted messages, payload schema, ordering, reconnect behavior, timeouts, album notifications, async waits, and non-flaky realtime tests."
---

# WebSocket Realtime Testing

## Goal

Verify realtime behavior without flaky timing assumptions.

## Workflow

1. Define connection URL, authentication behavior, subscription model, and expected message schema.
2. Establish the WebSocket connection and wait for an explicit connected or subscribed signal when available.
3. Trigger the server event through the normal API or service path.
4. Await expected messages with bounded timeouts and clear failure output.
5. Assert message type, payload fields, ordering requirements, and absence of unexpected messages.
6. Test disconnect, reconnect, unauthorized connection, and malformed message paths when supported.

## Stability Rules

- Do not use fixed sleeps as synchronization.
- Subscribe before triggering the event.
- Use unique test data so messages cannot be confused with other tests.
- Close clients and sessions after each test.
- Keep timeout values bounded and documented.

## Example

For album notifications, connect a test client, wait until subscribed, create an album through `AlbumResource`, then assert one message with the new album ID, title, and event type.
