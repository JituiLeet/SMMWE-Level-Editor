# SMMWE Level Editor

SMMWE level editor with a browser edition and an Android phone/tablet edition (Android 5.0 / API 21+).

## Features

- Grid-based canvas with paint, erase, select/pan, zoom, and touch pointer support.
- Level metadata: name, author, description, tags.
- Configurable map dimensions.
- Undo and redo.
- Portable `.smmweproj` project save/open.
- Import real `.swe` files, verify the sample-tested HMAC-SHA1 container, edit supported data layers, and export a newly checksummed `.swe`.
- SWE editor layers: terrain (`S0.S2`), objects (`S0.S4`), decorations (`S0.S3`), platforms (`S0.S5`), and doors (`S0.S8`).
- Untouched groups and extra per-object properties are retained. New object records are cloned from source-sample prototypes rather than invented from empty objects.

## Project layout

- `web/`: browser editor.
- `android/`: Android Studio app using the same editor code through WebViewAssetLoader.
- `core/`: shared project model, undo/redo, SWE parser/writer.
- `tests/`: SWE checksum and round-trip regression tests.
- `.github/workflows/android.yml`: manual/push Android debug APK build.
- `.github/workflows/pages.yml`: GitHub Pages deployment.

## Run the web edition

After enabling GitHub Pages and running **Deploy Web Editor**, the editor will be available at `https://jituiLeet.github.io/SMMWE-Level-Editor/web/`.

For local use, open `web/index.html` in a modern browser, or serve the repository root with a static HTTP server.

## Build Android

Open `android/` in Android Studio. Minimum SDK is 21 (Android 5.0). The GitHub Actions workflow builds a debug APK and uploads it as the `SMMWE-Level-Editor-debug` artifact.

## Deploy the web edition

In repository Settings → Pages, choose GitHub Actions as the source, then run **Deploy Web Editor** from the Actions tab.

## SWE compatibility and testing

The format adapter was built against the known-good `JituiLeet/Test/YINBI.swe` sample. The sample's HMAC-SHA1 digest was independently recomputed and matched. See [SWE compatibility notes](docs/SWE-COMPATIBILITY.md).

The app preserves unknown JSON groups and record properties. It exposes numeric terrain IDs and sample-derived object IDs without pretending that every ID's in-game meaning has been identified. Every exported file should still be imported into the target SMMWE game version before being treated as verified for release.
