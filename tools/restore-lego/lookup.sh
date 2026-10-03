#!/usr/bin/env bash
# Prints intermediary -> Mojang name pairs for the identifiers listed in
# lookup-names.txt, using the tiny mappings Loom generated for this build.
# Only the requested lines are printed (no mapping files are redistributed).
set -euo pipefail
cd "$(dirname "$0")"
FILES=$(find "$HOME/.gradle/caches/fabric-loom" .gradle -name '*.tiny' 2>/dev/null | sort -u)
echo "tiny files:"; echo "$FILES"
for f in $FILES; do head -1 "$f"; done
PATTERN=$(grep -v '^#' lookup-names.txt | grep -v '^$' | sed 's/.*/\t&(\t|$)/' | paste -sd'|' -)
for f in $FILES; do
  echo "== $f"
  grep -P "$PATTERN" "$f" | sort -u || true
done
