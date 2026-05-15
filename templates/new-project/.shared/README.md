# Shared skills, agents and commands

Single source of truth for the AI skills, specialist agents and slash
commands of this project. Each tool (Claude Code, Codex, Gemini CLI, Cursor)
gets a thin per-tool adapter generated from these files by
`scripts/build.py`.

## Layout

```
.shared/
├── skills/<name>.md       # action recipes / how-to guides
├── agents/<name>.md       # specialist personas / reviewers
├── commands/<name>.md     # slash commands (e.g. /<name>)
└── scripts/build.py       # generator
```

Add files freely as the project grows — empty folders are fine until you do.

## File format

Every `<name>.md` file must start with YAML frontmatter:

```markdown
---
name: add-rest-endpoint
description: Add a new REST endpoint with handler, validation, tests and OpenAPI docs. Use when the user asks for a new API route, new resource, or new POST/GET/etc handler.
---

# add-rest-endpoint

## When to invoke
- "add a route for X"
- "expose Y as an endpoint"

## Workflow
1. ...
2. ...
```

The `description` is what tools use for **auto-discovery** (Claude Code skill
matching, Cursor "Agent Requested" rules, Gemini command help). Make it
specific about *when* the skill should fire — generic descriptions miss
triggers.

## Build / refresh adapters

```bash
python3 .shared/scripts/build.py            # all four tools
python3 .shared/scripts/build.py --only claude   # one tool
```

What each tool gets:

| Tool | Output | Strategy |
|---|---|---|
| Claude Code | `.claude/{skills,agents,commands}/` | **Symlink** to `.shared/` (no rebuild needed when editing content) |
| Codex CLI | `.codex/{skills,agents,commands}/` + `agents/openai.yaml` per skill | **Symlink** + tiny generated UI sidecar |
| Gemini CLI | `.gemini/commands/{skills,agents,commands}/<n>.toml` | **Generated** TOML wrappers |
| Cursor | `.cursor/rules/{skills,agents}/<n>.mdc` + `.cursor/commands/<n>.md` | **Generated** MDC rules |

Edits to `.shared/` are picked up by Claude/Codex instantly (they are
symlinks). For Gemini/Cursor, run the build to refresh the wrappers.

## Platform note

Symlinks work natively on Linux and macOS. On Windows, enable Developer Mode
and run `git config --global core.symlinks true`, otherwise Claude Code and
Codex will see literal `../../../.shared/...` text instead of following the
links.
