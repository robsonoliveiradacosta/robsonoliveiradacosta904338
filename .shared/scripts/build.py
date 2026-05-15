#!/usr/bin/env python3
"""
Builds the unified skills/agents/commands library and generates per-tool
adapters for Claude Code, Codex, Gemini CLI and Cursor.

Source of truth:        .shared/{skills,agents,commands}/*.md
.claude/ and .codex/:   relative symlinks back into .shared/ (no duplication)
.codex/ also gets:      generated agents/openai.yaml sidecar per skill
.gemini/ and .cursor/:  generated wrappers (different formats — TOML / MDC)

Run from repository root:  python3 .shared/scripts/build.py
"""
from __future__ import annotations

import argparse
import os
import re
import shutil
import subprocess
import sys
from dataclasses import dataclass
from pathlib import Path
from typing import Optional

ROOT = Path(__file__).resolve().parents[2]
SHARED = ROOT / ".shared"

CLAUDE_BRANCH = "claude-opus-4.7-xhigh"
CODEX_BRANCH = "codex-gpt-5.5-xhigh"


# ---------------------------------------------------------------------------
# Manifest
# ---------------------------------------------------------------------------
# Each tuple is (canonical_name, claude_path|None, codex_path|None).
# When both sources are present the script merges them: Claude content first
# (action recipe) followed by a "Strategic considerations" section drawn from
# Codex (governance angle).

SKILLS: list[tuple[str, Optional[str], Optional[str]]] = [
    # --- Bootstrap & domain modules ---
    ("bootstrap-quarkus-rest",
     ".claude/skills/bootstrap-quarkus-rest/SKILL.md",
     ".codex/skills/quarkus-api-bootstrap/SKILL.md"),
    ("add-crud-resource",
     ".claude/skills/add-crud-resource/SKILL.md",
     ".codex/skills/quarkus-domain-module/SKILL.md"),

    # --- Persistence & schema ---
    ("add-flyway-migration",
     ".claude/skills/add-flyway-migration/SKILL.md",
     ".codex/skills/flyway-postgres-schema/SKILL.md"),
    ("postgres-migration-safety", None,
     ".codex/skills/postgres-migration-safety/SKILL.md"),
    ("data-integrity-constraints", None,
     ".codex/skills/data-integrity-constraints/SKILL.md"),
    ("panache-orm-mapping-patterns", None,
     ".codex/skills/panache-orm-mapping-patterns/SKILL.md"),
    ("postgres-query-patterns", None,
     ".codex/skills/postgres-query-patterns/SKILL.md"),
    ("database-performance-review", None,
     ".codex/skills/database-performance-review/SKILL.md"),
    ("transaction-boundary-design", None,
     ".codex/skills/transaction-boundary-design/SKILL.md"),
    ("add-optimistic-locking",
     ".claude/skills/add-optimistic-locking/SKILL.md",
     ".codex/skills/concurrency-locking-control/SKILL.md"),
    ("add-idempotency-key",
     ".claude/skills/add-idempotency-key/SKILL.md", None),
    ("add-soft-delete",
     ".claude/skills/add-soft-delete/SKILL.md", None),
    ("add-audit-trail",
     ".claude/skills/add-audit-trail/SKILL.md",
     ".codex/skills/audit-soft-delete-history/SKILL.md"),
    ("add-pagination",
     ".claude/skills/add-pagination/SKILL.md", None),
    ("add-jsonb-column",
     ".claude/skills/add-jsonb-column/SKILL.md", None),
    ("add-bulk-operations",
     ".claude/skills/add-bulk-operations/SKILL.md", None),
    ("add-multi-tenancy",
     ".claude/skills/add-multi-tenancy/SKILL.md", None),
    ("persistence-test-patterns", None,
     ".codex/skills/persistence-test-patterns/SKILL.md"),

    # --- Auth, security, privacy, supply chain ---
    ("add-jwt-auth",
     ".claude/skills/add-jwt-auth/SKILL.md",
     ".codex/skills/jwt-rbac-auth/SKILL.md"),
    ("add-rate-limit",
     ".claude/skills/add-rate-limit/SKILL.md", None),
    ("api-security-testing", None,
     ".codex/skills/api-security-testing/SKILL.md"),
    ("threat-modeling-api-security", None,
     ".codex/skills/threat-modeling-api-security/SKILL.md"),
    ("secrets-config-management", None,
     ".codex/skills/secrets-config-management/SKILL.md"),
    ("dependency-supply-chain-security", None,
     ".codex/skills/dependency-supply-chain-security/SKILL.md"),
    ("privacy-data-retention-lgpd", None,
     ".codex/skills/privacy-data-retention-lgpd/SKILL.md"),
    ("add-purge-job",
     ".claude/skills/add-purge-job/SKILL.md", None),

    # --- API contracts, docs, errors ---
    ("api-docs-openapi-health", None,
     ".codex/skills/api-docs-openapi-health/SKILL.md"),
    ("add-error-handling",
     ".claude/skills/add-error-handling/SKILL.md",
     ".codex/skills/api-error-handling/SKILL.md"),
    ("add-api-versioning",
     ".claude/skills/add-api-versioning/SKILL.md",
     ".codex/skills/api-versioning-compatibility/SKILL.md"),
    ("add-pact-contract-tests",
     ".claude/skills/add-pact-contract-tests/SKILL.md",
     ".codex/skills/contract-testing-openapi/SKILL.md"),
    ("add-openapi-client-gen",
     ".claude/skills/add-openapi-client-gen/SKILL.md", None),

    # --- Runtime, integrations, resilience, ops ---
    ("dockerized-quarkus-runtime", None,
     ".codex/skills/dockerized-quarkus-runtime/SKILL.md"),
    ("add-minio-storage",
     ".claude/skills/add-minio-storage/SKILL.md",
     ".codex/skills/minio-image-upload/SKILL.md"),
    ("add-scheduled-rest-client",
     ".claude/skills/add-scheduled-rest-client/SKILL.md",
     ".codex/skills/external-sync-client/SKILL.md"),
    ("add-fault-tolerance",
     ".claude/skills/add-fault-tolerance/SKILL.md",
     ".codex/skills/resilience-timeouts-retries/SKILL.md"),
    ("add-observability",
     ".claude/skills/add-observability/SKILL.md",
     ".codex/skills/observability-logging-tracing/SKILL.md"),
    ("add-cache",
     ".claude/skills/add-cache/SKILL.md", None),
    ("add-outbox-pattern",
     ".claude/skills/add-outbox-pattern/SKILL.md", None),
    ("add-websocket-broadcast",
     ".claude/skills/add-websocket-broadcast/SKILL.md",
     ".codex/skills/websocket-realtime-testing/SKILL.md"),
    ("sre-incident-runbooks", None,
     ".codex/skills/sre-incident-runbooks/SKILL.md"),
    ("backup-restore-disaster-recovery", None,
     ".codex/skills/backup-restore-disaster-recovery/SKILL.md"),
    ("release-readiness-checklist", None,
     ".codex/skills/release-readiness-checklist/SKILL.md"),

    # --- Testing & QA ---
    ("quarkus-test-patterns", None,
     ".codex/skills/quarkus-test-patterns/SKILL.md"),
    ("rest-assured-api-suite", None,
     ".codex/skills/rest-assured-api-suite/SKILL.md"),
    ("add-testcontainers-resource",
     ".claude/skills/add-testcontainers-resource/SKILL.md",
     ".codex/skills/testcontainers-integration-lab/SKILL.md"),
    ("add-test-data-builders",
     ".claude/skills/add-test-data-builders/SKILL.md",
     ".codex/skills/seed-data-fixtures/SKILL.md"),
    ("api-test-strategy-matrix", None,
     ".codex/skills/api-test-strategy-matrix/SKILL.md"),
    ("flaky-test-triage", None,
     ".codex/skills/flaky-test-triage/SKILL.md"),
    ("add-mutation-testing",
     ".claude/skills/add-mutation-testing/SKILL.md",
     ".codex/skills/mutation-testing-quality/SKILL.md"),
    ("add-load-testing",
     ".claude/skills/add-load-testing/SKILL.md",
     ".codex/skills/performance-load-testing/SKILL.md"),
    ("add-ci-pipeline",
     ".claude/skills/add-ci-pipeline/SKILL.md",
     ".codex/skills/ci-quality-gates/SKILL.md"),

    # --- Architecture decisions ---
    ("architecture-decision-records", None,
     ".codex/skills/architecture-decision-records/SKILL.md"),
]

AGENTS: list[tuple[str, Optional[str], Optional[str]]] = [
    # Planning & Architecture
    ("architect",
     ".claude/agents/quarkus-architect.md",
     ".codex/agents/architecture-agent.md"),
    ("adr", None, ".codex/agents/adr-agent.md"),
    ("data-modeling", None, ".codex/agents/data-modeling-agent.md"),
    ("domain-module", None, ".codex/agents/domain-module-agent.md"),
    ("orm-mapping", None, ".codex/agents/orm-mapping-agent.md"),

    # Persistence
    ("migration-safety",
     ".claude/agents/flyway-migration-reviewer.md",
     ".codex/agents/migration-safety-agent.md"),
    ("transaction-consistency",
     ".claude/agents/transaction-boundary-reviewer.md",
     ".codex/agents/transaction-consistency-agent.md"),
    ("query-optimization",
     ".claude/agents/database-query-reviewer.md",
     ".codex/agents/query-optimization-agent.md"),
    ("performance",
     ".claude/agents/quarkus-performance-reviewer.md",
     ".codex/agents/database-performance-agent.md"),

    # Security & Privacy
    ("security",
     ".claude/agents/quarkus-security-reviewer.md",
     ".codex/agents/security-agent.md"),
    ("threat-modeling", None, ".codex/agents/threat-modeling-agent.md"),
    ("supply-chain-security",
     ".claude/agents/dependency-vulnerability-reviewer.md",
     ".codex/agents/supply-chain-security-agent.md"),
    ("privacy-compliance",
     ".claude/agents/logging-and-pii-reviewer.md",
     ".codex/agents/privacy-compliance-agent.md"),

    # API
    ("api-governance",
     ".claude/agents/api-contract-reviewer.md",
     ".codex/agents/api-governance-agent.md"),
    ("error-handling",
     ".claude/agents/error-handling-reviewer.md",
     ".codex/agents/error-handling-agent.md"),

    # Resilience & Operations
    ("resilience", None, ".codex/agents/resilience-agent.md"),
    ("observability", None, ".codex/agents/observability-agent.md"),
    ("backup-recovery", None, ".codex/agents/backup-recovery-agent.md"),
    ("sre-runbook", None, ".codex/agents/sre-runbook-agent.md"),
    ("release-manager", None, ".codex/agents/release-manager-agent.md"),
    ("ci-cd", None, ".codex/agents/ci-cd-agent.md"),

    # Testing
    ("testing",
     ".claude/agents/quarkus-test-coverage.md",
     ".codex/agents/testing-agent.md"),
    ("integration-test", None, ".codex/agents/integration-test-agent.md"),
    ("api-test-automation", None, ".codex/agents/api-test-automation-agent.md"),
    ("realtime-qa", None, ".codex/agents/realtime-qa-agent.md"),
    ("mutation-testing",
     ".claude/agents/test-quality-reviewer.md",
     ".codex/agents/mutation-testing-agent.md"),
    ("flaky-test",
     ".claude/agents/flaky-test-detector.md",
     ".codex/agents/flaky-test-agent.md"),
    ("qa-strategy", None, ".codex/agents/qa-strategy-agent.md"),

    # Review
    ("review", None, ".codex/agents/review-agent.md"),
]

COMMANDS: list[tuple[str, str]] = []


# ---------------------------------------------------------------------------
# Helpers
# ---------------------------------------------------------------------------

def git_show(branch: str, path: str) -> str:
    out = subprocess.run(
        ["git", "show", f"{branch}:{path}"],
        cwd=ROOT, check=True, capture_output=True, text=True,
    )
    return out.stdout


_FRONTMATTER = re.compile(r"^---\s*\n(.*?)\n---\s*\n(.*)$", re.DOTALL)


def _yaml_unescape(s: str) -> str:
    """Reverse what quote_yaml() does: process `\\"` and `\\\\` left-to-right
    so we don't double-escape on a round-trip."""
    out: list[str] = []
    i = 0
    while i < len(s):
        if s[i] == "\\" and i + 1 < len(s) and s[i + 1] in ('"', "\\"):
            out.append(s[i + 1])
            i += 2
        else:
            out.append(s[i])
            i += 1
    return "".join(out)


def parse_frontmatter(text: str) -> tuple[dict, str]:
    m = _FRONTMATTER.match(text)
    if not m:
        return {}, text
    raw, body = m.group(1), m.group(2)
    fm: dict = {}
    for line in raw.splitlines():
        if ":" in line and not line.startswith(" "):
            k, _, v = line.partition(":")
            v = v.strip()
            if (v.startswith('"') and v.endswith('"')) or (
                v.startswith("'") and v.endswith("'")
            ):
                v = _yaml_unescape(v[1:-1])
            fm[k.strip()] = v
    return fm, body.lstrip("\n")


def strip_body(text: str) -> tuple[dict, str]:
    fm, body = parse_frontmatter(text)
    return fm, body.rstrip() + "\n"


def _first_paragraph(body: str) -> str:
    """First non-heading paragraph — used as a fallback description for files
    without YAML frontmatter (e.g. Codex agent prompts)."""
    paragraph: list[str] = []
    for line in body.splitlines():
        s = line.strip()
        if not s:
            if paragraph:
                break
            continue
        if s.startswith("#") or s.startswith(">"):
            if paragraph:
                break
            continue
        paragraph.append(s)
    return " ".join(paragraph)[:500]


def _strip_leading_h1(body: str) -> str:
    """Drop a leading `# something` line so we can prepend our own canonical H1."""
    lines = body.lstrip("\n").splitlines()
    if lines and lines[0].startswith("# "):
        # also drop the blank line that usually follows
        rest = lines[1:]
        if rest and rest[0].strip() == "":
            rest = rest[1:]
        return "\n".join(rest) + "\n"
    return body


def merge_sources(name: str, claude_text: Optional[str], codex_text: Optional[str]) -> tuple[str, str]:
    """Returns (description, body)."""
    claude_fm: dict = {}
    claude_body = ""
    codex_fm: dict = {}
    codex_body = ""
    if claude_text:
        claude_fm, claude_body = strip_body(claude_text)
        claude_body = _strip_leading_h1(claude_body)
    if codex_text:
        codex_fm, codex_body = strip_body(codex_text)
        codex_body = _strip_leading_h1(codex_body)

    if claude_text and codex_text:
        desc = claude_fm.get("description") or codex_fm.get("description") or name
        merged = (
            f"# {name}\n\n"
            f"{claude_body.rstrip()}\n\n"
            f"---\n\n"
            f"## Strategic considerations & governance\n\n"
            f"{codex_body.rstrip()}\n"
        )
        return desc, merged

    fm = claude_fm or codex_fm or {}
    body = claude_body or codex_body or ""
    body = f"# {name}\n\n{body.rstrip()}\n"
    desc = fm.get("description")
    if not desc:
        desc = _first_paragraph(body) or name
    return desc, body


def write(path: Path, content: str) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(content)


def symlink(link: Path, target: Path) -> None:
    """Create a relative symlink at `link` pointing to `target`. Any pre-
    existing file or symlink at `link` is replaced."""
    link.parent.mkdir(parents=True, exist_ok=True)
    if link.is_symlink() or link.exists():
        link.unlink()
    rel = os.path.relpath(target, start=link.parent)
    link.symlink_to(rel)


def quote_yaml(s: str) -> str:
    s = s.replace("\\", "\\\\").replace('"', '\\"')
    return f'"{s}"'


def quote_toml(s: str) -> str:
    return s.replace('\\', '\\\\').replace('"', '\\"')


def title(name: str) -> str:
    return name.replace("-", " ").title()


# ---------------------------------------------------------------------------
# Stages
# ---------------------------------------------------------------------------

def stage_extract_shared() -> dict:
    """Pulls source files from the two branches into .shared/ with merged
    bodies. Returns an index {kind: [(name, description)]}."""
    index: dict = {"skill": [], "agent": [], "command": []}

    for name, claude_path, codex_path in SKILLS:
        claude_text = git_show(CLAUDE_BRANCH, claude_path) if claude_path else None
        codex_text = git_show(CODEX_BRANCH, codex_path) if codex_path else None
        desc, body = merge_sources(name, claude_text, codex_text)
        write(SHARED / "skills" / f"{name}.md",
              f"---\nname: {name}\ndescription: {quote_yaml(desc)}\n---\n\n{body}")
        index["skill"].append((name, desc))

    for name, claude_path, codex_path in AGENTS:
        claude_text = git_show(CLAUDE_BRANCH, claude_path) if claude_path else None
        codex_text = git_show(CODEX_BRANCH, codex_path) if codex_path else None
        desc, body = merge_sources(name, claude_text, codex_text)
        write(SHARED / "agents" / f"{name}.md",
              f"---\nname: {name}\ndescription: {quote_yaml(desc)}\n---\n\n{body}")
        index["agent"].append((name, desc))

    for name, claude_path in COMMANDS:
        text = git_show(CLAUDE_BRANCH, claude_path)
        # Commands in Claude branch don't always have frontmatter — strip if
        # present, otherwise keep verbatim.
        fm, body = parse_frontmatter(text) if text.lstrip().startswith("---") else ({}, text)
        desc = fm.get("description") or f"Slash command: {name}"
        write(SHARED / "commands" / f"{name}.md",
              f"---\nname: {name}\ndescription: {quote_yaml(desc)}\n---\n\n{body.rstrip()}\n")
        index["command"].append((name, desc))

    return index


def read_shared(kind: str, name: str) -> tuple[dict, str]:
    text = (SHARED / kind / f"{name}.md").read_text()
    fm, body = parse_frontmatter(text)
    return fm, body


# ---------------------------------------------------------------------------
# Adapter generators
# ---------------------------------------------------------------------------

def gen_claude(index: dict) -> None:
    """Claude Code uses the same SKILL.md / agent.md / command.md format as
    `.shared/`, so every adapter is a relative symlink — zero duplication."""
    target = ROOT / ".claude"
    if target.exists():
        shutil.rmtree(target)

    for name, _desc in index["skill"]:
        symlink(target / "skills" / name / "SKILL.md",
                SHARED / "skills" / f"{name}.md")

    for name, _desc in index["agent"]:
        symlink(target / "agents" / f"{name}.md",
                SHARED / "agents" / f"{name}.md")

    for name, _desc in index["command"]:
        symlink(target / "commands" / f"{name}.md",
                SHARED / "commands" / f"{name}.md")

    write(target / "README.md", _claude_readme(index))


def gen_codex(index: dict) -> None:
    """Codex uses the same Markdown+YAML-frontmatter format as `.shared/`, so
    SKILL.md / agent.md / command.md are symlinks. Each skill additionally
    needs a small generated `agents/openai.yaml` UI sidecar."""
    target = ROOT / ".codex"
    if target.exists():
        shutil.rmtree(target)

    for name, desc in index["skill"]:
        symlink(target / "skills" / name / "SKILL.md",
                SHARED / "skills" / f"{name}.md")
        ui = (
            'interface:\n'
            f'  display_name: {quote_yaml(title(name))}\n'
            f'  short_description: {quote_yaml(desc[:140])}\n'
            f'  default_prompt: {quote_yaml(f"Use ${name} to {desc[0].lower()}{desc[1:]}")}\n'
        )
        write(target / "skills" / name / "agents" / "openai.yaml", ui)

    for name, _desc in index["agent"]:
        symlink(target / "agents" / f"{name}.md",
                SHARED / "agents" / f"{name}.md")

    for name, _desc in index["command"]:
        symlink(target / "commands" / f"{name}.md",
                SHARED / "commands" / f"{name}.md")

    write(target / "README.md", _codex_readme(index))


def gen_gemini(index: dict) -> None:
    target = ROOT / ".gemini"
    if target.exists():
        shutil.rmtree(target)

    for kind, folder, prefix in [
        ("skill", "skills", "skill"),
        ("agent", "agents", "agent"),
        ("command", "commands", "cmd"),
    ]:
        for name, desc in index[kind]:
            _, body = read_shared(folder, name)
            prompt_body = body.replace('"""', '\\"\\"\\"')
            toml = (
                f'description = "{quote_toml(desc)}"\n'
                f'prompt = """\n'
                f"{prompt_body}\n\n"
                f"---\n\n"
                f"User request / extra context: {{{{args}}}}\n"
                f'"""\n'
            )
            write(target / "commands" / folder / f"{name}.toml", toml)

    write(target / "GEMINI.md", _gemini_md(index))
    write(target / "README.md", _gemini_readme(index))


def gen_cursor(index: dict) -> None:
    target = ROOT / ".cursor"
    if target.exists():
        shutil.rmtree(target)

    for name, desc in index["skill"]:
        _, body = read_shared("skills", name)
        write(target / "rules" / "skills" / f"{name}.mdc",
              f"---\ndescription: {quote_yaml(desc)}\nalwaysApply: false\n---\n\n{body}")

    for name, desc in index["agent"]:
        _, body = read_shared("agents", name)
        write(target / "rules" / "agents" / f"{name}.mdc",
              f"---\ndescription: {quote_yaml(desc)}\nalwaysApply: false\n---\n\n{body}")

    for name, desc in index["command"]:
        _, body = read_shared("commands", name)
        write(target / "commands" / f"{name}.md",
              f"# /{name}\n\n> {desc}\n\n{body}")

    write(target / "README.md", _cursor_readme(index))


# ---------------------------------------------------------------------------
# READMEs
# ---------------------------------------------------------------------------

def _table(items):
    rows = "\n".join(f"| `{n}` | {d} |" for n, d in items)
    return f"| Name | Description |\n|---|---|\n{rows}\n"


def _claude_readme(index):
    return (
        "# Claude Code adapters\n\n"
        "Every `SKILL.md`, agent and command file in this directory is a "
        "**relative symlink into `.shared/`** — there is no duplicated "
        "content. Edit the file in `.shared/` and the change is visible "
        "everywhere automatically; rerun `python3 .shared/scripts/build.py` "
        "only when you add or remove items from the manifest.\n\n"
        f"- **Skills**: {len(index['skill'])} (skill auto-discovery via the "
        f"frontmatter `description`)\n"
        f"- **Agents**: {len(index['agent'])} (used with the `Agent` tool)\n"
        f"- **Slash commands**: {len(index['command'])}\n\n"
        "## Skills\n" + _table(index["skill"]) +
        "\n## Agents\n" + _table(index["agent"]) +
        "\n## Commands\n" + _table(index["command"])
    )


def _codex_readme(index):
    return (
        "# Codex adapters\n\n"
        "`SKILL.md`, agent and command files are **relative symlinks into "
        "`.shared/`**. Each skill also has a small generated "
        "`agents/openai.yaml` sidecar — that file is not a symlink because "
        "it carries Codex-specific UI metadata.\n\n"
        f"- **Skills**: {len(index['skill'])} — invoke with `$<name>` "
        f"(e.g. `$add-jwt-auth`)\n"
        f"- **Agents**: {len(index['agent'])} — name explicitly "
        f"(`security`, `architect`, …)\n"
        f"- **Commands**: {len(index['command'])}\n\n"
        "## Skills\n" + _table(index["skill"]) +
        "\n## Agents\n" + _table(index["agent"]) +
        "\n## Commands\n" + _table(index["command"])
    )


def _gemini_md(index):
    lines = [
        "# Project context for Gemini CLI\n",
        "Skills, agents and slash commands for this Quarkus 21 / PostgreSQL / "
        "MinIO project are exposed as Gemini custom commands under `.gemini/commands/`.\n",
        "Invocation:\n",
        "- `/skills:<name>` — execute a skill (action recipe or governance guide).",
        "- `/agents:<name>` — adopt a specialist agent persona.",
        "- `/commands:<name>` — run an existing slash command (PRD, tasks, techspec, …).",
        "",
        "Pass extra request context with the standard `{{args}}` slot — the "
        "command prompt already references it.",
        "",
        f"## Available skills ({len(index['skill'])})",
        ", ".join(f"`/skills:{n}`" for n, _ in index["skill"]),
        "",
        f"## Available agents ({len(index['agent'])})",
        ", ".join(f"`/agents:{n}`" for n, _ in index["agent"]),
        "",
        f"## Available commands ({len(index['command'])})",
        ", ".join(f"`/commands:{n}`" for n, _ in index["command"]),
    ]
    return "\n".join(lines) + "\n"


def _gemini_readme(index):
    return (
        "# Gemini CLI adapters\n\n"
        "Generated from `.shared/` by `.shared/scripts/build.py`.\n\n"
        "Each item is exposed as a TOML custom command. Gemini CLI auto-loads "
        "everything under `.gemini/commands/` — invoke with `/<folder>:<name>`.\n\n"
        f"- Skills: {len(index['skill'])} → `/skills:<name>`\n"
        f"- Agents: {len(index['agent'])} → `/agents:<name>`\n"
        f"- Commands: {len(index['command'])} → `/commands:<name>`\n\n"
        "## Skills\n" + _table(index["skill"]) +
        "\n## Agents\n" + _table(index["agent"]) +
        "\n## Commands\n" + _table(index["command"])
    )


def _cursor_readme(index):
    return (
        "# Cursor adapters\n\n"
        "Generated from `.shared/` by `.shared/scripts/build.py`.\n\n"
        "Each skill and agent becomes an **Agent Requested** rule under "
        "`.cursor/rules/`. Cursor automatically attaches matching rules when "
        "their description fits the user's request. Slash commands are placed "
        "under `.cursor/commands/`.\n\n"
        f"- Skills: {len(index['skill'])} (`.cursor/rules/skills/`)\n"
        f"- Agents: {len(index['agent'])} (`.cursor/rules/agents/`)\n"
        f"- Commands: {len(index['command'])} (`.cursor/commands/`)\n\n"
        "## Skills\n" + _table(index["skill"]) +
        "\n## Agents\n" + _table(index["agent"]) +
        "\n## Commands\n" + _table(index["command"])
    )


# ---------------------------------------------------------------------------
# Entrypoint
# ---------------------------------------------------------------------------

def load_index_from_shared() -> dict:
    """Walk .shared/ for ALL canonical files — the upstream-extracted ones
    plus any locally-authored skills/agents/commands not in the manifest."""
    index: dict = {"skill": [], "agent": [], "command": []}
    for kind, folder in [("skill", "skills"), ("agent", "agents"), ("command", "commands")]:
        d = SHARED / folder
        if not d.exists():
            continue
        for f in sorted(d.glob("*.md")):
            fm, _ = parse_frontmatter(f.read_text())
            name = fm.get("name") or f.stem
            desc = fm.get("description") or name
            index[kind].append((name, desc))
    return index


def main(argv: list[str]) -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument(
        "--only",
        choices=["extract", "claude", "codex", "gemini", "cursor", "all"],
        default="all",
    )
    args = parser.parse_args(argv)

    if args.only in ("extract", "all"):
        print("[1/5] Extracting source files into .shared/ ...", flush=True)
        stage_extract_shared()

    # Always rebuild the index by walking .shared/, so adapters include both
    # upstream-extracted AND locally-authored entries.
    index = load_index_from_shared()
    print(f"      → {len(index['skill'])} skills, "
          f"{len(index['agent'])} agents, "
          f"{len(index['command'])} commands in .shared/")

    if args.only in ("claude", "all"):
        print("[2/5] Generating .claude/ adapters ...", flush=True)
        gen_claude(index)
    if args.only in ("codex", "all"):
        print("[3/5] Generating .codex/ adapters ...", flush=True)
        gen_codex(index)
    if args.only in ("gemini", "all"):
        print("[4/5] Generating .gemini/ adapters ...", flush=True)
        gen_gemini(index)
    if args.only in ("cursor", "all"):
        print("[5/5] Generating .cursor/ adapters ...", flush=True)
        gen_cursor(index)

    print("Done.")
    return 0


if __name__ == "__main__":
    sys.exit(main(sys.argv[1:]))
