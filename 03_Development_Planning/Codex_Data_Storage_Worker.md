# Codex Data-Storage Worker

## Worker Charter

`worker/data-storage` owns persistent session data. The role is to implement local storage for summaries, feature traces, and controlled research-mode landmark capture, while keeping shared contracts fixed and exposing stable APIs for the rest of the app.

## Owned Scope

- Module/file ownership: `data:session`
- Inputs: frozen summary and feature contracts
- Outputs: storage APIs, session history APIs, export artifacts
- Non-goal: changing shared contracts like `LandmarkFrame`, `SessionSummary`, or `WebViewPayload`

## Primary Responsibilities

- Define the storage schema.
- Implement Room entities and repositories.
- Build local persistence for session summaries.
- Persist feature traces.
- Gate raw landmark persistence behind research mode only.
- Define clear boundaries between normal session storage and research-only raw landmark storage.
- Support JSON export.
- Support CSV export.
- Add a storage-backed session history list.
- Define retention behavior.
- Define debug/export format.

## Architecture Decisions To Make

- Choose `Room` or a `file-plus-index hybrid`.
- Decide which records are canonical in DB vs generated on export.
- Decide how research-mode raw landmarks are separated from standard session summaries.
- Decide retention and cleanup policy.

## Acceptance Checklist

- `SessionSummary` can be persisted after a session.
- Feature traces can be persisted and retrieved.
- Raw landmark storage is disabled outside research mode.
- JSON export works for stored session outputs.
- CSV export works for stored session outputs.
- Session history can be listed from storage.
- Storage schema is stable enough for UI consumption.
- No shared contract types were modified by this worker.

## Suggested Task Breakdown

1. Freeze against existing contracts and define storage boundaries.
2. Pick storage strategy and document the decision.
3. Create entities/schema for summaries, features, and research-only landmarks.
4. Implement repository interfaces and local persistence.
5. Add session history query/list support.
6. Add JSON and CSV export utilities.
7. Add retention handling and debug export format.
8. Verify Merge Wave 2 criteria: persist features, generate summary, export raw/derived outputs.
