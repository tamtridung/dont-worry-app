# Quickstart: Android Chat Thread Search

This quickstart is for running the Android app locally during development.

## Prerequisites (macOS)

- Android Studio (stable)
- Android SDK + an Android emulator image (or a physical Android device)
- JDK compatible with Android Studio (use the bundled JBR if available)

## Run Locally

1. Open the repository in Android Studio.
2. Open the Android project (planned location: `android/`).
3. Select an emulator device (or connect a phone with USB debugging enabled).
4. Click **Run**.

## Smoke Test

1. Launch app → see chat-like search UI.
2. Enter a keyword → see up to 10 results.
3. Tap a result → detail screen shows thread content + responses; responder names are emphasized.
4. Tap thread link → browser opens.
5. Back → returns to results.

## Smoke Test Record

Date: 2026-03-22

Environment checks completed in workspace:

- [x] Android source folder and navigation/search/detail implementation are present under `android/app/src/main/java/com/dontworry/app/`.
- [x] Bundled datasets and synonyms config are present in `android/app/src/main/assets/`.
- [x] IDE diagnostics check reports no current errors for `android/`.

Manual device/emulator checks to run in Android Studio:

- [ ] Launch app and confirm search screen renders.
- [ ] Search a keyword and confirm max 10 ranked results.
- [ ] Confirm results exclude threads with empty `responses`.
- [ ] Open detail and verify responder names are emphasized.
- [ ] Open thread link and confirm browser hand-off.
- [ ] Navigate back and verify prior search results are retained.

## Notes

- The app is designed to work offline with bundled YAML data.
- Threads with empty `responses` must not appear in results.
