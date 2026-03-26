# Research: Android Chat Thread Search

This document resolves planning-time unknowns and records technology decisions.

## Decisions

### 1) Platform & UI
- **Decision**: Native Android app using **Kotlin** + **Jetpack Compose**.
- **Rationale**: Best-in-class Android UX, simple navigation, smooth subtle motion; fits “clean, Apple-like” requirement without heavy theming.
- **Alternatives considered**:
  - Flutter: strong cross-platform, but adds stack complexity and larger dependency surface.
  - React Native: similar tradeoffs; not necessary for Android-only scope.

### 2) Offline Search / Ranking
- **Decision**: On-device search using a deterministic lexical ranking approach (BM25-style scoring) over normalized text.
- **Rationale**: Works fully offline, fast on mid-range devices, no third-party data transfer, easy to unit test.
- **Alternatives considered**:
  - SQLite FTS5: good performance, but diacritics-insensitive Vietnamese matching and custom synonym expansion can be trickier.
  - Embeddings/LLM-based semantic search: would require model hosting or on-device model packaging; increases size/cost/complexity and risks privacy.

### 3) Vietnamese Normalization
- **Decision**: Search normalization is **case-insensitive** and **diacritics-insensitive**.
- **Rationale**: Mobile users often type without Vietnamese accents; improves recall.
- **Alternatives considered**:
  - Diacritics-sensitive matching only: simpler but produces frequent “no results”.

### 4) Synonyms / Abbreviations Configuration
- **Decision**: Owner-editable **YAML** file defines synonym groups; search expands query terms using these groups.
- **Rationale**: Meets requirement: “gái mại dâm” ↔ “gmd” ↔ “gái md”, etc., without code changes.
- **Alternatives considered**:
  - Hardcoded synonym map in code: violates owner-editable requirement.

### 5) Data Packaging & Identity
- **Decision**: Bundle both provided thread YAML files inside the app; build a unified Thread dataset by preferring `thread-id`, else `thread-link`, else normalized `thread-title`.
- **Rationale**: Keeps offline-first and deterministic behavior; tolerates missing fields.
- **Alternatives considered**:
  - Backend sync: not required and violates offline-first goal.

### 6) Parsing YAML on Android
- **Decision**: Use a mature YAML parser library for Android to parse data from assets.
- **Rationale**: Reliability and schema tolerance.
- **Alternatives considered**:
  - Custom YAML parsing: error-prone.

### 7) Ads Monetization
- **Decision**: Use a mainstream ad network SDK (Google AdMob) for banner/interstitial as a later implementation step.
- **Rationale**: Standard path for Android monetization.
- **Alternatives considered**:
  - No ads: does not meet monetization requirement.

## Open Issues

None blocking Phase 1 design.
