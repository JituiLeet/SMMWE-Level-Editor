#!/usr/bin/env sh
set -eu
ROOT="$(CDPATH= cd -- "$(dirname -- "$0")/.." && pwd)"
DEST="$ROOT/android/app/src/main/assets/editor"
rm -rf "$DEST"
mkdir -p "$DEST/web" "$DEST/core"
cp -R "$ROOT/web/." "$DEST/web/"
cp -R "$ROOT/core/." "$DEST/core/"
