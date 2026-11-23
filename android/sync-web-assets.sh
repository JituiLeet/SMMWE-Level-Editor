#!/usr/bin/env sh
set -eu
ROOT="$(CDPATH= cd -- "$(dirname -- "$0")/.." && pwd)"
DEST="$ROOT/android/app/src/main/assets/editor"
rm -rf "$DEST"
mkdir -p "$DEST/web" "$DEST/core"
cp -R "$ROOT/web/." "$DEST/web/"
cp -R "$ROOT/core/." "$DEST/core/"
# Bundle ES modules and downlevel syntax for older Android System WebView versions.
npx --yes esbuild@0.24.2 "$DEST/web/app.js" --bundle --format=iife --target=chrome37 --outfile="$DEST/web/app.bundle.js"
sed -i 's|<script type="module" src="./app.js"></script>|<script src="./app.bundle.js"></script>|' "$DEST/web/index.html"
