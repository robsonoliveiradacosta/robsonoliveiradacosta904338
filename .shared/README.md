# Unified skills and agents

Single source of truth for the AI skills and specialist agents that target
this Quarkus 21 / PostgreSQL / Flyway / MinIO project. The collection is the
**deduplicated union** of two upstream branches plus locally-authored skills
(notably the `spec-*` spec-driven-development flow):

| Source branch | Style |
|---|---|
| `claude-opus-4.7-xhigh` | Action recipes (`add-X`, `bootstrap-X`) and reviewer agents |
| `codex-gpt-5.5-xhigh` | Capability/governance guides and specialist implementer agents |
| local | `spec-create` → `spec-plan` → `spec-tasks` → `spec-implement` flow |

When a Claude action and a Codex governance guide describe the same concept,
they are merged into a single canonical skill — the action recipe runs first
and the strategic considerations from the Codex guide are appended under a
`## Strategic considerations & governance` section.

## Layout

```
.shared/
├── skills/<name>.md         # 57 canonical skills (frontmatter + body)
│                            # 53 from upstream + 4 local spec-* skills
├── agents/<name>.md         # 29 canonical agents
├── commands/<name>.md       # slash commands (currently empty — supported
│                            # by the build script if you add files here)
└── scripts/
    └── build.py             # generator — pulls from upstream branches and
                             # produces per-tool adapters under .claude/,
                             # .codex/, .gemini/ and .cursor/.
```

## Per-tool adapters (hybrid: symlink + generate)

Run `python3 .shared/scripts/build.py` from the repo root to (re)generate:

| Tool | Output | Strategy | Invocation |
|---|---|---|---|
| Claude Code | `.claude/{skills,agents}/` | **Symlink** → `.shared/` | Skills auto-trigger; agents via the `Agent` tool |
| Codex CLI | `.codex/{skills,agents}/` | **Symlink** + tiny generated `agents/openai.yaml` per skill | `$<skill-name>` to invoke a skill, name the agent explicitly |
| Gemini CLI | `.gemini/commands/{skills,agents}/<name>.toml` + `.gemini/GEMINI.md` | **Generated** (TOML format) | `/skills:<name>`, `/agents:<name>` |
| Cursor | `.cursor/rules/{skills,agents}/<name>.mdc` (Agent Requested) | **Generated** (different frontmatter) | Cursor auto-attaches matching rules |

Why hybrid: Claude Code and Codex use the same Markdown + YAML-frontmatter
file shape as `.shared/`, so symlinks let edits in `.shared/` propagate
instantly with no duplicated bytes. Gemini CLI expects TOML and Cursor uses a
divergent frontmatter (`description` + `alwaysApply`, no `name` field), so
those two tools genuinely need transformed output.

**Platform note**: relative symlinks work natively on Linux and macOS (default
`git config core.symlinks=true`). On Windows the repo needs symlinks enabled
(Developer Mode + `git config --global core.symlinks true`); otherwise Claude
Code and Codex will see literal `../../../.shared/...` paths instead of
following them.

## Refresh from upstream

```bash
python3 .shared/scripts/build.py            # full rebuild
python3 .shared/scripts/build.py --only claude   # only one tool
```

The script reads source files from the two upstream branches via `git show`,
so the only requirement is that those branches exist in the local repo.

## Inventory at a glance

- 57 skills: 29 action recipes + 23 governance guides (17 merged pairs) + 4
  spec-driven-development flow skills (`spec-create`, `spec-plan`,
  `spec-tasks`, `spec-implement`)
- 29 agents (5 architecture/planning, 4 persistence, 4 security/privacy, 2 API,
  6 ops/resilience, 7 testing, 1 final review)

For the full list, see `.claude/README.md` (or any of the other adapter
READMEs — they all show the same inventory).

## Spec-driven development flow

For non-trivial features (multiple layers, several files), use the four
local `spec-*` skills as a chain. Each writes/reads files under
`specs/NNN-<slug>/` so the work is reviewable, resumable, and committed
alongside the code:

```
spec-create  →  specs/NNN-<slug>/spec.md     # what + why + acceptance criteria
spec-plan    →  specs/NNN-<slug>/plan.md     # files, packages, migration #, skills
spec-tasks   →  specs/NNN-<slug>/tasks.md    # ordered checklist with validation
spec-implement(NNN [Tnn])                    # runs one task, ticks the box
```

`spec-plan` delegates cross-file design to the `architect` agent, and
`spec-tasks` lists which `add-*` skills and which review agents
(`migration-safety`, `security`, `testing`, …) to invoke per task. So the
flow doesn't replace the existing toolkit — it sequences it.
