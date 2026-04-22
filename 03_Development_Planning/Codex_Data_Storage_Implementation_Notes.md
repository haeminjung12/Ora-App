# Codex Data Storage Implementation Notes

## Scope

This slice implements the first `data-session` draft module for the data-storage worker inside the foundation Android project.

Owned files:

- `data-session/src/main/kotlin/com/ora/data/session/export/*`
- `03_Development_Planning/Codex_Data_Storage_Implementation_Notes.md`

Explicit non-goals for this pass:

- no Room entity annotations or migrations yet
- no app or UI integration
- no additional root Gradle changes beyond the existing foundation wiring
- no edits to shared contracts outside the new storage draft module

## Assumptions

- The storage layer will persist session-level summary data, per-frame feature traces, and optional debug artifact references.
- The current `SessionSummary`, `FeatureFrame`, and `LandmarkFrame` models are draft storage-facing contracts derived from the planning docs and may still need alignment with the eventual contract-owner branch.
- Export should remain lightweight enough to work before serialization dependencies are chosen for the module.
- JSON and CSV outputs are intended for local export, debug inspection, and downstream tooling, not as a network API contract.
- Research-mode raw landmark persistence must stay clearly separated from normal session summaries.

## Implementation Shape

The module currently includes:

- storage-facing draft models for `SessionSummary`, `FeatureFrame`, `LandmarkFrame`, session history, and setup readiness
- repository contracts for summaries, feature traces, research-only landmark writes, and session history queries
- `InMemorySessionStore` as a local persistence draft that enforces research-mode write boundaries
- export helpers:
  - `SessionExportRecord`
  - `SessionJsonExporter`
  - `SessionCsvExporter`
  - `SessionExportBundleFactory`

Design choices:

- JSON is built manually with escaping to avoid adding a serialization dependency in this worker slice.
- CSV export is split by concern: session summaries, feature frames, and debug artifacts.
- Export now maps directly from the storage draft models instead of duplicating them in a second placeholder contract layer.
- Null artifact paths are emitted as empty CSV cells and `null` in JSON.
- Non-finite float values are normalized to `0.0` to avoid malformed exports.

## Scope Boundaries For Parallel Work

Parallel workers can proceed independently on:

- session history APIs
- UI consumption models
- Room schema and entities
- app wiring and DI
- file-backed persistence and export destinations

When those pieces land, this draft module should either be adopted as the storage contract surface or reconciled with the contract-owner branch before wider integration.

## Follow-Up Integration Tasks

1. Add `data-session` module Gradle wiring if the module is meant to compile immediately in the shared project.
2. Replace `InMemorySessionStore` with Room-backed persistence once the DB strategy is frozen.
3. Decide file naming conventions and export destination management.
4. Add tests for JSON escaping, CSV quoting, null handling, sort order, paging, and research-mode rejection behavior.
5. Define whether debug artifact paths remain relative paths or become URI/value objects.
6. Confirm whether feature-frame CSV should include additional quality or playback columns once the final feature schema is frozen.
7. Add landmark export separately only if research-mode export is approved.

## Handoff Notes

- The new code is isolated to the requested package to reduce merge conflicts.
- No existing files outside the allowed scope were edited.
- The current API is intentionally simple so other workers can wrap it without taking on extra dependencies.
