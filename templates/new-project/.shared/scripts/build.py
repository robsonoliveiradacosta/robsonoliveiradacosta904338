#!/usr/bin/env python3
"""
Generates per-tool adapters from the canonical skills/agents/commands defined
under .shared/.

Source of truth:        .shared/{skills,agents,commands}/<name>.md
.claude/ and .codex/:   relative symlinks back into .shared/
.codex/ also gets:      generated agents/openai.yaml sidecar per skill
.gemini/ and .cursor/:  generated wrappers (TOML / MDC formats)

Each `<name>.md` must start with YAML frontmatter:

    ---
    name: <kebab-name>
    description: <one-line trigger description>
    ---

    # <name>
    ...body...

Run from the repository root:  python3 .shared/scripts/build.py
"""
from __future__ import annotations

import argparse
import os
import re
import shutil
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
SHARED = ROOT / ".shared"

_FRONTMATTER = re.compile(r"^---\s*\n(.*?)\n---\s*\n(.*)$", re.DOTALL)


def parse_frontmatter(text: str) -> tuple[dict, str]:
    m = _FRONTMATTER.match(text)
    if not m:
        return {}, text
    raw, body = m.group(1), m.group(2)
    fm: dict = {}
    for line in raw.splitlines():
        if ":" in line and not line.startswith(" "):
            k, _, v = line.partition(":")
            fm[k.strip()] = v.strip().strip('"').strip("'")
    return fm, body.lstrip("\n")


def write(path: Path, content: str) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(content)


def symlink(link: Path, target: Path) -> None:
    link.parent.mkdir(parents=True, exist_ok=True)
    if link.is_symlink() or link.exists():
        link.unlink()
    rel = os.path.relpath(target, start=link.parent)
    link.symlink_to(rel)


def quote_yaml(s: str) -> str:
    s = s.replace("\\", "\\\\").replace('"', '\\"')
    return f'"{s}"'


def quote_toml(s: str) -> str:
    return s.replace("\\", "\\\\").replace('"', '\\"')


def title(name: str) -> str:
    return name.replace("-", " ").title()


def load_index() -> dict:
    """Walks .shared/{skills,agents,commands}/*.md and returns
    {kind: [(name, description), ...]}."""
    index: dict = {"skill": [], "agent": [], "command": []}
    for kind, folder in [("skill", "skills"), ("agent", "agents"), ("command", "commands")]:
        d = SHARED / folder
        if not d.exists():
            continue
        for f in sorted(d.glob("*.md")):
            fm, _body = parse_frontmatter(f.read_text())
            name = fm.get("name") or f.stem
            desc = fm.get("description") or name
            index[kind].append((name, desc))
    return index


# ---------------------------------------------------------------------------
# Adapter generators
# ---------------------------------------------------------------------------

def gen_claude(index: dict) -> None:
    target = ROOT / ".claude"
    if target.exists():
        shutil.rmtree(target)
    for name, _ in index["skill"]:
        symlink(target / "skills" / name / "SKILL.md", SHARED / "skills" / f"{name}.md")
    for name, _ in index["agent"]:
        symlink(target / "agents" / f"{name}.md", SHARED / "agents" / f"{name}.md")
    for name, _ in index["command"]:
        symlink(target / "commands" / f"{name}.md", SHARED / "commands" / f"{name}.md")
    write(target / "README.md", _readme("Claude Code", index, symlink_note=True))


def gen_codex(index: dict) -> None:
    target = ROOT / ".codex"
    if target.exists():
        shutil.rmtree(target)
    for name, desc in index["skill"]:
        symlink(target / "skills" / name / "SKILL.md", SHARED / "skills" / f"{name}.md")
        ui = (
            "interface:\n"
            f"  display_name: {quote_yaml(title(name))}\n"
            f"  short_description: {quote_yaml(desc[:140])}\n"
            f"  default_prompt: {quote_yaml(f'Use ${name} to {desc[0].lower()}{desc[1:]}')}\n"
        )
        write(target / "skills" / name / "agents" / "openai.yaml", ui)
    for name, _ in index["agent"]:
        symlink(target / "agents" / f"{name}.md", SHARED / "agents" / f"{name}.md")
    for name, _ in index["command"]:
        symlink(target / "commands" / f"{name}.md", SHARED / "commands" / f"{name}.md")
    write(target / "README.md", _readme("Codex CLI", index, symlink_note=True))


def gen_gemini(index: dict) -> None:
    target = ROOT / ".gemini"
    if target.exists():
        shutil.rmtree(target)
    for kind, folder in [("skill", "skills"), ("agent", "agents"), ("command", "commands")]:
        for name, desc in index[kind]:
            body = (SHARED / folder / f"{name}.md").read_text()
            _fm, body = parse_frontmatter(body)
            body = body.replace('"""', '\\"\\"\\"')
            toml = (
                f'description = "{quote_toml(desc)}"\n'
                f'prompt = """\n{body.rstrip()}\n\n---\n\n'
                f'User request / extra context: {{{{args}}}}\n"""\n'
            )
            write(target / "commands" / folder / f"{name}.toml", toml)
    write(target / "README.md", _readme("Gemini CLI", index, symlink_note=False))


def gen_cursor(index: dict) -> None:
    target = ROOT / ".cursor"
    if target.exists():
        shutil.rmtree(target)
    for kind, folder in [("skill", "skills"), ("agent", "agents")]:
        for name, desc in index[kind]:
            body = (SHARED / folder / f"{name}.md").read_text()
            _fm, body = parse_frontmatter(body)
            write(target / "rules" / folder / f"{name}.mdc",
                  f"---\ndescription: {quote_yaml(desc)}\nalwaysApply: false\n---\n\n{body}")
    for name, desc in index["command"]:
        body = (SHARED / "commands" / f"{name}.md").read_text()
        _fm, body = parse_frontmatter(body)
        write(target / "commands" / f"{name}.md", f"# /{name}\n\n> {desc}\n\n{body}")
    write(target / "README.md", _readme("Cursor", index, symlink_note=False))


def _table(items):
    if not items:
        return "_(none yet — add files under `.shared/`)_\n"
    rows = "\n".join(f"| `{n}` | {d} |" for n, d in items)
    return f"| Name | Description |\n|---|---|\n{rows}\n"


def _readme(tool: str, index: dict, *, symlink_note: bool) -> str:
    note = (
        "All skill/agent/command files in this directory are **relative "
        "symlinks into `.shared/`** — edit `.shared/` to change the content."
        if symlink_note
        else "All files in this directory are **generated** from `.shared/`. "
             "Re-run `python3 .shared/scripts/build.py` after editing `.shared/`."
    )
    return (
        f"# {tool} adapters\n\n{note}\n\n"
        f"- Skills: {len(index['skill'])}\n"
        f"- Agents: {len(index['agent'])}\n"
        f"- Commands: {len(index['command'])}\n\n"
        "## Skills\n" + _table(index["skill"]) +
        "\n## Agents\n" + _table(index["agent"]) +
        "\n## Commands\n" + _table(index["command"])
    )


def main(argv: list[str]) -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument(
        "--only",
        choices=["claude", "codex", "gemini", "cursor", "all"],
        default="all",
    )
    args = parser.parse_args(argv)

    index = load_index()
    print(f"Loaded {len(index['skill'])} skills, "
          f"{len(index['agent'])} agents, "
          f"{len(index['command'])} commands from .shared/")

    if args.only in ("claude", "all"):
        gen_claude(index)
        print("  → .claude/ (symlinks)")
    if args.only in ("codex", "all"):
        gen_codex(index)
        print("  → .codex/ (symlinks + openai.yaml sidecars)")
    if args.only in ("gemini", "all"):
        gen_gemini(index)
        print("  → .gemini/ (TOML)")
    if args.only in ("cursor", "all"):
        gen_cursor(index)
        print("  → .cursor/ (MDC rules)")

    print("Done.")
    return 0


if __name__ == "__main__":
    sys.exit(main(sys.argv[1:]))
