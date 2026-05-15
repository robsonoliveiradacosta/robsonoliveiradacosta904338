# AGENTS.md

Project guidance for AI coding agents — Claude Code, Codex, Gemini CLI,
Cursor, Aider, and any tool that follows the [agents.md](https://agents.md)
convention. `CLAUDE.md` and `GEMINI.md` at the repo root are symlinks back to
this file.

> **Template note**: replace every `<!-- TODO: ... -->` block below with
> project-specific content on the first commit. Sections you do not need can
> be deleted entirely. The "AI agent toolkit" section near the bottom is
> generic and should stay as is.

## Stack

<!-- TODO: language + version, framework + version, database, object storage,
     queue/broker, infra (e.g. "Java 21 + Quarkus 3.x, PostgreSQL 16, MinIO,
     Docker Compose"). One paragraph max. -->

## Build and Run Commands

<!-- TODO: replace with the real commands for this project. Examples per
     stack:

     # Node / pnpm
     pnpm install
     pnpm dev
     pnpm test
     pnpm build

     # Python / uv
     uv sync
     uv run pytest
     uv run ruff check

     # Go
     go run ./cmd/server
     go test ./...
     go build -o bin/app ./cmd/server

     # Rust
     cargo run
     cargo test
     cargo build --release
-->

```bash
# dev mode
# tests
# build
```

## Service URLs

<!-- TODO: list local URLs the agent will need to verify behavior, e.g.
     - API: http://localhost:8080
     - Health: http://localhost:8080/health
     - DB:    localhost:5432
-->

## Architecture

<!-- TODO: 1–2 paragraphs naming the layers, where each kind of file lives,
     and any non-obvious convention. Examples to call out:
     - layered structure (resource → service → repository → entity)
     - module boundaries
     - schema migration tool + folder
     - auth scheme
     - any "owned by" rule (e.g. "schema owned entirely by Flyway, never
       rely on auto-DDL")
-->

## Testing patterns

<!-- TODO: framework + how integration tests run + any fixture conventions
     (factories, seed data, container reuse, etc.). -->

## Configuration

<!-- TODO: where config lives, how environment overrides are wired, profile
     conventions, secrets handling. -->

## AI agent toolkit (skills, agents, commands)

This project uses the **shared-source / per-tool-adapter** pattern for AI
skills, specialist agents and slash commands. Canonical content lives in
`.shared/` and is exposed to each agent runtime through its native convention:

| Tool | Path | How to invoke |
|---|---|---|
| Claude Code | `.claude/` (symlinks) | Skills auto-trigger; agents via `Agent` tool; `/<cmd>` |
| Codex CLI | `.codex/` (symlinks + tiny `agents/openai.yaml` sidecars) | `$<skill-name>`; name agents explicitly |
| Gemini CLI | `.gemini/commands/{skills,agents,commands}/` (TOML) | `/skills:<n>`, `/agents:<n>`, `/commands:<n>` |
| Cursor | `.cursor/rules/{skills,agents}/` (Agent Requested MDC) | Auto-attached when description matches |

Edit the canonical file in `.shared/` and Claude/Codex see the change
immediately (they are symlinks). Run `python3 .shared/scripts/build.py` to
refresh the Gemini/Cursor wrappers and the Codex sidecars.

See `.shared/README.md` for the format of a skill/agent file and how to add
new ones.

## Conventions

<!-- TODO: lint/format tools, naming rules, package layout, banned patterns,
     PR/commit message style, anything an agent would otherwise have to
     re-derive on every conversation. Be specific and short. -->
