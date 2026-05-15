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

This project ships a **deduplicated union** of 57 skills and 29 specialist
agents — 53 skills sourced from two upstream branches (`claude-opus-4.7-xhigh`
for action recipes/reviewers, `codex-gpt-5.5-xhigh` for governance
guides/specialists) plus 4 locally-authored `spec-*` skills that wire a
spec-driven-development flow on top. Canonical content lives in `.shared/`
and is exposed to each tool through its native convention:

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

### MCP servers (context7)

The `spec-create` and `spec-plan` skills use **context7** to fetch *current*
docs for libraries/frameworks/SDKs/CLIs (Quarkus, Hibernate, Panache, etc.)
instead of relying on training data. Install once per tool — config is
machine-local, not committed. The skills degrade gracefully if context7 is
absent (they note "context7 unavailable, used training data" in the spec).

Two transport options:

- **Local (stdio)** — runs `@upstash/context7-mcp` via `npx` on your
  machine. Requires Node ≥ 18. Default; no account needed but rate-limited.
- **Remote (HTTP)** — points at the hosted endpoint
  `https://mcp.context7.com/mcp`. No Node required. **Strongly recommended
  with an API key** (free at https://context7.com/dashboard) — without one,
  the hosted endpoint is aggressively rate-limited.

#### Local install (npx)

| Tool | Install command |
|---|---|
| Claude Code | `claude mcp add context7 -- npx -y @upstash/context7-mcp` |
| Codex CLI | edit `~/.codex/config.toml` — add `[mcp_servers.context7]` with `command = "npx"`, `args = ["-y", "@upstash/context7-mcp"]` |
| Gemini CLI | edit `~/.gemini/settings.json` — add `mcpServers.context7` with `command: "npx"`, `args: ["-y", "@upstash/context7-mcp"]` |
| Cursor | Settings → MCP → Add server `context7` with `npx -y @upstash/context7-mcp` |

To pass the API key in local mode, inject `CONTEXT7_API_KEY` as an env var:

- Claude Code: `claude mcp add context7 -e CONTEXT7_API_KEY=ctx7sk_xxx -- npx -y @upstash/context7-mcp`
- Codex CLI: add `env = { CONTEXT7_API_KEY = "ctx7sk_xxx" }` to the `[mcp_servers.context7]` block
- Gemini CLI / Cursor: add `"env": { "CONTEXT7_API_KEY": "ctx7sk_xxx" }` next to `command`/`args`

#### Remote install (hosted, no Node)

Pass the key via an `Authorization: Bearer` header.

- **Claude Code:**
  ```bash
  claude mcp add --transport http context7 https://mcp.context7.com/mcp \
      --header "Authorization: Bearer ctx7sk_xxx"
  ```
- **Cursor** (`~/.cursor/mcp.json`):
  ```json
  { "mcpServers": { "context7": {
      "url": "https://mcp.context7.com/mcp",
      "headers": { "Authorization": "Bearer ctx7sk_xxx" }
  } } }
  ```
- **Gemini CLI** (`~/.gemini/settings.json`):
  ```json
  { "mcpServers": { "context7": {
      "httpUrl": "https://mcp.context7.com/mcp",
      "headers": { "Authorization": "Bearer ctx7sk_xxx" }
  } } }
  ```
- **Codex CLI** doesn't support HTTP MCP transports natively at time of
  writing — bridge via `mcp-remote`:
  ```toml
  [mcp_servers.context7]
  command = "npx"
  args = ["-y", "mcp-remote", "https://mcp.context7.com/mcp",
          "--header", "Authorization: Bearer ctx7sk_xxx"]
  ```

> **Don't commit the key.** Keep it in your machine-local config
> (`~/.claude.json`, `~/.codex/config.toml`, etc.), not in `.mcp.json` or
> any tracked file. If you must put it in a project file, source it from
> an env var (`${CONTEXT7_API_KEY}`) and gitignore the resolved version.

#### Verify

In Claude Code, `claude mcp list` should show `context7` as `connected`.
Tools then appear as `mcp__context7__resolve-library-id` and
`mcp__context7__query-docs`.

### Spec-driven flow (for non-trivial features)

Four chained skills produce a reviewable, resumable record under
`specs/NNN-<slug>/` for any change touching multiple layers:

1. `spec-create` — writes `spec.md` (problem, goals, acceptance criteria).
2. `spec-plan` — delegates to the `architect` agent, writes `plan.md`
   (files, packages, next migration #, role annotations, skills per phase).
3. `spec-tasks` — writes `tasks.md` as an ordered checklist with per-task
   skill, validation step, and depends-on chain.
4. `spec-implement NNN [Tnn]` — runs one task (or all in `auto` mode),
   invokes the named skill/agent, runs validation, ticks the box.

Use this for full-slice features and any change that benefits from being
captured before code. Direct skill invocation (`add-crud-resource`,
`add-flyway-migration`, …) is still the right path for one-off edits.

#### Model & reasoning recommendations per stage

Optimize cost × quality by reserving Opus + extended thinking for the steps
where a wrong call cascades downstream; use Sonnet for execution and
structured transformations.

| Stage | Model | Reasoning | Why |
|---|---|---|---|
| `spec-create` (interview) | Sonnet 4.6 | standard | Fast turn UX matters more than deep reasoning. The questions are light judgment calls; over-thinking just slows the conversation. |
| `spec-plan` (runner) | Sonnet 4.6 | standard | The skill itself only reads files, picks the next migration #, and prepares the agent prompt. The weight lives in the agent. |
| `spec-plan` → **`architect` agent** | **Opus 4.7** | **extended thinking** | Where one bad decision contaminates everything downstream (wrong junction shape, missing JOIN FETCH, wrong role boundary). Worth the spend. Fast mode is a good default — same quality, lower latency on the step that gates the rest of the flow. |
| `spec-tasks` | Sonnet 4.6 or **Haiku 4.5** | standard | Structured plan → checklist transformation. Deterministic. Drop to Haiku to cut cost. |
| `spec-implement` (runner) | Sonnet 4.6 | standard | Sequence tasks, flip checkboxes, capture validation errors. No heavy reasoning. |
| `spec-implement` → code-gen tasks (T02-T06 style) | Sonnet 4.6 | standard | Repo conventions guide the output; Quarkus + Panache patterns are well-represented in training data. Opus is overkill and tends to over-engineer simple DTOs. |
| `spec-implement` → review agents (T09-T12) | **Opus 4.7** | **extended thinking** | This is where real findings surface. Bugs like `HHH000104` (collection JOIN FETCH + pagination → silent in-memory paging) need strong reasoning to flag — Sonnet typically lets them through. |
| context7 calls | any | n/a | Pure doc lookup; the model just formats the query and reads the result. |

**Practical rules:**

1. **Opus + extended thinking only on the architect and the reviewers.** Two clear-ROI points: architectural decisions and subtle-bug hunting.
2. **Sonnet standard for everything else** — interview, structured transformations, code generation guided by repo conventions.
3. **Haiku only for `spec-tasks`** if cost matters — the most mechanical step.
4. **Fast mode on Opus** is great for the architect: same quality, lower latency on the step that blocks the rest of the flow.

**Anti-pattern:** putting Opus on every step "to be safe." Cost balloons and the marginal gain on structured transformations is zero or negative (Opus over-engineers simple DTOs). Reserve the heavy ammunition for steps where reasoning differentiates the outcome.

**Where the bump is automated (Claude Code only):** the `architect`,
`migration-safety`, `query-optimization`, `security`, and `testing` agent
files carry `model: opus` in their frontmatter (`.shared/agents/<name>.md`).
The Agent tool reads that and routes them to Opus regardless of the parent
session's model — Sonnet sessions still get Opus on those five agents.
Other choices (session model, extended thinking toggle) remain manual. The
overrides live in `AGENT_MODEL_OVERRIDES` in `.shared/scripts/build.py` so
they survive a full rebuild from upstream; edit there to add or change
overrides.

**Codex CLI equivalent (manual via profile).** The `model: opus` line is
Claude-Code-specific syntax — Codex runs GPT models and ignores the field.
The closest mapping is to define profiles in `~/.codex/config.toml` and
invoke them explicitly on the heavy steps:

```toml
[profiles.deep]
model = "gpt-5-codex"            # current GPT model with strongest reasoning
model_reasoning_effort = "high"

[profiles.fast]
model = "gpt-5-mini"
model_reasoning_effort = "medium"
```

Then run with `--profile`:

```bash
codex --profile deep   $spec-plan 001        # architect engages here
codex --profile deep   $spec-implement 001   # T09–T12 reviews engage here
codex --profile fast   $spec-create 001      # interview, transformations
```

Codex doesn't auto-switch per agent the way Claude Code does; the choice is
session-level. If you wrap everything in one `codex --profile deep` session
you get correct behavior on the heavy steps at the cost of paying GPT-5's
high-reasoning rate on the light steps too. Splitting into two sessions
matches the cost profile in the table above.

**Gemini CLI / Cursor:** `model:` frontmatter isn't carried into the
generated TOML / MDC adapters. Configure model and reasoning at the tool's
own level (Gemini CLI flags or `~/.gemini/settings.json`; Cursor's model
selector).

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
