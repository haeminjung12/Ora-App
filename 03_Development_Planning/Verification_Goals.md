# Verification Goals

Date: 2026-04-22
Project: Oral-D Track A
Purpose: define which verification agents should run now, what each one must check, and the exact command text to send them

## Verification Log Rule

- This document is the canonical rolling verifier log for the project.
- Every verifier must append its result to this document after each run.
- Every future verifier prompt and every manual verifier run must treat appending to this file as required output, not an optional follow-up task.
- Add new runs at the end of the file under `## Verification Run Log`.
- Never overwrite or delete prior verifier entries unless the user explicitly asks for cleanup.
- Each appended entry must include: date, verifier name, scope, build/test command if used, verdict, and findings with concrete file references.

## Verification Order

Run verification in this order:

1. `verifier/runtime`
2. `verifier/data-ui`
3. `verifier/final-integration`

Run `validation/prototype-evaluator` only if a real validation toolkit, replay path, or annotation pipeline already exists.

## 1. `verifier/runtime`

### Goal

Verify that the runtime-facing modules are integration-ready against the new Android foundation baseline.

### Scope

- `:feature-player`
- `:feature-camera`
- runtime wiring in `:app`

### Must Check

- project builds cleanly from the current baseline
- `feature-player` and `feature-camera` are registered correctly in the module graph
- playback and camera responsibilities remain cleanly separated
- runtime contracts are coherent and not duplicative
- session lifecycle semantics are defensible
- playback event ordering is sane
- camera lifecycle and permission handling are sane
- no obvious integration blockers remain for runtime spine work

### Reject If

- contract meaning is ambiguous
- runtime modules assume conflicting ownership
- player and camera integration would require contract rewrites
- lifecycle handling is obviously unsafe or inconsistent
- the current app scaffold cannot sensibly host the runtime flow

### Agent Command

```text
Review the current Track A Android baseline as verifier/runtime.

Scope:
- :feature-player
- :feature-camera
- runtime-related wiring in :app

Goals:
- verify build and integration readiness
- check contract coherence
- check player lifecycle semantics
- check camera lifecycle semantics
- check whether playback and camera can coexist cleanly in the runtime spine

Report:
- findings first, ordered by severity
- include concrete file references
- explicitly state whether runtime integration is acceptable, blocked, or requires contract cleanup
- append the verification result to `03_Development_Planning/Verification_Goals.md` under `## Verification Run Log`

Do not implement changes unless explicitly asked.
```

## 2. `verifier/data-ui`

### Goal

Verify that data, summary, and UI-facing assumptions are coherent and usable from the current baseline.

### Scope

- `:data-session`
- UI-facing state usage in `:app`
- any currently integrated summary or contract surfaces that feed future UI and storage work

### Must Check

- session models are internally coherent
- summary and frame models match intended prototype outputs
- persistence interfaces are usable by downstream modules
- research-mode storage boundaries are explicit
- UI-facing assumptions do not conflict with stored model semantics
- current module boundaries are good enough for feature and setup workers to continue

### Reject If

- session models are unstable or contradictory
- storage interfaces are too vague for downstream use
- research and normal-mode data are mixed carelessly
- UI/state contracts would need rewrites before feature work can continue

### Agent Command

```text
Review the current Track A Android baseline as verifier/data-ui.

Scope:
- :data-session
- app-level UI/state wiring that depends on current contracts

Goals:
- verify model coherence
- verify persistence and summary interfaces
- verify research-mode storage boundaries
- verify downstream UI/data work can continue without contract churn

Report:
- findings first, ordered by severity
- include concrete file references
- explicitly state whether the data/UI layer is acceptable, blocked, or requires contract cleanup
- append the verification result to `03_Development_Planning/Verification_Goals.md` under `## Verification Run Log`

Do not implement changes unless explicitly asked.
```

## 3. `verifier/final-integration`

### Goal

Verify that the assembled baseline behaves like a real frozen foundation branch rather than a set of disconnected worker outputs.

### Scope

- full root project
- `:app`
- `:core-contracts`
- `:feature-player`
- `:feature-camera`
- `:data-session`

### Must Check

- fresh build works
- root Gradle project is coherent
- module registration is correct
- app shell is valid as a baseline
- no critical path is still just a disconnected placeholder
- there is no obvious contract drift across modules
- no claim-language violations appear in current UI text
- branch is suitable as the frozen baseline for future workers

### Reject If

- foundation project is not actually stable
- module graph is incomplete or misleading
- baseline cannot support downstream worker rebases
- current app shell hides unresolved integration defects

### Agent Command

```text
Review the current Track A Android baseline as verifier/final-integration.

Scope:
- full root project
- :app
- :core-contracts
- :feature-player
- :feature-camera
- :data-session

Goals:
- verify the project works as a true frozen foundation baseline
- verify build and module registration
- verify app shell validity
- verify there is no contract drift across integrated modules
- verify downstream workers can now rebase onto this baseline safely

Report:
- findings first, ordered by severity
- include concrete file references
- explicitly state whether this branch is acceptable as the frozen baseline
- append the verification result to `03_Development_Planning/Verification_Goals.md` under `## Verification Run Log`

Do not implement changes unless explicitly asked.
```

## Optional: `validation/prototype-evaluator`

Only run this if a real validation toolkit already exists.

### Goal

Check whether the prototype outputs are testable and meaningful for internal validation, not just whether the code compiles.

### Scope

- replay tooling
- annotation schema
- comparison utilities
- validation reports
- stored session outputs used for evaluation

### Agent Command

```text
Review the current Track A Android prototype as validation/prototype-evaluator.

Scope:
- validation toolkit, replay tooling, annotation schema, and evaluation outputs that already exist

Goals:
- verify replayability
- verify annotation schema quality
- verify comparison logic
- verify reported metrics are aligned with allowed product claims
- verify the prototype is ready for internal validation runs

Report:
- findings first, ordered by severity
- include concrete file references
- explicitly state whether validation work can begin or whether toolkit gaps still block it
- append the verification result to `03_Development_Planning/Verification_Goals.md` under `## Verification Run Log`

Do not implement changes unless explicitly asked.
```

## Verification Run Log

### 2026-04-22 - `verifier/runtime`

Scope:

- `:feature-player`
- `:feature-camera`
- runtime-related wiring in `:app`

Build command:

```text
./gradlew :feature-player:assembleDebug :feature-camera:assembleDebug :app:assembleDebug
```

Build result:

- Success

Verdict:

- Runtime integration is acceptable for the current baseline, but it still requires contract cleanup.

Findings:

- Medium: The runtime spine stops camera when player stop events arrive, but it does not restart camera if playback restarts within the same session. See [app/src/main/java/com/ora/app/MainActivity.kt](/Users/haeminjung/Mac_Portable_Pack_2026-04-21/app/src/main/java/com/ora/app/MainActivity.kt:153), [app/src/main/java/com/ora/app/MainActivity.kt](/Users/haeminjung/Mac_Portable_Pack_2026-04-21/app/src/main/java/com/ora/app/MainActivity.kt:156), [feature-player/src/main/kotlin/com/ora/feature/player/runtime/Media3PlayerSessionController.kt](/Users/haeminjung/Mac_Portable_Pack_2026-04-21/feature-player/src/main/kotlin/com/ora/feature/player/runtime/Media3PlayerSessionController.kt:66), and [feature-player/src/main/kotlin/com/ora/feature/player/ui/VideoSessionScreen.kt](/Users/haeminjung/Mac_Portable_Pack_2026-04-21/feature-player/src/main/kotlin/com/ora/feature/player/ui/VideoSessionScreen.kt:213).

Notes:

- The previous hard blockers observed earlier on 2026-04-22 were resolved in the current source state: `:app` now owns both runtime components, player `READY` vs `PAUSED` mapping is coherent, and camera lifecycle state is bound to host lifecycle events.
- No relevant tests were found in `:app`, `:feature-player`, or `:feature-camera`, so this run is build-backed and source-reviewed rather than test-backed.

### 2026-04-22 01:36:22 CDT - `verifier/data-ui`

Scope:

- `:data-session`
- app-level UI/state wiring that depends on current contracts

Build/test command:

```text
./gradlew :data-session:testDebugUnitTest :data-session:assembleDebug :app:assembleDebug
```

Build/test result:

- Success

Verdict:

- The data/UI layer is acceptable.

Findings:

- None.

Notes:

- The previously reported `verifier/data-ui` issues from earlier on 2026-04-22 were resolved in the current source state. Research-mode landmark reads are now policy-gated in [data-session/src/main/kotlin/com/ora/data/session/contract/SessionStoreContracts.kt](/Users/haeminjung/Mac_Portable_Pack_2026-04-21/data-session/src/main/kotlin/com/ora/data/session/contract/SessionStoreContracts.kt:20) and [data-session/src/main/kotlin/com/ora/data/session/runtime/InMemorySessionStore.kt](/Users/haeminjung/Mac_Portable_Pack_2026-04-21/data-session/src/main/kotlin/com/ora/data/session/runtime/InMemorySessionStore.kt:143).
- Raw landmark export is now part of the stable export surface in [data-session/src/main/kotlin/com/ora/data/session/export/SessionExportRecord.kt](/Users/haeminjung/Mac_Portable_Pack_2026-04-21/data-session/src/main/kotlin/com/ora/data/session/export/SessionExportRecord.kt:6), [data-session/src/main/kotlin/com/ora/data/session/export/SessionExportBundle.kt](/Users/haeminjung/Mac_Portable_Pack_2026-04-21/data-session/src/main/kotlin/com/ora/data/session/export/SessionExportBundle.kt:3), [data-session/src/main/kotlin/com/ora/data/session/export/SessionJsonExporter.kt](/Users/haeminjung/Mac_Portable_Pack_2026-04-21/data-session/src/main/kotlin/com/ora/data/session/export/SessionJsonExporter.kt:19), and [data-session/src/main/kotlin/com/ora/data/session/export/SessionCsvExporter.kt](/Users/haeminjung/Mac_Portable_Pack_2026-04-21/data-session/src/main/kotlin/com/ora/data/session/export/SessionCsvExporter.kt:4).
- Verification is now test-backed for the repaired contract surfaces via [data-session/src/test/kotlin/com/ora/data/session/runtime/InMemorySessionStoreTest.kt](/Users/haeminjung/Mac_Portable_Pack_2026-04-21/data-session/src/test/kotlin/com/ora/data/session/runtime/InMemorySessionStoreTest.kt:1) and [data-session/src/test/kotlin/com/ora/data/session/export/SessionExportersTest.kt](/Users/haeminjung/Mac_Portable_Pack_2026-04-21/data-session/src/test/kotlin/com/ora/data/session/export/SessionExportersTest.kt:1).
- App-level UI still does not actively consume `:data-session`, so this verdict is contract-backed and build/test-backed rather than full end-to-end UI integration coverage.
