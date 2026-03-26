# Contract: Thread Info YAML

## Source
Bundled from the provided `thread-info-*.yaml` files.

## Purpose
Provides `thread-title` and `thread-link` (and potentially other metadata) used for display and for opening in browser.

## Observed Fields
Each document entry is an object with:

- `thread-title` (string)
- `thread-link` (string URL) — may be missing
- `created-by` (string) — optional for app use

## Rules

- `thread-title` should be treated as required.
- `thread-link` is optional. If missing or invalid, hide/disable link action.
