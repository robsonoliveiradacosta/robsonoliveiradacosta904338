# AGENTS.md

Project guidance for AI coding agents — Claude Code, Codex, Gemini CLI,
Cursor, Aider, and any tool that follows the [agents.md](https://agents.md)
convention. Tool-specific files (`CLAUDE.md`, `GEMINI.md`) at the repo root
are symlinks back to this file.

## Build and Run Commands

```bash
# Development mode with live reload (Quarkus Dev Mode)
./mvnw quarkus:dev

# Unit tests only (default skips ITs)
./mvnw test

# Single test class / method
./mvnw test -Dtest=AlbumResourceTest
./mvnw test -Dtest=AlbumResourceTest#shouldListAlbums

# Integration tests (failsafe) — only run when -Dnative or -DskipITs=false
./mvnw verify -DskipITs=false

# Build JVM package
./mvnw package

# Build native executable
./mvnw package -Dnative
./mvnw package -Dnative -Dquarkus.native.container-build=true  # no local GraalVM needed
```

The Maven `native` profile (activated by `-Dnative`) flips `skipITs` to false and disables jar packaging.

## Docker Compose

`./start.sh` packages the app and runs the full stack (`postgres`, `minio`, `app`). Compose builds the `app` service from the root `Dockerfile` (multi-stage `maven:3.9.9-eclipse-temurin-21` → `distroless/java21`), not from `src/main/docker/`. The Dockerfiles under `src/main/docker/` are the upstream Quarkus templates and are only used by the documented manual `docker build` commands.

```bash
./start.sh                                      # build + compose up
docker compose up --build -d                    # manual
docker compose logs -f app|postgres|minio
docker compose down [-v]                        # -v wipes volumes
curl http://localhost:8080/q/health             # full health
```

## Service URLs

- API: http://localhost:8080
- Swagger UI: http://localhost:8080/q/swagger-ui
- Health: http://localhost:8080/q/health (`/live`, `/ready`)
- Quarkus Dev UI (dev mode only): http://localhost:8080/q/dev
- MinIO Console: http://localhost:9001 (minioadmin/minioadmin)
- PostgreSQL: localhost:5432 (db `music_catalog`, postgres/postgres)

## Architecture

Quarkus 3.31.1 on Java 21. Standard layered REST app under `com.quarkus.*`:

`resource/` (JAX-RS) → `service/` (`@ApplicationScoped`, `@Transactional`) → `repository/` (Panache `PanacheRepository`) → `entity/` (JPA). DTOs in `dto/request/` and `dto/response/` decouple the wire format from entities. Endpoints are all under `/api/v1/...`.

### Authentication & authorization

- **SmallRye JWT (RS256)**. Keys live at `src/main/resources/{privateKey,publicKey}.pem` and **must be generated locally** before running — they are not in git. See README §"Gere as Chaves JWT" for the `openssl` commands.
- Tokens are issued by `security/TokenService` with a **5-minute lifespan** (`smallrye.jwt.new-token.lifespan=300`); clients must call `POST /api/v1/auth/refresh` to renew.
- Roles `USER` and `ADMIN` are enforced via `@RolesAllowed`. `quarkus.security.jaxrs.deny-unannotated-endpoints=false`, so endpoints without an annotation are public — always annotate explicitly.
- Default seeded users come from Flyway `V9__insert_sample_users.sql`: `admin/admin123`, `user/user123`. Passwords are BCrypt-hashed (`quarkus-elytron-security-common`).

### Rate limiting

`security/RateLimitFilter` is a JAX-RS `@Provider` running at `Priorities.AUTHENTICATION + 1`. It uses an in-memory `ConcurrentHashMap<String, Bucket>` keyed by the JWT principal name; unauthenticated requests bypass it. Limit is hard-coded at **10 req/min per user**. The test profile sets `app.rate-limit.enabled=false` — keep that in mind when writing tests that hammer endpoints. Because state is in-process, this does not scale horizontally without changes.

### Persistence & migrations

- Hibernate ORM with Panache. `quarkus.hibernate-orm.database.generation=none` — **schema is owned entirely by Flyway** (`src/main/resources/db/migration/V*.sql`). Never rely on auto-DDL; add a new `Vn__*.sql` for schema changes.
- Test profile uses `flyway.clean-at-start=true` against `music_catalog_test`, so tests get a fresh schema each run.
- The album↔artist relationship is many-to-many via the `album_artist` junction (`V3`). Album cover images are a separate `album_images` table (`V4`, altered in `V10`) holding the MinIO object key/hash plus metadata; the binary lives in MinIO, not Postgres.

### Object storage (MinIO)

- `config/MinioStartup` observes `StartupEvent` and **auto-creates the bucket** (default `album-images`) if missing.
- `service/ImageService` issues **presigned GET URLs with a 30-minute expiry** (`app.minio.presigned-url.expiry=30`) so clients fetch images directly from MinIO. Max upload size 50 MB (`app.minio.max-file-size`).
- `health/MinioHealthCheck` participates in `/q/health/ready`, so MinIO outages will mark the app not-ready.

### External regional API + scheduler

- `integration/RegionalApiClient` is a MicroProfile REST Client (`@RegisterRestClient(configKey="regional-api")`) pointed at `quarkus.rest-client.regional-api.url` (default `https://integrador-argus-api.geia.vip`).
- `scheduler/RegionalSyncScheduler` runs `service/RegionalSyncService` daily at **04:00** via `@Scheduled(cron = "0 0 4 * * ?")`. Admins can also trigger a sync manually via `POST /api/v1/regionals/sync`.
- In tests the URL is overridden to `${quarkus.wiremock.devservices.url}` so `quarkus-wiremock-test` can stub responses.

### WebSocket notifications

`websocket/AlbumNotificationSocket` (Quarkus `websockets-next`) exposes `/ws/albums` and broadcasts via a **static** `BroadcastProcessor`. `AlbumService.create` calls `notifyNewAlbum` after persist. The static field means the broadcaster is shared across all CDI clients of the bean — intentional, but it also means tests interact with the same processor (see `AlbumNotificationSocketTest`).

## Testing patterns

- `@QuarkusTest` for in-JVM integration tests. Most tests assume a **real Postgres at localhost:5432** (test profile config, line 84 of `application.properties`). For an isolated Testcontainers Postgres, annotate with `@QuarkusTestResource(PostgresResource.class)` (`src/test/java/com/quarkus/common/PostgresResource.java`); a similar `MinioTestResource` exists for MinIO.
- REST endpoints are tested with **REST Assured**; auth uses `util/TestTokenHelper` to mint valid JWTs against the bundled keys.
- The external regional API is stubbed by `quarkus-wiremock-test`; do not hit the real URL from tests.
- Services use Mockito via `quarkus-junit5-mockito` (`@InjectMock`).

## Configuration

All config in `src/main/resources/application.properties`. Environment overrides flow through:
`DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `MINIO_URL`, `MINIO_ACCESS_KEY`, `MINIO_SECRET_KEY`, `MINIO_SECURE`, `MINIO_BUCKET`, `CORS_ALLOWED_ORIGINS`, `REGIONAL_API_URL`. Quarkus profiles `%dev`, `%test`, `%prod` override at the bottom of the file. `quarkus.devservices.enabled=false` globally — do **not** rely on Dev Services for Postgres/MinIO; the compose stack is the source of truth for local infra.

## AI agent toolkit (skills, agents)

This project ships a **deduplicated union** of 52 skills and 29 specialist
agents, sourced from two upstream branches (`claude-opus-4.7-xhigh` for
action recipes/reviewers, `codex-gpt-5.5-xhigh` for governance
guides/specialists). Canonical content lives in `.shared/` and is exposed to
each tool through its native convention:

| Tool | Path | How to invoke |
|---|---|---|
| Claude Code | `.claude/` (symlinks) | Skills auto-trigger; agents via `Agent` tool |
| Codex CLI | `.codex/` (symlinks + tiny `agents/openai.yaml` sidecars) | `$<skill-name>`; name agents explicitly |
| Gemini CLI | `.gemini/commands/{skills,agents}/` (TOML) | `/skills:<n>`, `/agents:<n>` |
| Cursor | `.cursor/rules/{skills,agents}/` (Agent Requested MDC) | Auto-attached when description matches |

See `.shared/README.md` for the full inventory. The build script
`.shared/scripts/build.py` re-extracts content from upstream branches and
regenerates the Gemini/Cursor adapters; Claude/Codex symlinks just refresh
to point at whatever is in `.shared/`.

### Most-used skills in this codebase

- New endpoint → `add-crud-resource <Entity>` (generates entity + repo + service + resource + DTOs + migration + tests in one pass).
- Schema change → `add-flyway-migration`. Edits to applied migrations are refused — always a new `Vn+k__*.sql`.
- JWT/auth wiring → `add-jwt-auth`. Image upload → `add-minio-storage`. Scheduled sync → `add-scheduled-rest-client`. Real-time push → `add-websocket-broadcast`.

### Most-used review agents

- Cross-file design before coding → `architect`.
- Pre-merge sweep → `security` + `migration-safety` + `transaction-consistency` + `query-optimization`.
- Pre-release sweep → `security` + `supply-chain-security` + `api-governance` + `privacy-compliance` + `release-manager`.

Each skill's `SKILL.md` lists anti-patterns it refuses to generate (e.g. PEM
keys in git, `@Transactional` on private methods, password in logs) — trust
those over re-deriving the rules from scratch.

## Quarkus conventions

- `@ApplicationScoped` services, constructor or field `@Inject` (current code uses field injection).
- Configuration via `@ConfigProperty` (e.g., `app.minio.bucket`).
- Document REST endpoints with MicroProfile OpenAPI annotations (`@Operation`, `@APIResponse`, `@Tag`) — Swagger UI reads them at runtime.
- Throw `jakarta.ws.rs.NotFoundException` etc. from services; `exception/NotAuthorizedExceptionMapper` handles auth failures. Add new mappers under `exception/` rather than catching in resources.
