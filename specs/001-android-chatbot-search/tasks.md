---

description: "Task list for Android Chat Thread Search"
---

# Tasks: Android Chat Thread Search

**Input**: Design documents from `specs/001-android-chatbot-search/`

**Prerequisites**: `plan.md` (required), `spec.md` (required for user stories), plus optional `research.md`, `data-model.md`, `contracts/`, `quickstart.md`

**Tests**: Not included by default. Add test tasks later only if explicitly requested.

**Organization**: Tasks are grouped by user story so each story is independently implementable and testable.

## Format

- `[P]` = can be done in parallel (different files, no unfinished dependencies)
- `[US#]` = user story mapping
- Every task includes an exact file or directory path

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Create the Android project skeleton and bundle the initial dataset.

- [X] T001 Create Android project root in `android/` (Gradle + Android Studio project)
- [X] T002 Initialize Compose app module in `android/app/` (create `android/app/build.gradle.kts` + `android/settings.gradle.kts` + `android/build.gradle.kts`)
- [X] T003 Configure Compose + Navigation + Coroutines dependencies in `android/app/build.gradle.kts`
- [X] T004 Add YAML parsing dependency in `android/app/build.gradle.kts` (Android-compatible YAML parser)
- [X] T005 Create initial assets directories `android/app/src/main/assets/data/` and `android/app/src/main/assets/synonyms/`
- [X] T006 Copy raw datasets into app assets: `data/thread-details-2025-03-22.yaml` → `android/app/src/main/assets/data/thread-details.yaml`
- [X] T007 Copy raw datasets into app assets: `data/thread-info-2025-03-22.yaml` → `android/app/src/main/assets/data/thread-info.yaml`
- [X] T008 Create owner-editable synonyms file `android/app/src/main/assets/synonyms/synonyms.yaml` (match contract in `specs/001-android-chatbot-search/contracts/synonyms.yaml.contract.md`)

**Checkpoint**: Android project opens in Android Studio and builds a debug APK.

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Core offline data loading, normalization, indexing, and safe error handling that all user stories depend on.

**⚠️ CRITICAL**: No user story work should begin until this phase is complete.

- [X] T009 Define domain models `Thread` + `Response` in `android/app/src/main/java/com/dontworry/app/domain/model/Thread.kt`
- [X] T010 [P] Implement Vietnamese normalization utilities (lowercase, diacritics-insensitive, whitespace collapse) in `android/app/src/main/java/com/dontworry/app/domain/text/TextNormalizer.kt`
- [X] T011 [P] Implement YAML asset reader helper in `android/app/src/main/java/com/dontworry/app/data/assets/AssetReader.kt`
- [X] T012 [P] Implement thread-info YAML parsing (defensive) in `android/app/src/main/java/com/dontworry/app/data/yaml/ThreadInfoYamlParser.kt`
- [X] T013 [P] Implement thread-details YAML parsing (defensive, tolerate missing fields / varying response keys) in `android/app/src/main/java/com/dontworry/app/data/yaml/ThreadDetailsYamlParser.kt`
- [X] T014 Implement thread identity resolver (prefer `thread-id` → `thread-link` → normalized title) in `android/app/src/main/java/com/dontworry/app/domain/thread/ThreadIdentity.kt`
- [X] T015 Implement dataset merger (details + info → unified Thread list) in `android/app/src/main/java/com/dontworry/app/data/repo/ThreadRepository.kt`
- [X] T016 Enforce “exclude empty responses” rule at repository level in `android/app/src/main/java/com/dontworry/app/data/repo/ThreadRepository.kt`
- [X] T017 [P] Implement excerpt builder for result previews in `android/app/src/main/java/com/dontworry/app/domain/search/ExcerptBuilder.kt`
- [X] T018 [P] Implement deterministic lexical scorer (BM25-style) in `android/app/src/main/java/com/dontworry/app/domain/search/Bm25Scorer.kt`
- [X] T019 Implement search index builder (token counts + doc lengths) in `android/app/src/main/java/com/dontworry/app/domain/search/SearchIndex.kt`
- [X] T020 Implement query parser/tokenizer using `TextNormalizer` in `android/app/src/main/java/com/dontworry/app/domain/search/QueryTokenizer.kt`
- [X] T021 Implement search service (top 10 ranking; no synonym expansion yet) in `android/app/src/main/java/com/dontworry/app/domain/search/SearchService.kt`
- [X] T022 Create app entry + Compose nav host scaffold (Search + Detail routes, Detail can be placeholder) in `android/app/src/main/java/com/dontworry/app/ui/App.kt`
- [X] T023 Wire a simple app `MainActivity` to `App()` in `android/app/src/main/java/com/dontworry/app/MainActivity.kt`

**Checkpoint**: With mock UI wiring (even placeholder), repository can load assets and `SearchService` returns a deterministic top-10 list for a sample query.

---

## Phase 3: User Story 1 — Search by Chat Keyword (Priority: P1) 🎯 MVP

**Goal**: Chat-like search input that returns up to 10 relevant threads (title + excerpt), excluding threads with empty responses.

**Independent Test**: Launch app → enter a keyword → see up to 10 results; verify none have empty responses.

- [X] T024 [US1] Define UI state models (input, loading, results, empty) in `android/app/src/main/java/com/dontworry/app/ui/search/SearchUiState.kt`
- [X] T025 [US1] Implement `SearchViewModel` (load dataset once, run offline search) in `android/app/src/main/java/com/dontworry/app/ui/search/SearchViewModel.kt`
- [X] T026 [US1] Implement chat-like search screen UI (input row + message bubble + results list) in `android/app/src/main/java/com/dontworry/app/ui/search/SearchScreen.kt`
- [X] T027 [US1] Add empty/whitespace-only input guard (no search; prompt user) in `android/app/src/main/java/com/dontworry/app/ui/search/SearchViewModel.kt`
- [X] T028 [US1] Render each result as title + excerpt (from `ExcerptBuilder`) in `android/app/src/main/java/com/dontworry/app/ui/search/SearchResultRow.kt`
- [X] T029 [US1] Wire Search screen into nav graph route in `android/app/src/main/java/com/dontworry/app/ui/App.kt`

**Checkpoint**: US1 complete and usable as standalone MVP.

---

## Phase 4: User Story 2 — Read Full Q&A Thread (Priority: P2)

**Goal**: Tap a result to open a detail screen showing full thread content + responses (responder emphasized) and open the original link in browser.

**Independent Test**: From results → tap item → detail renders correctly → link opens (if present) → back returns to results.

- [X] T030 [US2] Define detail UI model + state in `android/app/src/main/java/com/dontworry/app/ui/detail/ThreadDetailUiState.kt`
- [X] T031 [US2] Implement `ThreadDetailViewModel` (resolve thread by identity, expose content + responses) in `android/app/src/main/java/com/dontworry/app/ui/detail/ThreadDetailViewModel.kt`
- [X] T032 [US2] Implement detail screen UI (thread content + responses Q&A list; emphasize responder names) in `android/app/src/main/java/com/dontworry/app/ui/detail/ThreadDetailScreen.kt`
- [X] T033 [US2] Add link row/button that opens `thread-link` via system browser Intent in `android/app/src/main/java/com/dontworry/app/ui/detail/OpenLink.kt`
- [X] T034 [US2] Navigate to detail from result click, preserving back stack in `android/app/src/main/java/com/dontworry/app/ui/App.kt`
- [X] T035 [US2] Handle missing/malformed links by hiding/disabled link action in `android/app/src/main/java/com/dontworry/app/ui/detail/ThreadDetailScreen.kt`

**Checkpoint**: US2 complete and does not lose prior results on back.

---

## Phase 5: User Story 3 — Abbreviation & Synonym-Aware Search (Priority: P3)

**Goal**: Owner-editable synonyms YAML improves query matching (abbreviation ↔ full phrase) with safe fallback behavior.

**Independent Test**: Configure synonym group in assets YAML → search using variants → comparable results; invalid YAML → app still searches (no expansion).

- [X] T036 [US3] Define `SynonymGroup` model in `android/app/src/main/java/com/dontworry/app/domain/model/SynonymGroup.kt`
- [X] T037 [US3] Implement synonyms YAML parsing with safe fallback (missing/invalid YAML → no expansion) in `android/app/src/main/java/com/dontworry/app/data/yaml/SynonymsYamlParser.kt`
- [X] T038 [US3] Load and cache synonym groups from `android/app/src/main/assets/synonyms/synonyms.yaml` in `android/app/src/main/java/com/dontworry/app/data/repo/SynonymsRepository.kt`
- [X] T039 [US3] Implement synonym expansion for queries (using loaded SynonymGroup list) in `android/app/src/main/java/com/dontworry/app/domain/search/SynonymExpander.kt`
- [X] T040 [US3] Add conflict handling for terms present in multiple synonym groups (deterministic rule + no crash) in `android/app/src/main/java/com/dontworry/app/domain/search/SynonymExpander.kt`
- [X] T041 [US3] Integrate synonym expansion into search flow (tokenization → expansion → scoring) in `android/app/src/main/java/com/dontworry/app/domain/search/SearchService.kt`
- [X] T042 [US3] Add “synonyms disabled” safe mode when YAML invalid (log/flag state, but keep UX minimal) in `android/app/src/main/java/com/dontworry/app/data/yaml/SynonymsYamlParser.kt`

**Checkpoint**: US3 complete with safe fallback on YAML errors.

---

## Phase 6: User Story 4 — Owner Can Test & Publish (Priority: P4)

**Goal**: Provide a macOS-friendly guide to test, produce a release build, publish, and set up ads monetization.

**Independent Test**: A new contributor can follow the guide on macOS and run the app + produce a signed release AAB/APK (as applicable) and understand Play Console + AdMob setup steps.

- [X] T043 [US4] Create macOS run/test guide in `docs/macos-testing.md` (emulator + physical device + smoke test steps)
- [X] T044 [US4] Create release build + signing guide in `docs/android-release-build.md` (keystore, versioning, generating release artifact)
- [X] T045 [US4] Create Play Store submission checklist in `docs/google-play-publishing.md` (Play Console steps, screenshots, internal testing track)
- [X] T046 [US4] Create ads monetization guide in `docs/admob-monetization.md` (AdMob account/app unit IDs, policy notes, integration outline)

**Checkpoint**: US4 docs enable an owner to ship and monetize.

---

## Phase 7: Polish & Cross-Cutting Concerns

**Purpose**: Final tightening without adding scope beyond the spec.

- [X] T047 [P] Improve empty-state UX (“no results”, “enter keyword”) in `android/app/src/main/java/com/dontworry/app/ui/search/SearchScreen.kt`
- [X] T048 Add performance guardrails (index once, reuse in-memory) in `android/app/src/main/java/com/dontworry/app/ui/search/SearchViewModel.kt`
- [X] T049 Ensure missing data never crashes UI (placeholders for missing content/responser) in `android/app/src/main/java/com/dontworry/app/ui/detail/ThreadDetailScreen.kt`
- [X] T050 Run the smoke test checklist and update if needed in `specs/001-android-chatbot-search/quickstart.md`

---

## Dependencies & Execution Order

### Phase Dependencies

- **Phase 1 (Setup)** → required before everything else.
- **Phase 2 (Foundational)** → blocks all user stories.
- **Phase 3 (US1)** → MVP after Phase 2.
- **Phase 4 (US2)** → depends on Phase 2 + US1 navigation entry points.
- **Phase 5 (US3)** → depends on Phase 2 search pipeline.
- **Phase 6 (US4)** → can be written in parallel with implementation, but verify once the app exists.
- **Phase 7 (Polish)** → after US1–US3 as needed.

### User Story Completion Order

US1 → US2 → US3 → US4

---

## Dependency Graph

Setup (Phase 1) → Foundational (Phase 2) → US1 → US2 → US3 → US4 → Polish

---

## Parallel Execution Examples

### Setup (Phase 1)

- Run in parallel:
  - T006 (copy thread-details asset) and T007 (copy thread-info asset)
  - T008 (create synonyms.yaml) while dependencies are being configured

### Foundational (Phase 2)

- Run in parallel:
  - T010 (TextNormalizer) and T011 (AssetReader)
  - T012 (ThreadInfo parser) and T013 (ThreadDetails parser)

### US1 (Phase 3)

- Run in parallel:
  - T024 (UI state) and T028 (result row component)

### US2 (Phase 4)

- Run in parallel:
  - T030 (detail UI state) and T033 (open-link helper)

### US3 (Phase 5)

- Run in parallel:
  - T036 (model) and T037 (YAML parser)
  - T038 (repository) and T039 (expander)

### US4 Documentation

- Run in parallel:
  - T043–T046 (docs) can be authored independently

---

## Implementation Strategy

### MVP First

1. Complete Phase 1 + Phase 2
2. Complete **US1** (Phase 3) and stop for a real-device smoke test

### Incremental Delivery

1. Add **US2** (detail + open link)
2. Add **US3** (synonyms)
3. Finish **US4** (publish + ads docs)
