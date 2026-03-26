# Android Release Build and Signing Guide

## 1. Prepare App Metadata

Update in `android/app/build.gradle.kts`:

- `versionCode`
- `versionName`

Use semantic versioning for `versionName` and increment `versionCode` on every upload.

## 2. Create Signing Keystore

Run in terminal:

```bash
keytool -genkeypair -v \
  -keystore dontworry-release.jks \
  -alias dontworry \
  -keyalg RSA -keysize 2048 -validity 10000
```

Store keystore securely outside source control.

## 3. Configure Signing in Gradle

Add release signing config in `android/app/build.gradle.kts`:

- `storeFile`
- `storePassword`
- `keyAlias`
- `keyPassword`

Use environment variables for passwords instead of hardcoding.

## 4. Build Release Artifact

From `android/` directory:

```bash
./gradlew :app:bundleRelease
```

Expected output:

- `android/app/build/outputs/bundle/release/app-release.aab`

Optional APK build:

```bash
./gradlew :app:assembleRelease
```

## 5. Verify Release Build

- Install release APK on a device (if generated).
- Run smoke checklist from `docs/macos-testing.md`.
- Verify no debug-only logging or test data is exposed.

## 6. Pre-Submission Checklist

- Version updated.
- App icons and app name finalized.
- Privacy policy URL prepared (required for ads and Play policies).
- Crash-free smoke test completed.
