# Plan 001 — User Favorites

- **Spec:** [./spec.md](./spec.md)
- **Status:** draft
- **Updated:** 2026-05-15

Produced by `spec-plan` via the `architect` agent against the project state at
HEAD. Open questions from the spec are resolved below; any remaining
decisions are listed in the final section.

## Open questions resolved

1. **Sort options on listing endpoint** — only `createdAt:asc` and
   `createdAt:desc` (default) are exposed. Album-field sorting would
   require an extra JOIN and a less obvious contract; defer until a real
   need surfaces. Sort param format mirrors `AlbumService.parseSortParam`
   (`field:direction`) with whitelisted values.
2. **Listing response shape** — flat-with-nested-album:
   `FavoriteResponse(Long albumId, Instant favoritedAt, AlbumResponse album)`.
   Reuses `AlbumResponse.from(Album)` verbatim; matches the existing
   record-style under `com.quarkus.dto.response`.

## Layered impact

| Layer | Action |
|---|---|
| Migration | NEW `V11__create_favorites_table.sql` — `favorites(id, user_id, album_id, created_at)`, UNIQUE `(user_id, album_id)`, FKs `ON DELETE CASCADE` to `users.id` and `albums.id`, index on `(user_id, created_at DESC)` |
| Entity | NEW `Favorite` (junction-with-payload — dedicated entity because we expose `favoritedAt`) |
| Repository | NEW `FavoriteRepository`: `findByUserAndAlbum`, `findByUserPaged(userId, Page, Sort)` (with `JOIN FETCH` of the album), `countByUser(userId)`, `deleteByUserAndAlbum(userId, albumId)` |
| Service | NEW `FavoriteService` — `add` / `remove` `@Transactional`; `list` / `get` no annotation (matches `AlbumService` pattern) |
| Resource | NEW `FavoriteResource` at `/v1/me/favorites` — 4 endpoints, all `@RolesAllowed({"USER","ADMIN"})` |
| DTOs | NEW `FavoriteResponse` (flat-with-nested-album), NEW `FavoriteStatusResponse` (probe shape `{albumId, favoritedAt}`); NO request DTO (path-param only) |
| Tests | NEW `FavoriteServiceTest` (Mockito), NEW `FavoriteResourceTest` (REST Assured + `TestTokenHelper`) |
| Cross-cutting | None — `RateLimitFilter` and JWT roles already cover; `NotFoundExceptionMapper` covers 404s |

## Files to create

- `src/main/resources/db/migration/V11__create_favorites_table.sql`
- `src/main/java/com/quarkus/entity/Favorite.java`
- `src/main/java/com/quarkus/repository/FavoriteRepository.java`
- `src/main/java/com/quarkus/service/FavoriteService.java`
- `src/main/java/com/quarkus/resource/FavoriteResource.java`
- `src/main/java/com/quarkus/dto/response/FavoriteResponse.java`
- `src/main/java/com/quarkus/dto/response/FavoriteStatusResponse.java`
- `src/test/java/com/quarkus/service/FavoriteServiceTest.java`
- `src/test/java/com/quarkus/resource/FavoriteResourceTest.java`

## Files to modify

**None.** Explicit choice not to touch:

- `Album.java` / `User.java` — no back-reference `@OneToMany`. Keeps
  Album/User side-effect-free; favorites query goes through
  `FavoriteRepository`. CASCADE is enforced at the DB layer (FK), not via
  JPA cascade.
- `AlbumResponse.java` — reused as-is.
- `UserRepository.java` — already has `findByUsername`.
- `application.properties` — no new config keys.

## Skills to invoke

In execution order. `spec-tasks` will map these to specific tasks.

1. **`add-flyway-migration`** — for `V11__create_favorites_table.sql` (use
   the create-table template, add explicit FK cascades + composite UNIQUE
   + the listing index).
2. **`add-crud-resource Favorite`** — as a structural starting point only.
   This is junction-with-payload, not a standard CRUD entity, so
   afterward manually:
   - Drop the generated `FavoriteRequest` (no body).
   - Replace generated `findById` with `findByUserAndAlbum` semantics.
   - Make POST idempotent (200 vs 201 based on found/created).
   - Make DELETE idempotent (always 204).
3. **`rest-assured-api-suite`** — companion guidance for the resource test.
4. **`persistence-test-patterns`** — only if we later add a Postgres-backed
   concurrency test (currently deferred — see "Decisions").

## Endpoints introduced

| Method | Path | Role | Status codes |
|---|---|---|---|
| `POST` | `/v1/me/favorites/{albumId}` | `USER`, `ADMIN` | 201, 200 (idempotent), 401, 404 (album), 429 |
| `DELETE` | `/v1/me/favorites/{albumId}` | `USER`, `ADMIN` | 204 (always — idempotent), 401, 429 |
| `GET` | `/v1/me/favorites` | `USER`, `ADMIN` | 200 `PageResponse<FavoriteResponse>`, 401, 429 |
| `GET` | `/v1/me/favorites/{albumId}` | `USER`, `ADMIN` | 200 `FavoriteStatusResponse`, 401, 404, 429 |

**Asymmetry note:** POST validates album existence and returns 404 if
missing; DELETE is fire-and-forget cleanup and always returns 204 even if
the album doesn't exist. Documented this way in the spec.

## Roles & security

- Every endpoint: `@RolesAllowed({"USER","ADMIN"})`. ADMIN does **not** get
  cross-user visibility — `/me/...` is always the JWT subject.
- Principal name (= `User.username`) resolved at the service layer via
  `securityIdentity.getPrincipal().getName()` →
  `userRepository.findByUsername(...)`. No new JWT claim.
- `quarkus.security.jaxrs.deny-unannotated-endpoints=false` is set
  globally — every endpoint has explicit `@RolesAllowed`. No `@PermitAll`.
- `RateLimitFilter` (10 req/min per principal) applies automatically.

## Transaction boundaries

- `FavoriteService.add(...)` → `@Transactional` (writes)
- `FavoriteService.remove(...)` → `@Transactional` (bulk delete-by-keys)
- `FavoriteService.list(...)` → no annotation (matches `AlbumService.findAll`)
- `FavoriteService.get(...)` → no annotation
- Do **not** use `Transactional.TxType.NEVER` — repo doesn't, and it'd
  break some Panache lazy-fetch patterns.

## Validation strategy

- `albumId`: `@PathParam("albumId") Long` — JAX-RS auto-converts;
  non-numeric → 404 (Quarkus default).
- Query params: `page >= 0`, `0 < size <= 100` — clamp in service (mirror
  `AlbumService.findAll` lines 46-54), do not throw on out-of-range.
- Sort: whitelist `createdAt`; fallback to `createdAt:desc`.
- No request body → no Bean Validation needed.

## Risks & mitigations

| Risk | Likelihood | Mitigation |
|---|---|---|
| Concurrent POST race on same `(user, album)` | low | DB UNIQUE constraint + service catches `PersistenceException`/`ConstraintViolationException`, falls through to "already favorited" path → returns 200 with existing row. Idempotent end state. |
| N+1 on listing endpoint (each `Favorite` lazy-loads `Album` → each `Album` loads artists) | high if naive | Repository's `findByUserPaged` uses `JOIN FETCH f.album a LEFT JOIN FETCH a.artists`. Validate via SQL log in dev. Do NOT eager-fetch images (separate table, not in `AlbumResponse`). |
| Missing index on `(user_id, created_at DESC)` → seq scan as table grows | medium | Explicit index in V11. |
| `UserRepository.findByUsername` called on every request | low | Acceptable at current scale; revisit only if profiling shows it. |
| FK CASCADE silently deletes favorites on album/user delete | n/a | Documented as desired behavior in spec. |

## Test plan

### `FavoriteServiceTest` (Mockito unit, no `@QuarkusTest`)

- `shouldAddFavoriteAndPersist`
- `shouldReturn200SemanticsWhenFavoriteAlreadyExists` (no persist call)
- `shouldThrowNotFoundWhenAlbumDoesNotExistOnAdd`
- `shouldRemoveFavoriteWhenItExists`
- `shouldBeNoOpWhenRemovingNonExistentFavorite`
- `shouldNotFailWhenRemovingForNonExistentAlbum`
- `shouldListFavoritesScopedToCurrentUser`
- `shouldClampPageSizeAt100AndMin1`
- `shouldReturnFavoriteStatusWhenFavorited`
- `shouldThrowNotFoundOnStatusProbeWhenNotFavorited`

### `FavoriteResourceTest` (`@QuarkusTest`, REST Assured)

- `shouldReturn401WithoutToken` (parameterized over 4 endpoints)
- `shouldReturn201OnFirstFavorite`
- `shouldReturn200OnRepeatFavorite` (idempotent POST)
- `shouldReturn404WhenFavoritingNonExistentAlbum`
- `shouldReturn204OnDeleteWhetherOrNotFavoriteExisted` (call DELETE twice)
- `shouldReturn204OnDeleteForNonExistentAlbum`
- `shouldListOnlyMyFavoritesNotOthers` (cross-user isolation)
- `shouldListNewestFirst`
- `shouldRespectPageSizeAndPageParams`
- `shouldReturn200OnStatusProbeWhenFavorited`
- `shouldReturn404OnStatusProbeWhenNotFavorited`
- `shouldAllowAdminRoleSameAsUser`

### Optional / deferred

- `FavoriteConcurrencyTest` (`@QuarkusTestResource(PostgresResource.class)`)
  — fire two concurrent POSTs for same `(user, album)`, assert race
  resolves to one row + 200 on second. Not in initial scope; revisit if
  bug report surfaces.

## Migration sketch (for `add-flyway-migration`)

```sql
-- V11__create_favorites_table.sql

CREATE TABLE favorites (
    id          BIGSERIAL PRIMARY KEY,
    user_id     BIGINT NOT NULL,
    album_id    BIGINT NOT NULL,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT fk_favorites_user  FOREIGN KEY (user_id)  REFERENCES users(id)  ON DELETE CASCADE,
    CONSTRAINT fk_favorites_album FOREIGN KEY (album_id) REFERENCES albums(id) ON DELETE CASCADE,
    CONSTRAINT uq_favorites_user_album UNIQUE (user_id, album_id)
);

CREATE INDEX idx_favorites_user_created ON favorites(user_id, created_at DESC);
```

## Companion-agent reviews to schedule

After implementation, before merge:

- `migration-safety` — confirm V11 ordering, additive nature, FK cascade direction, index sizing
- `query-optimization` — confirm `JOIN FETCH` in the listing query and that the listing query plan hits `idx_favorites_user_created`
- `security` — confirm no IDOR (cross-user reads are impossible because userId is always derived from the principal, never from the URL)
- `testing` — confirm cross-user-isolation test and ADMIN-as-USER test exist

Skipped for this story:

- `transaction-consistency` — boundaries are trivially small (single-method writes, no cross-aggregate spans)
- `privacy-compliance` — no new PII beyond `user_id`, already covered by existing patterns

## Definition of done

- [ ] `./mvnw test -Dtest=FavoriteServiceTest,FavoriteResourceTest` passes
- [ ] `./mvnw test` (full suite) still green
- [ ] Swagger UI at `/q/swagger-ui` shows the 4 new endpoints under a `Favorites` tag with correct `@RolesAllowed` reflected
- [ ] `curl /q/health/ready` still returns 200 after schema change
- [ ] Manual smoke: `POST` twice → 201 then 200; `GET /me/favorites` returns the row; `DELETE` → 204; `GET /me/favorites/{id}` → 404 after delete

## Decisions still open (flag if you disagree)

- [ ] **POST 404 vs DELETE 204 for non-existent album** — current plan keeps
      the asymmetry (POST validates, DELETE is fire-and-forget). Alternative
      would be unifying both to 404. Spec leans toward current plan.
- [ ] **No back-reference on `User`/`Album` entities** — current plan keeps
      Album/User untouched; alternative would add `@OneToMany` for
      navigation convenience. Defer unless a real consumer needs it.
- [ ] **Concurrency test deferred** — race is covered by unique constraint
      + idempotent path; explicit test can wait. Switch to "include now" if
      you want race coverage proven before merge.
