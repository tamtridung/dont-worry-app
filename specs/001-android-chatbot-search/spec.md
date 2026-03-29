# Feature Specification: Android Chat Thread Search

**Feature Branch**: `[001-android-chatbot-search]`  
**Created**: 2026-03-22  
**Status**: Draft  
**Input**: User description: "Android app dạng chatbot: người dùng nhập từ khoá để tra cứu nội dung các thread, trả về top 10 thread liên quan (bỏ thread không có trả lời), xem chi tiết hội thoại và mở link gốc; hỗ trợ từ viết tắt/đồng nghĩa qua file YAML; UI tối giản, thanh lịch." 

## Clarifications

### Session 2026-03-22

- Q: Should search/ranking be offline or online? → A: Offline-only in v1 (no network required for search/ranking).

## User Scenarios & Testing *(mandatory)*

<!--
  IMPORTANT: User stories should be PRIORITIZED as user journeys ordered by importance.
  Each user story/journey must be INDEPENDENTLY TESTABLE - meaning if you implement just ONE of them,
  you should still have a viable MVP (Minimum Viable Product) that delivers value.
  
  Assign priorities (P1, P2, P3, etc.) to each story, where P1 is the most critical.
  Think of each story as a standalone slice of functionality that can be:
  - Developed independently
  - Tested independently
  - Deployed independently
  - Demonstrated to users independently
-->

### User Story 1 - Search by Chat Keyword (Priority: P1)

As a user, I can type a keyword/question into a chat-like input and immediately receive up to 10 of the most relevant threads (title + content excerpt) so I can quickly self-serve answers.

**Why this priority**: This is the core value of the app: turning a large set of forum discussions into fast, readable answers.

**Independent Test**: Can be fully tested by loading the included dataset, entering a keyword, and verifying that a ranked list of up to 10 results is shown and that none of the results come from threads with no replies.

**Acceptance Scenarios**:

1. **Given** the app has thread details available, **When** the user submits a non-empty keyword in the chat input, **Then** the app shows up to 10 results ranked by relevance.
2. **Given** some threads have an empty `responses` list, **When** the user searches, **Then** those threads are excluded from the results.
3. **Given** there are fewer than 10 eligible matches, **When** the user searches, **Then** the app shows all available matches (and clearly indicates when there are no results).
4. **Given** the user submits an empty or whitespace-only message, **When** the search is triggered, **Then** the app prompts the user to enter a keyword and does not run a search.

---

### User Story 2 - Read Full Q&A Thread (Priority: P2)

As a user, I can tap a search result to open a detail screen that shows the original thread content and the full set of replies (with responder names highlighted), and I can open the original thread link in my phone browser.

**Why this priority**: Search results alone are often not enough; users need to read the full back-and-forth to feel confident and see context.

**Independent Test**: Can be tested by tapping any result and confirming the detail screen renders thread content + replies, includes a working link action, and supports back navigation without losing the prior results.

**Acceptance Scenarios**:

1. **Given** the user sees the top results list, **When** the user taps a thread title, **Then** the app navigates to a detail screen for that thread.
2. **Given** the detail screen is open, **When** it is displayed, **Then** it shows the thread content followed by its responses in a readable Q&A format.
3. **Given** the detail screen shows responses, **When** responder names are shown, **Then** each responder name is visually emphasized to distinguish speakers.
4. **Given** the thread has a `thread-link`, **When** the user taps the link, **Then** the phone opens the URL in the default browser.
5. **Given** the user is on the detail screen, **When** the user taps back, **Then** the app returns to the prior screen with the previous search results preserved.

---

### User Story 3 - Abbreviation & Synonym-Aware Search (Priority: P3)

As a user, I can search using abbreviations or alternative terms (e.g., “gmd”, “gái md”, “gái mại dâm”) and still get results that are as relevant as if I typed the full phrase.

As the app owner, I can update a YAML file that defines abbreviations/synonyms without changing the app’s code.

**Why this priority**: Users frequently type shorthand, especially on mobile; abbreviation support improves match quality and reduces anxiety from “no results.”

**Independent Test**: Can be tested by configuring a small set of synonym groups in the YAML file and verifying that each variant returns comparable results.

**Acceptance Scenarios**:

1. **Given** a synonym/abbreviation group is configured in the YAML file, **When** the user searches using any term in the group, **Then** results are consistent in relevance across variants.
2. **Given** the YAML file is missing or invalid, **When** the app starts or search runs, **Then** the app continues to function using a safe default behavior and informs the owner/user appropriately.

---

### User Story 4 - Owner Can Test & Publish (Priority: P4)

As the app owner (non-Android developer), I can follow a step-by-step guide on macOS to run and test the app (emulator and/or a physical device) and to publish it to an app store and enable ad monetization.

**Why this priority**: The app is only valuable if it can be tested, shipped, and monetized by the owner.

**Independent Test**: Can be tested by a new contributor following the guide from a clean macOS setup and successfully running a local build and a basic end-to-end test.

**Acceptance Scenarios**:

1. **Given** the repository and guide are available, **When** the owner follows the guide on macOS, **Then** they can run the app locally and validate core flows.
2. **Given** the owner wants ad monetization, **When** they follow the publishing steps, **Then** they can produce a release build ready for store submission and understand the required store and ad setup steps.

### Edge Cases

- No eligible results (e.g., all matches have empty `responses`)
- Very short queries (1–2 characters) that are likely too broad
- Queries containing only punctuation, emojis, or whitespace
- Vietnamese spelling variations (with/without diacritics) affecting search expectations
- Missing fields in data (missing `thread-link`, missing `thread-id`, missing `thread-content`)
- Extremely long thread content or many responses (ensure readable layout and stable scrolling)
- Duplicate or near-duplicate threads in the dataset
- Abbreviation YAML includes conflicting mappings (same term in multiple groups)
- Thread link is malformed or cannot be opened

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: The app MUST provide a chat-like interface with an input box for the user to submit a keyword/question.
- **FR-002**: When the user submits `user_keyword`, the app MUST search across available `thread-content` and rank results by relevance.
- **FR-003**: The app MUST return up to 10 results and MUST exclude any thread where `responses` is empty.
- **FR-004**: Each result MUST display the corresponding `thread-title` and a readable excerpt of `thread-content` sufficient to judge relevance.
- **FR-005**: The app MUST allow the user to select a result and navigate to a detail screen for that thread.
- **FR-006**: The detail screen MUST display the full `thread-content` and all items in `responses` in a clear Q&A format.
- **FR-007**: The detail screen MUST visually emphasize each `responser` so users can distinguish speakers at a glance.
- **FR-008**: The detail screen MUST display `thread-link` (when available) and allow the user to open it in the phone browser.
- **FR-009**: The detail screen MUST provide a clear back navigation that returns to the previous screen and preserves the prior results view.
- **FR-010**: The app MUST support abbreviation/synonym handling so that shorthand queries can match full-term content and vice versa.
- **FR-011**: The abbreviation/synonym mapping MUST be owner-editable via a YAML file.
- **FR-012**: The app MUST handle missing/partial data gracefully (e.g., missing link, missing content) without crashing.
- **FR-013**: The app MUST work without requiring user accounts.
- **FR-014**: The primary search and reading experience MUST be usable offline with the bundled dataset.
- **FR-014a**: Search/ranking MUST NOT require any network connection in the initial release.
- **FR-015**: The app MUST feel visually simple, calm, and “Apple-like” in polish (clean typography, restrained color, subtle motion), without distracting UI elements.
- **FR-016**: The repository MUST include a step-by-step macOS guide for running tests locally and for publishing the app and enabling ad monetization.
- **FR-017**: Search MUST be case-insensitive and diacritics-insensitive for Vietnamese text (e.g., “gai mai dam” matches “gái mại dâm”).
- **FR-018**: The app MUST build a unified dataset from the provided thread files such that:
  - `thread-content` and `responses` come from thread details data
  - `thread-link` comes from thread info data when present
  - Missing fields do not prevent a thread from being searchable (unless excluded by empty `responses`)

### Key Entities *(include if feature involves data)*

- **Thread**: A discussion item with `thread-title`, `thread-link` (optional), `thread-content`, and `responses`.
- **Thread Identity**: Prefer `thread-id` when present; otherwise use `thread-link`; otherwise fall back to a normalized `thread-title` (as a last resort).
- **Response**: A reply within a thread with `responser` (display name) and response text/content.
- **Synonym Group**: A set of terms that should be treated as equivalent for search (stored in a YAML file).
- **Search Result**: A thread plus a relevance score and a display excerpt.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: In a usability test (n≥10), at least 90% of participants can find and open a relevant thread from a keyword search within 60 seconds.
- **SC-002**: For the bundled dataset, at least 95% of searches show results (or a “no results” message) within 2 seconds on a typical mid-range Android phone.
- **SC-003**: In a curated evaluation set (n≥50 queries), at least 90% of queries return results judged “relevant” by the owner.
- **SC-004**: In exploratory testing covering the main flows for 30 minutes, the app experiences 0 crashes.
- **SC-005**: For configured synonym groups, abbreviation-form queries produce comparable top results to full-term queries in at least 90% of sampled cases.
- **SC-006**: In a visual polish review (n≥10), at least 80% of participants rate the UI as “clean and calming” (≥4/5).

## Assumptions

- The dataset is pre-packaged with the app and updated via app releases (no in-app data syncing in the initial scope).
- The primary audience reads Vietnamese.
- The app aims to reduce anxiety and confusion by showing real Q&A threads; it is not a substitute for medical diagnosis.

## Out of Scope (Initial Release)

- User accounts, community posting, or messaging with real clinicians
- Personalized medical recommendations or emergency guidance
- Complex browsing features (filters, multiple categories, saved collections)
