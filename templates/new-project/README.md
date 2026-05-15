# `templates/new-project/` — AI agent toolkit starter

Bootstraps any new repo with the **shared-source / per-tool-adapter** pattern
used elsewhere in this repository:

- One canonical `AGENTS.md` at the project root, plus `CLAUDE.md` and
  `GEMINI.md` as symlinks to it (so each tool loads the same content through
  its native filename).
- One `.shared/` tree where skills, agents and slash commands are written
  once.
- A `build.py` that produces per-tool adapters: symlinks for Claude Code and
  Codex (no duplication), generated TOML for Gemini CLI, generated MDC for
  Cursor.

## Usage

From a new project's root (the directory must already exist; can be empty
or contain only the framework's initial scaffold):

```bash
bash /absolute/path/to/this/templates/new-project/init.sh
```

That copies the template, creates the symlinks, makes `build.py` executable
and runs it once to produce empty `.claude/`, `.codex/`, `.gemini/` and
`.cursor/` adapter trees.

The script refuses to overwrite an existing `AGENTS.md`, `CLAUDE.md`,
`GEMINI.md` or `.shared/` — back them up first if you want to re-init.

## What the init script produces

```
<new project>/
├── AGENTS.md              # canonical project guide (filled with TODOs)
├── CLAUDE.md  → AGENTS.md # symlink
├── GEMINI.md  → AGENTS.md # symlink
└── .shared/
    ├── README.md
    ├── scripts/build.py
    ├── skills/.gitkeep
    ├── agents/.gitkeep
    └── commands/.gitkeep
```

After init, the adapter dirs (`.claude/`, `.codex/`, `.gemini/`, `.cursor/`)
also exist but are essentially empty — they fill up as you add files under
`.shared/{skills,agents,commands}/` and re-run `build.py`.

## What you do next in the new project

1. **Edit `AGENTS.md`** — replace every `<!-- TODO: ... -->` block with the
   real build commands, service URLs, architecture sketch and conventions
   for the project. The "AI agent toolkit" section is reusable as is.
2. **Add skills as you need them.** Drop a `<name>.md` under
   `.shared/skills/` with the YAML frontmatter shown in `.shared/README.md`,
   then run `python3 .shared/scripts/build.py` to regenerate the Gemini and
   Cursor wrappers. Claude and Codex pick up changes automatically because
   their adapters are symlinks.
3. **Commit `.shared/` and the adapter dirs together** so collaborators on
   any of the four tools get a working setup straight from the clone.

## Manual install (without the script)

If you prefer to do it by hand:

```bash
cp -r /path/to/templates/new-project/.shared .shared
cp /path/to/templates/new-project/AGENTS.md AGENTS.md
ln -s AGENTS.md CLAUDE.md
ln -s AGENTS.md GEMINI.md
chmod +x .shared/scripts/build.py
python3 .shared/scripts/build.py
```
