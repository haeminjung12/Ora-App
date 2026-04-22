# `data-session`

Foundation-integrated draft of the Track A `worker/data-storage` module.

## Scope

This module covers the storage responsibilities assigned in the planning docs:

- storage contracts for session summaries
- storage contracts for feature traces
- research-mode-only raw landmark storage
- export support for JSON / CSV session artifacts
- session history queries over the current store implementation
- stable storage-facing contracts for the rest of the Track A runtime

## Current contents

- `build.gradle.kts`
  Standalone Android library configuration aligned with the existing draft module style.
- `consumer-rules.pro`
  Placeholder consumer ProGuard rules file for future library shrinker requirements.
- `src/main/AndroidManifest.xml`
  Minimal manifest for the library scaffold.
- `contract/`
  Repository interfaces for summaries, feature traces, research-only landmark storage, and session history queries.
- `model/`
  Draft storage-facing models for session summaries, feature traces, landmark frames, setup readiness, and history entries.
- `runtime/InMemorySessionStore`
  In-memory draft that enforces research-mode landmark boundaries without claiming final persistence semantics.
- `export/`
  JSON and CSV export helpers over the storage draft models.

## Integration notes

- This module is now included in the foundation Android project and follows the shared version-catalog/module conventions.
- Expected eventual write scope is `data:session`.
- The foundation layer owns app-level DI wiring and startup initialization.
- The current runtime store is in-memory only; Room-backed persistence is not wired yet.
- This module should still avoid redefining shared contracts owned elsewhere; it is intended to persist Track A session and feature outputs once those contracts are finalized.
