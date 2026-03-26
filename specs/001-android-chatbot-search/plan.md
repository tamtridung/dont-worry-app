# Implementation Plan: Android Chat Thread Search

**Branch**: `[001-android-chatbot-search]` | **Date**: 2026-03-22 | **Spec**: [specs/001-android-chatbot-search/spec.md](spec.md)
**Input**: Feature specification from `specs/001-android-chatbot-search/spec.md`

**Note**: This template is filled in by the `/speckit.plan` command. See `.specify/templates/plan-template.md` for the execution workflow.

## Summary

Build a native Android app with a chat-like search UI that performs fully offline keyword search over bundled forum thread content, returns the top 10 relevant threads (excluding threads with empty responses), and provides a detail view to read full Q&A and open the original thread link in the system browser.

Technical approach (from research): Kotlin + Jetpack Compose, deterministic on-device lexical ranking (BM25-style), Vietnamese diacritics-insensitive normalization, and owner-editable synonym groups from a YAML file.

## Technical Context

**Language/Version**: Kotlin (Android)  
**Primary Dependencies**: Jetpack Compose (UI), AndroidX Navigation (screen transitions), Coroutines (async), YAML parser (for assets)  
**Storage**: Bundled YAML assets; optional lightweight local cache for parsed/indexed data (no network required)  
**Testing**: JUnit for unit tests; Compose UI tests for primary flows  
**Target Platform**: Android (offline-first)  
**Project Type**: Mobile app  
**Performance Goals**: Typical search returns results in ≤2 seconds for the bundled dataset  
**Constraints**: Offline-only search/ranking; no user accounts; no sending queries/data to third parties  
**Scale/Scope**: 2 screens (Search + Detail) + external link open; synonym YAML configuration

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

Constitution source: `.specify/memory/constitution.md`

- Gate A (Scope Discipline): PASS — plan sticks to 2 screens + link open only.
- Gate B (Offline-First): PASS — offline search/ranking and offline reading; network only for opening links.
- Gate C (Privacy): PASS — no accounts; no third-party query/data sharing in v1.
- Gate D (No crashes): PASS — explicit handling for missing fields/invalid YAML.

Post-Design Re-check (after Phase 1 artefacts): PASS — see `research.md`, `data-model.md`, and `contracts/`.

## Project Structure

### Documentation (this feature)

```text
specs/001-android-chatbot-search/
├── spec.md
├── plan.md
├── research.md
├── data-model.md
├── quickstart.md
├── contracts/
│   ├── README.md
│   ├── synonyms.yaml.contract.md
│   ├── thread-info.yaml.contract.md
│   └── thread-details.yaml.contract.md
└── tasks.md             # Phase 2 output (/speckit.tasks)
```

### Source Code (repository root)

```text
android/
├── app/
│   ├── src/main/
│   │   ├── java/...                # UI + domain + search
│   │   ├── assets/
│   │   │   ├── data/               # bundled YAML thread data
│   │   │   └── synonyms/           # owner-editable YAML synonyms (packaged in v1)
│   │   └── AndroidManifest.xml
│   └── src/test/                   # unit tests
└── settings.gradle / build.gradle   # standard Gradle structure

01_crawl_forum_threads_crawl4ai.py
02_crawl_thread_details_crawl4ai.py
data/                                # crawler outputs used to refresh bundled app assets
specs/
```

**Structure Decision**: Use a dedicated `android/` Gradle project for the mobile app, while keeping existing crawler scripts and raw datasets at repo root for data refresh workflows.

## Complexity Tracking

No constitution gate violations identified; no complexity justification required.
