# macOS Testing Guide

This guide walks a beginner through running the Android app in Android Studio on macOS.

## Prerequisites

- macOS 13+
- Android Studio (latest stable)
- Xcode command line tools (for some SDK tools)
- JDK 17 (Android Studio embedded JBR is fine)

## 0. First-Time Android Studio Setup (Recommended)

If this is your first Android app build, do these once:

1. Install Android Studio (stable channel).
2. Open Android Studio.
3. On the welcome screen, open **More Actions** → **SDK Manager**.
4. In **SDK Platforms**:
	- Install at least one recent platform (e.g., Android 14).
5. In **SDK Tools**:
	- Ensure **Android SDK Platform-Tools** is installed.
	- Ensure **Android SDK Build-Tools** is installed.
	- Ensure **Android Emulator** is installed (if you will use an emulator).
6. Click **Apply** to install.

Notes:

- You do not need to install a separate Java for Android Studio builds. Android Studio includes an embedded JDK (JBR) that works well.

## 1. Open and Sync Project

1. Open Android Studio.
2. Choose **Open** and select the `android/` folder.
3. If prompted about “Trust Project”, choose **Trust**.
4. Wait for Gradle sync to complete.

### 1.1 Fix the Most Common Setup Issue: Gradle JDK

If Gradle sync/build fails with messages about Java/JDK, set the Gradle JDK explicitly:

1. Android Studio → **Settings** (or **Preferences** on macOS).
2. **Build, Execution, Deployment** → **Build Tools** → **Gradle**.
3. Set **Gradle JDK** to **Embedded JDK (recommended)** (JBR 17).
4. Click **OK**.

Then click **File** → **Sync Project with Gradle Files**.

## 2. Configure Device for Testing

### Emulator

1. Open Device Manager.
2. Create a virtual device (Pixel 7 or similar).
3. Download a stable Android 14 image.
4. Start emulator.

If you do not see Device Manager:

- Android Studio → **Tools** → **Device Manager**.

### Physical Android Device

1. Enable Developer Options and USB Debugging.
2. Connect by USB.
3. Accept trust prompt on device.

## 3. Run Debug Build

1. Select `app` run configuration.
2. Select emulator or connected phone.
3. Click Run.

If you can’t find a run configuration:

1. In the top toolbar, choose the dropdown near the Run button.
2. Select **app**.
3. Choose a device (emulator/phone).
4. Click **Run**.

Where to see logs/output:

- **Build Output**: View → Tool Windows → Build
- **Device logs**: View → Tool Windows → Logcat

## 4. Smoke Test Checklist

- App launches successfully.
- Search screen appears with keyword field.
- Empty search shows prompt.
- Search with a common term returns up to 10 results.
- Tap a result opens detail view.
- Detail shows thread content and responses.
- Responder names are visually emphasized.
- Valid thread link opens external browser.
- Back from detail returns to search results.

## 5. Data Validation Checks

- Confirm threads with empty `responses` are excluded.
- Confirm app remains usable if synonyms YAML is missing/invalid.
- Confirm Vietnamese no-diacritic query can still match accented text.

## 6. Troubleshooting

- Gradle sync fails: check Android SDK path and JDK 17.
- Error `checkDebugAarMetadata` with AndroidX dependencies: ensure `android/gradle.properties` contains `android.useAndroidX=true` (and optionally `android.enableJetifier=true`).
- App install fails: clean project, then reinstall.
- No results: verify assets exist in `android/app/src/main/assets/data/`.

### Quick Fixes

- Clean build:
	- **Build** → **Clean Project**
	- **Build** → **Rebuild Project**
- If an SDK error mentions `sdk.dir`:
	- Android Studio → Settings/Preferences → **Android SDK** and ensure it points to a valid SDK location.
