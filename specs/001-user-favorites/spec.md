# Spec 001 — User Favorites

- **Status:** implemented
- **Created:** 2026-05-15
- **Owner:** robsonaisoftwareengineer@gmail.com
- **Slug:** `001-user-favorites`

## Problem

Today users have no way to mark albums they like and revisit them later. Each
session starts from the catalog root, so anything a user found interesting on
a previous visit is effectively lost unless they remember the exact title.
That kills the reason to return to the app.

A binary "favorite" mark is the lightest possible signal that solves this:
one tap to remember, one tap to forget, and a personal listing endpoint to
come back to.

## Goals

- Authenticated users can mark and unmark any album as a favorite.
- Authenticated users can list and paginate their own favorites, sorted by
  most-recently favorited.
- The UI can cheaply check whether a specific album is currently favorited
  (to drive the toggle state of a star button).

## Non-goals

- Ratings (1-5 stars or thumbs). This spec is binary only.
- Playlists, ordered collections, or any user-curated grouping beyond the
  flat favorites list.
- Public profiles or peer visibility — favorites are private to the owner.
- Personalization, recommendations, or any downstream use of the favorites
  signal. The only consumer is the user's own listing endpoint.

## User-facing behavior

All endpoints are scoped to the authenticated principal via JWT — there is
no `userId` in the URL. Path style is `/v1/me/favorites/...` so it's
clear the resource belongs to the caller.

### Endpoints

| Method | Path | Description |
|---|---|---|
| `POST` | `/v1/me/favorites/{albumId}` | Mark album as favorite. Idempotent. |
| `DELETE` | `/v1/me/favorites/{albumId}` | Unmark. Idempotent (no-op if not favorited). |
| `GET` | `/v1/me/favorites` | Paginated list of favorited albums. |
| `GET` | `/v1/me/favorites/{albumId}` | Probe — is this album favorited? |

### Example interactions

**Mark as favorite (first time)**

```http
POST /v1/me/favorites/42
Authorization: Bearer <jwt>

→ 201 Created
{ "albumId": 42, "favoritedAt": "2026-05-15T10:30:00Z" }
```

**Mark as favorite (already favorited — idempotent)**

```http
POST /v1/me/favorites/42
Authorization: Bearer <jwt>

→ 200 OK
{ "albumId": 42, "favoritedAt": "2026-05-15T10:30:00Z" }
```

**Unmark**

```http
DELETE /v1/me/favorites/42
Authorization: Bearer <jwt>

→ 204 No Content
```

**List favorites (paginated, newest first)**

```http
GET /v1/me/favorites?page=0&size=20&sort=createdAt,desc
Authorization: Bearer <jwt>

→ 200 OK
{
  "items": [
    {
      "favoritedAt": "2026-05-15T10:30:00Z",
      "album": { "id": 42, "title": "Kind of Blue", "year": 1959, ... }
    },
    ...
  ],
  "page": 0,
  "size": 20,
  "totalItems": 137,
  "totalPages": 7
}
```

**Probe state (for UI toggle)**

```http
GET /v1/me/favorites/42
Authorization: Bearer <jwt>

→ 200 OK
{ "albumId": 42, "favoritedAt": "2026-05-15T10:30:00Z" }
```

```http
GET /v1/me/favorites/999
Authorization: Bearer <jwt>

→ 404 Not Found
```

### Edge cases

- **Album does not exist:** `POST` and `DELETE` return `404 Not Found`
  (don't silently create a dangling row).
- **Album is deleted while favorited:** the database FK cascades — the
  favorite row goes away with the album. No orphan rows, no error to the
  user; their list just no longer contains it.
- **User is deleted:** same FK cascade — all of their favorites are
  removed.
- **Concurrent POST from two clients:** the unique constraint on
  `(user_id, album_id)` makes the second insert a no-op via the
  idempotent path. Service catches the conflict and returns 200.

## Acceptance criteria

- [ ] Authenticated USER or ADMIN can `POST` and `DELETE` against their
      own favorites; unauthenticated requests return `401`.
- [ ] `POST` is idempotent: 1st call returns 201, subsequent calls return
      200 with the original `favoritedAt`.
- [ ] `DELETE` is idempotent: returns 204 whether or not the favorite
      existed.
- [ ] `GET /v1/me/favorites` returns only the caller's favorites,
      paginated, sorted by `createdAt desc` by default.
- [ ] `GET /v1/me/favorites/{albumId}` returns 200 with `favoritedAt`
      if favorited, 404 otherwise.
- [ ] Deleting an album removes all favorite rows for that album.
- [ ] All four endpoints documented in OpenAPI (`@Operation`,
      `@APIResponse` for 200/201/204/401/404).
- [ ] Tests cover: happy path for each endpoint, idempotent POST, no-op
      DELETE, 401 without token, 404 for missing album, isolation between
      two users.

## Constraints & assumptions

- **Reuses:** `entity/Album` (V1), `entity/User` (V9), `PageResponse<T>`
  pattern, `TestTokenHelper` for test JWTs.
- **Roles:** read = `{USER, ADMIN}`, write = `{USER, ADMIN}`. A user only
  ever sees their own favorites — there is no admin-of-others view in
  this spec.
- **Performance:** no specific budget. The favorites table will be small
  per user (≤ a few thousand rows in practice); the FK index on
  `user_id` makes the listing query a single index scan + join.
- **Capacity:** no per-user cap in this iteration. Add one later if abuse
  surfaces.
- **Deadline:** none — example spec for testing the spec-driven flow.

## Out of scope

- Ratings (separate spec; would add a `score` column or a sibling table).
- Playlists / ordered collections (separate spec).
- Public favorites / peer profiles (would change auth model significantly).
- Bulk favorite / unfavorite endpoints.
- Notifying the user when a favorited album is updated (would need
  WebSocket or email; out of scope here).
- Using favorites as a personalization or recommendation signal.

## Open questions

- [ ] Sort options on `GET /v1/me/favorites` — only `createdAt desc`,
      or also expose `album.title` / `album.year`? Defer to plan stage,
      check what `PageResponse` already supports.
- [ ] Response shape for the listing — flat (`{favoritedAt, album: {...}}`)
      vs nested under the album (`{album: {...}, favorite: {favoritedAt}}`).
      Defer to plan stage; whichever is closer to existing list responses
      in this repo.

## Library references

No new libraries introduced — all required APIs (Panache, JWT/SecurityIdentity,
Bean Validation, `PageResponse<T>`, REST Assured, Mockito) are already
established in `CLAUDE.md` and used elsewhere in the project. context7 was
**not** invoked because nothing in this spec is beyond what the project
already documents.

## References

- Related specs: — (this is the first spec)
- Tickets / chat threads: example spec to validate the
  `spec-create → spec-plan → spec-tasks → spec-implement` flow
