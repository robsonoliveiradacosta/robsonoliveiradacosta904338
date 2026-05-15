# Tasks 001 — User Favorites

- **Spec:** [./spec.md](./spec.md)
- **Plan:** [./plan.md](./plan.md)
- **Status:** completed
- **Updated:** 2026-05-15

Run with `spec-implement 001` (next pending task) or `spec-implement 001 T05`
(specific task). Each task is checked off in place when completed.

12 tasks total: 6 implementation, 2 tests, 4 reviews.

## Implementation

- [x] **T01** — Add Flyway migration `V11__create_favorites_table.sql` → done 2026-05-15 17:01
  - **Files:** `src/main/resources/db/migration/V11__create_favorites_table.sql`
  - **Skill:** `add-flyway-migration` (create-table variant)
  - **Body sketch:** `favorites(id BIGSERIAL PK, user_id BIGINT NOT NULL, album_id BIGINT NOT NULL, created_at TIMESTAMPTZ NOT NULL DEFAULT now())`, `UNIQUE(user_id, album_id)`, FKs `ON DELETE CASCADE` to `users.id` and `albums.id`, index `idx_favorites_user_created ON favorites(user_id, created_at DESC)` — exact SQL in `plan.md` §"Migration sketch".
  - **Validation:** `./mvnw test-compile` (compile-only — schema runs in T07/T08 via `flyway.clean-at-start=true`).
  - **Depends on:** —

- [x] **T02** — Create `Favorite` JPA entity → done 2026-05-15 17:06
  - **Files:** `src/main/java/com/quarkus/entity/Favorite.java`
  - **Skill:** `add-crud-resource Favorite` (entity portion only — structural starting point)
  - **Adjustments after generation:**
    - `@Entity @Table(name="favorites")`.
    - `@ManyToOne(fetch=LAZY) @JoinColumn(name="user_id")` to `User`.
    - `@ManyToOne(fetch=LAZY) @JoinColumn(name="album_id")` to `Album`.
    - `Instant createdAt` with `@PrePersist` to set `Instant.now()` if null.
    - Equality on `Long id` only (post-persist); no Set membership requires structural equality.
  - **Validation:** `./mvnw compile`
  - **Depends on:** T01

- [x] **T03** — Create `FavoriteRepository` → done 2026-05-15 17:06
  - **Files:** `src/main/java/com/quarkus/repository/FavoriteRepository.java`
  - **Skill:** `add-crud-resource Favorite` (repository portion)
  - **Methods to add:**
    - `Optional<Favorite> findByUserAndAlbum(Long userId, Long albumId)`
    - `List<Favorite> findByUserPaged(Long userId, Page page, Sort sort)` — JPQL with `JOIN FETCH f.album a LEFT JOIN FETCH a.artists` to avoid N+1
    - `long countByUser(Long userId)`
    - `long deleteByUserAndAlbum(Long userId, Long albumId)`
  - **Validation:** `./mvnw compile`
  - **Depends on:** T02

- [x] **T04** — Create response DTOs (`FavoriteResponse`, `FavoriteStatusResponse`) → done 2026-05-15 17:06
  - **Files:**
    - `src/main/java/com/quarkus/dto/response/FavoriteResponse.java` — `record FavoriteResponse(Long albumId, Instant favoritedAt, AlbumResponse album)` + static `from(Favorite)` factory reusing `AlbumResponse.from(Album)`.
    - `src/main/java/com/quarkus/dto/response/FavoriteStatusResponse.java` — `record FavoriteStatusResponse(Long albumId, Instant favoritedAt)`.
  - **Skill:** `add-crud-resource Favorite` (dto portion). **Drop the generated `FavoriteRequest`** — endpoints take only path params, no body.
  - **Validation:** `./mvnw compile`
  - **Depends on:** T02

- [x] **T05** — Create `FavoriteService` with idempotent semantics → done 2026-05-15 17:06
  - **Files:** `src/main/java/com/quarkus/service/FavoriteService.java`
  - **Skill:** `add-crud-resource Favorite` (service portion — adjusted heavily)
  - **Methods (signatures):**
    - `FavoriteResult add(String username, Long albumId)` — returns `{response, created: boolean}` so resource can pick 201 vs 200. `@Transactional`. Looks up `User` by username; throws `NotFoundException` if album missing; returns existing row if `(user, album)` already favorited; otherwise persists. Catches `PersistenceException`/`ConstraintViolationException` from race and falls through to "already favorited".
    - `void remove(String username, Long albumId)` — `@Transactional`. Lenient: no-op if not favorited or album missing. Returns 0 vs 1 deleted but resource ignores.
    - `PageResponse<FavoriteResponse> list(String username, int page, int size, String sort)` — no annotation. Clamps `page>=0`, `0 < size <= 100`. Whitelisted sort: `createdAt` only; default `createdAt:desc`.
    - `FavoriteStatusResponse get(String username, Long albumId)` — no annotation. Throws `NotFoundException` if not favorited.
  - **Validation:** `./mvnw compile`
  - **Depends on:** T03, T04

- [x] **T06** — Create `FavoriteResource` at `/v1/me/favorites` → done 2026-05-15 17:06
  - **Files:** `src/main/java/com/quarkus/resource/FavoriteResource.java`
  - **Skill:** `add-crud-resource Favorite` (resource portion — adjusted)
  - **Endpoints (all `@RolesAllowed({"USER","ADMIN"})`):**
    - `POST /{albumId}` → calls `service.add(...)`, returns 201 if `created`, 200 otherwise. Body = `FavoriteResponse`.
    - `DELETE /{albumId}` → calls `service.remove(...)`, returns 204 always.
    - `GET /` → calls `service.list(...)`, returns 200 `PageResponse<FavoriteResponse>`.
    - `GET /{albumId}` → calls `service.get(...)`, returns 200 `FavoriteStatusResponse` or 404.
  - **Wiring:** `@Inject SecurityIdentity` to read `getPrincipal().getName()` as username. OpenAPI annotations: `@Tag(name="Favorites")`, `@Operation`, `@APIResponse` for each status code in plan.
  - **Validation:** `./mvnw compile`
  - **Depends on:** T05

## Tests

- [x] **T07** — `FavoriteServiceTest` (Mockito unit) → done 2026-05-15 17:08 (11/11 passed)
  - **Files:** `src/test/java/com/quarkus/service/FavoriteServiceTest.java`
  - **Skill:** `quarkus-test-patterns`
  - **Setup:** `@ExtendWith(MockitoExtension.class)`, no `@QuarkusTest`. Mock `FavoriteRepository`, `UserRepository`, `AlbumRepository`. Inject into `FavoriteService` via `@InjectMocks`.
  - **Cases (10):** see `plan.md` §"FavoriteServiceTest".
  - **Validation:** `./mvnw test -Dtest=FavoriteServiceTest`
  - **Depends on:** T05

- [x] **T08** — `FavoriteResourceTest` (`@QuarkusTest`, REST Assured) → done 2026-05-15 17:10 (15/15 passed)
  - **Files:** `src/test/java/com/quarkus/resource/FavoriteResourceTest.java`
  - **Skill:** `rest-assured-api-suite`
  - **Setup:** `@QuarkusTest`. JWTs via `TestTokenHelper.userToken()` / `adminToken()`. Hits real Postgres at localhost:5432 (test profile already wired) — `flyway.clean-at-start=true` runs V11 fresh.
  - **Cases (12):** see `plan.md` §"FavoriteResourceTest".
  - **Validation:** `./mvnw test -Dtest=FavoriteResourceTest`
  - **Depends on:** T06

## Reviews (gates before merge)

- [x] **T09** — `migration-safety` agent review on V11 → done 2026-05-15 17:11 (no findings)
  - **Agent:** `migration-safety`
  - **Scope:** Confirm V11 ordering (no gap, next-free integer), additive nature (no destructive ALTER), FK cascade direction (deleting `users`/`albums` rows cascades to `favorites`), index `idx_favorites_user_created` is sized appropriately for the listing query, UNIQUE constraint correctly enforces idempotency.
  - **Validation:** Agent reports no critical findings.
  - **Depends on:** T01

- [x] **T10** — `query-optimization` agent review on listing query → done 2026-05-15 17:14 (P1 found and resolved by T13 — two-query pattern; HHH000104 no longer triggered)
  - **Agent:** `query-optimization`
  - **Scope:** Confirm `findByUserPaged` uses `JOIN FETCH f.album a LEFT JOIN FETCH a.artists` (no N+1 against artists collection); confirm Postgres query plan hits `idx_favorites_user_created` for the paginated listing; confirm `countByUser` is cheap (uses the same index or a separate count-only path).
  - **Validation:** Agent reports no critical findings.
  - **Depends on:** T03, T06

- [x] **T11** — `security` agent review on `FavoriteResource` → done 2026-05-15 17:12 (no critical findings; 2 P2 minor)
  - **Agent:** `security`
  - **Scope:** Confirm no IDOR — every service call derives `userId` from `SecurityIdentity.getPrincipal().getName()`, never from URL/body; confirm all 4 endpoints have explicit `@RolesAllowed` (project sets `deny-unannotated-endpoints=false`); confirm 401 path returns no information about whether the album exists; confirm rate limit applies.
  - **Validation:** Agent reports no critical findings.
  - **Depends on:** T06

- [x] **T12** — `testing` agent — coverage check → done 2026-05-15 17:11 (coverage complete; 3 polish items noted as low priority)
  - **Agent:** `testing`
  - **Scope:** Confirm `shouldListOnlyMyFavoritesNotOthers` (cross-user isolation) and `shouldAllowAdminRoleSameAsUser` (admin doesn't get cross-user view) are present and meaningful; confirm idempotent POST and lenient DELETE both have explicit assertions; confirm 401 covers each endpoint.
  - **Validation:** Agent confirms no missing tests.
  - **Depends on:** T07, T08

## Discovered during execution

- [x] **T13** — Fix in-memory paging in `FavoriteRepository.findByUserPaged` (per T10 finding) → done 2026-05-15 17:14 (15/15 tests still pass, no HHH000104 in logs)
  - **Files:** `src/main/java/com/quarkus/repository/FavoriteRepository.java`
  - **Skill:** none — direct edit
  - **Change:** Replace the single JPQL `JOIN FETCH a.artists` + `.page().list()` with the two-query pattern: (1) page favorite IDs only via `find("user.id = ?1", sort, userId).project(Long.class, "id").page(page).list()` so SQL `LIMIT/OFFSET` is honored; (2) hydrate the page with `SELECT DISTINCT f FROM Favorite f JOIN FETCH f.album a LEFT JOIN FETCH a.artists WHERE f.id IN ?1`. Drop `SELECT DISTINCT` once IDs guarantee uniqueness in the projection step.
  - **Why:** Hibernate logs `HHH000104` and pulls the entire result set in memory when `setMaxResults`/`setFirstResult` is combined with collection fetch. Cost is unbounded as a user accumulates favorites.
  - **Validation:** `./mvnw test -Dtest=FavoriteResourceTest` still passes (the `shouldRespectPageSizeAndPageParams` test should now reflect a real SQL `LIMIT`); optionally tail the test logs for absence of `HHH000104` warning.
  - **Depends on:** T10 (which surfaced this), T08 (existing tests must still pass)

## Notes

- A failed validation pauses execution; the user must decide to fix and retry, skip, or abort.
- Marking `[x]` requires the validation step to have passed.
- New tasks discovered during execution are appended (e.g. `T13`, `T14`); never renumbered.
- The `add-crud-resource Favorite` invocation is used as a **structural starting point** in T02-T06. The standard CRUD shape doesn't fit junction-with-payload + idempotent semantics + path-only params — manual adjustments are listed inline per task.
- Plan §"Decisions still open" has 3 items the user can revisit; current tasks reflect the recommended defaults (POST 404 / DELETE 204 asymmetry, no back-reference, concurrency test deferred).
