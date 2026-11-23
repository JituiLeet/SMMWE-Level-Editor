# SMMWE Level Editor

Cross-platform level editor for SMMWE: browser edition and Android phone/tablet edition (Android 5.0 / API 21+).

## Project layout

- `web/`: browser editor.
- `android/`: Android Studio application; packages the same web editor into a native WebView shell.
- `core/`: shared project model, history, and SWE container/schema adapters.
- `.github/workflows/`: manual Android APK build and Pages deployment.

## Current implementation scope

The editor supports a grid canvas, tile palette, paint/erase/select, zoom/pan, level metadata, resize, undo/redo, and portable `.smmweproj` save/open.

SWE compatibility is deliberately guarded. The file container helper checks the candidate Base64 + HMAC-SHA1 layout, but the adapter must preserve unknown groups and only edit fields confirmed by real SMMWE samples. A successful checksum alone does not prove that a generated file is accepted by the game. The UI must not label an unverified export as game-compatible.

## Run the web edition

Open `web/index.html` in a modern browser, or serve the repository root with any static HTTP server.

## Build Android

Open `android/` in Android Studio. Minimum SDK is 21 (Android 5.0). CI builds a debug APK through the **Android APK** workflow.

## Deploy web edition

Enable GitHub Pages with GitHub Actions as the source, then run the **Deploy Web Editor** workflow.

## Development status

This repository is the active source of truth. Do not claim a release APK or fully verified SWE writer until CI and in-game import tests have passed.
