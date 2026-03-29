# dont-worry-app Constitution

## Core Principles

### I. Simplicity & Scope Discipline
- Prefer the smallest set of features that satisfies the spec.
- Avoid adding screens, flows, settings, or infrastructure not explicitly required.
- Any complexity beyond the spec must be justified in the plan’s Complexity Tracking table.

### II. Offline-First by Default
- The primary user journey (search → results → detail Q&A) must work offline using bundled data.
- Network access is allowed only for user-initiated actions that open external links in the system browser.

### III. Safety, Privacy, and Data Minimization
- Do not collect user accounts or personal data in the initial release.
- Avoid sending user queries or dataset content to third parties.
- Prefer on-device processing.

### IV. Testability & Determinism
- Core search behavior must be deterministic and testable with the bundled dataset.
- Edge cases (empty results, missing fields, invalid YAML) must have predictable behavior.

### V. Documentation as a Product Requirement
- The repo must include an owner-friendly guide for running, testing, and publishing on macOS.

## Quality Gates

- Gate A: Plan and implementation must not introduce non-required features.
- Gate B: Offline-first requirements are preserved.
- Gate C: No user PII collection in v1.
- Gate D: Failure modes are handled without crashes.

## Governance

- This constitution governs planning and implementation artifacts under `specs/`.
- Amendments must be recorded as a new section entry with date and rationale.

**Version**: 1.0.0 | **Ratified**: 2026-03-22 | **Last Amended**: 2026-03-22
