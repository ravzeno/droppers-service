#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
export DROPPERS_DB="${DROPPERS_DB:-$HOME/.local/share/droppers/droppers.db}"
mkdir -p "$(dirname "$DROPPERS_DB")"
if [[ -x ./gradlew ]]; then exec ./gradlew bootRun; else exec gradle bootRun; fi
