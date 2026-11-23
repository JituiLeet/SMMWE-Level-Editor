#!/usr/bin/env sh
set -eu
ROOT="$(CDPATH= cd -- "$(dirname -- "$0")/.." && pwd)"
DEST="$ROOT/android/app/src/main/assets/editor"
rm -rf "$DEST"
mkdir -p "$DEST/web" "$DEST/core"
cp -R "$ROOT/web/." "$DEST/web/"
cp -R "$ROOT/core/." "$DEST/core/"
# esbuild bundles modules and lowers modern syntax; Terser performs the final
# ES5 conversion because esbuild intentionally does not rewrite let/const.
npx --yes esbuild@0.24.2 "$DEST/web/app.js" --bundle --format=iife --target=es2015 --outfile="$DEST/web/app.intermediate.js"
npx --yes --package terser@5.37.0 terser "$DEST/web/app.intermediate.js" --ecma 5 --compress --mangle --format "ecma=5" --output "$DEST/web/app.bundle.js"
rm "$DEST/web/app.intermediate.js"
sed -i 's|<script type="module" src="./app.js"></script>|<script src="./app.bundle.js"></script>|' "$DEST/web/index.html"
