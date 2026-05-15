#!/usr/bin/env bash
# Initialize a new project with the AGENTS.md / .shared/ AI agent toolkit.
#
# Usage:  bash /path/to/templates/new-project/init.sh [target_dir]
#
# Defaults to the current working directory. Refuses to overwrite existing
# AGENTS.md, CLAUDE.md, GEMINI.md or .shared/ — back them up or remove them
# first if you want to re-init.

set -euo pipefail

TEMPLATE_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
TARGET_DIR="${1:-$PWD}"

if [[ ! -d "$TARGET_DIR" ]]; then
    echo "error: target directory does not exist: $TARGET_DIR" >&2
    exit 1
fi

cd "$TARGET_DIR"

for path in AGENTS.md CLAUDE.md GEMINI.md .shared; do
    if [[ -e "$path" || -L "$path" ]]; then
        echo "error: $TARGET_DIR/$path already exists — refusing to overwrite" >&2
        exit 1
    fi
done

# 1. Canonical project guide
cp "$TEMPLATE_DIR/AGENTS.md" AGENTS.md

# 2. Tool-specific filenames as symlinks to AGENTS.md
ln -s AGENTS.md CLAUDE.md
ln -s AGENTS.md GEMINI.md

# 3. Shared skills/agents/commands tree (empty by default)
cp -r "$TEMPLATE_DIR/.shared" .shared
chmod +x .shared/scripts/build.py

# 4. Generate empty per-tool adapter dirs (so they are tracked early)
python3 .shared/scripts/build.py >/dev/null

cat <<EOF
Initialized AI agent toolkit in $TARGET_DIR

Next steps:
  1. Edit AGENTS.md — replace every "<!-- TODO: ... -->" block with
     project-specific content (build commands, architecture, conventions).
  2. Add skills under .shared/skills/<name>.md as the project grows.
     See .shared/README.md for the file format.
  3. Re-run \`python3 .shared/scripts/build.py\` after adding/removing
     skills to refresh the Gemini and Cursor adapters.
EOF
