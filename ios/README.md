# iOS App Scaffold

This folder contains the initial iOS implementation for Dont Worry using SwiftUI (iOS 16+), targeting iPhone X and newer.

## Current scope

- Search screen with query input and paged results
- Suggested thread list
- Thread detail screen with responses
- Open original thread link in Safari
- Offline cache for parsed thread data
- YAML parsing compatible with Android dataset structure

## Project generation

This iOS project is defined with XcodeGen.

1. Install XcodeGen (once):
   - brew install xcodegen
2. Generate Xcode project from this folder:
   - cd ios
   - xcodegen generate
3. Open generated project:
   - open DontWorryiOS.xcodeproj

## Build and test prerequisites

- Full Xcode app is required for `xcodebuild` (Command Line Tools only is not enough).
- If `xcodebuild` points to CommandLineTools, switch developer directory:
   - sudo xcode-select -s /Applications/Xcode.app/Contents/Developer
- Accept Xcode license once:
   - sudo xcodebuild -license accept

## Troubleshooting simulator runtime

- If `xcrun simctl list runtimes` shows empty, open Xcode and install iOS Simulator runtime:
   - Xcode -> Settings -> Components (or Platforms) -> install `iOS Simulator` matching your Xcode version.
- Then create/boot a simulator once from Xcode -> Open Developer Tool -> Simulator.
- Verify from terminal:
   - xcrun simctl list devices available

## Run tests

- Build (simulator SDK):
   - xcodebuild -project DontWorryiOS.xcodeproj -scheme DontWorryiOS -configuration Debug -sdk iphonesimulator build
- Test (after simulator is available):
   - xcodebuild -project DontWorryiOS.xcodeproj -scheme DontWorryiOS -configuration Debug -destination 'platform=iOS Simulator,name=iPhone 16' test

## Notes

- Data files are bundled from:
  - Resources/data/thread-info.yaml
  - Resources/data/thread-details.yaml
  - Resources/synonyms/synonyms.yaml
- Search behavior mirrors Android core logic: normalization, tokenization, synonym expansion, and BM25 ranking.
