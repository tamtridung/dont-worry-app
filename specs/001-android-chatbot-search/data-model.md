# Data Model: Android Chat Thread Search

## Entities

### Thread
Represents a forum thread rendered to the user.

**Fields**
- `threadId` (optional): string
- `threadTitle` (required): string
- `threadLink` (optional): string (URL)
- `threadContent` (optional): string
- `responses` (required): list of Response (may be empty in raw data; excluded from search results if empty)

**Derived / Index Fields**
- `normalizedTitle`: string
- `normalizedContent`: string
- `tokenCounts`: map<string, int> (for BM25-style scoring)

**Validation Rules**
- `threadTitle` must be present and non-empty
- `threadLink` must be a valid URL to be openable; otherwise show as plain text or hide link action

**Relationships**
- One `Thread` has many `Response`

### Response
Represents a reply within a thread.

**Fields**
- `responser` (required): string (display name)
- `content` (required): string

**Validation Rules**
- `responser` non-empty
- `content` non-empty

### SynonymGroup
Defines a set of terms treated as equivalent.

**Fields**
- `canonical` (required): string
- `terms` (required): list<string> (must include canonical or be equivalent after normalization)

**Validation Rules**
- No empty strings
- After normalization, terms should be unique within a group

## State / Flow Model

- **Search Screen**
  - Idle → Searching → ResultsShown OR NoResults
  - SelectingResult → Detail Screen

- **Detail Screen**
  - Viewing → OpenExternalLink (system browser)
  - Back → returns to prior ResultsShown state
