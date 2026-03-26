# Contract: Synonyms / Abbreviations YAML

## Purpose
Owner-editable file that defines equivalences between abbreviations and full phrases.

## Proposed Format

Top-level key: `synonyms`

```yaml
synonyms:
  - canonical: "gái mại dâm"
    terms:
      - "gmd"
      - "gái md"
      - "gai mai dam"
```

## Rules

- `canonical` is the representative phrase for the group.
- `terms` contains variants that should be treated as equivalent.
- Search behavior normalizes terms using:
  - lowercase
  - remove diacritics
  - collapse whitespace
- If the YAML is missing/invalid:
  - app still works
  - app uses no synonym expansion (safe default)

## Backward Compatibility

- The format may be extended with optional metadata fields later (e.g., `notes`).
