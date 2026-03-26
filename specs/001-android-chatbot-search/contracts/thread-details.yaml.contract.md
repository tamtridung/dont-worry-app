# Contract: Thread Details YAML

## Source
Bundled from the provided `thread-details-*.yaml` files.

## Purpose
Provides `thread-content` and `responses` for search + detail rendering.

## Observed Fields
Each document entry is an object with:

- `thread-title` (string)
- `thread-id` (string) — may be missing
- `thread-content` (string) — may be missing in some entries
- `responses` (list) — may be empty

A `responses` item is expected to contain:

- `responser` (string)
- response text/content (string) — exact key may vary by extraction version; treat unknown keys defensively

## Rules

- Threads with `responses: []` must be excluded from search results.
- Missing `thread-content` should not crash the app; display a friendly placeholder.
- Response items missing `responser` should fall back to a neutral label.
