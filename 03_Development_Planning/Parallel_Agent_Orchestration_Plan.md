# Parallel Agent Orchestration Plan

Date: 2026-04-22
Project: Oral-D Track A
Role: Main orchestrator delegates; workers implement; verifiers check results

## Core Principle

Use one short foundation phase, then parallel workers with narrow write scopes, plus one verification agent per merge wave.

The main failure mode is letting workers invent contracts independently. Freeze contracts first, then let them build against those contracts.

## Agent Set

### 1. `foundation/contract-owner`

Owns:
- initial Android project
- Gradle setup
- module layout
- SDK and JDK versions
- package namespace
- shared data contracts

Write scope:
- root Gradle files
- `app`
- shared contract modules only

Deliverable:
- compilable baseline branch that all other agents branch from

### 2. `worker/video-player`

Builds:
- local video playback with Media3 or ExoPlayer
- full-screen session UI
- playback lifecycle
- timing callbacks

Write scope:
- `feature:player`

Output contract:
- playback state
- playback position
- session start and stop events

### 3. `worker/camera-pipeline`

Builds:
- CameraX front-camera ingestion
- permissions handling
- timestamps
- fixed-orientation behavior

Write scope:
- `feature:camera`

Output contract:
- frame stream
- timestamps
- camera status

### 4. `worker/face-tracking`

Builds:
- MediaPipe Face Landmarker integration
- mapping inference output into `LandmarkFrame`

Write scope:
- `feature:tracking`

Depends on:
- camera contract only

### 5. `worker/runtime-integration`

Builds:
- app-level runtime orchestration glue
- cross-module lifecycle coordination
- runtime spine integration in `:app`

Write scope:
- `app`
- app-level runtime wiring only

Does not own:
- `feature:player` internals
- `feature:camera` internals
- shared contracts, unless explicitly approved

### 6. `worker/feature-engineering`

Builds:
- mouth openness calculation
- mouth event detection
- head motion calculation
- face-detected percent
- `SessionSummary`

Write scope:
- `feature:features`

Depends on:
- frozen `LandmarkFrame`
- frozen `FeatureFrame`
- frozen `SessionSummary`

### 7. `worker/data-storage`

Builds:
- Room entities
- repositories
- local persistence
- JSON and CSV export
- research-mode raw landmark storage boundaries

Write scope:
- `data:session`

Depends on:
- frozen summary and feature contracts

### 8. `worker/setup-calibration`

Builds:
- setup wizard
- lighting, framing, orientation, and distance checks
- `SetupReadinessResult`

Write scope:
- `feature:setup`

### 9. `worker/webview-visualization`

Builds:
- in-app WebView page
- JS bridge
- summary rendering
- debug trace charts

Write scope:
- `feature:webview`

Depends on:
- frozen `WebViewPayload`

### 10. `worker/validation-toolkit`

Builds:
- replay tooling
- annotation schema
- comparison utilities
- accuracy reports

Write scope:
- `tools:validation`

Depends on:
- stored session outputs
- feature contracts

## Verification Agents

Use separate verification agents that do not own feature work and do not rewrite worker code unless explicitly asked.

### 1. `verifier/runtime`

Checks:
- `worker/video-player`
- `worker/camera-pipeline`
- `worker/face-tracking`

Verifies:
- app compiles
- playback and camera coexist
- landmarks flow correctly
- no obvious lifecycle regressions

### 2. `verifier/data-ui`

Checks:
- `worker/feature-engineering`
- `worker/data-storage`
- `worker/setup-calibration`
- `worker/webview-visualization`

Verifies:
- summaries are computed correctly
- persistence schema matches usage
- setup gating works
- payload renders in WebView

### 3. `verifier/validation`

Checks:
- `worker/validation-toolkit`

Verifies:
- annotation schema is coherent
- replay works
- metrics are reproducible

### 4. `verifier/final-integration`

Runs after merge waves.

Verifies:
- clean build
- no contract drift
- no claim-language violations
- end-to-end session path works

## How To Assign Them

Every worker gets:
- one owned module or file set
- explicit input and output contracts
- explicit instruction not to change shared contracts
- explicit instruction to use mocks or stubs if upstream is not merged

Every verifier gets:
- exact branch or diff to inspect
- acceptance checklist
- authority to report defects, regressions, missing tests, and contract mismatches
- no authority to silently redefine product behavior

## Recommended Rollout

1. Run `foundation/contract-owner` alone.
2. After baseline freezes, run in parallel:
   - `worker/video-player`
   - `worker/camera-pipeline`
   - `worker/data-storage`
3. Then run:
   - `worker/runtime-integration`
   - `worker/face-tracking`
   - `worker/setup-calibration`
4. Then run:
   - `worker/feature-engineering`
   - `worker/webview-visualization`
   - `worker/validation-toolkit`
5. Pair each merge wave with its verifier before merging upward.

## Critical Rule

Have exactly one contract owner.

If multiple workers can edit `LandmarkFrame`, `SessionSummary`, or `WebViewPayload`, the parallelization will collapse into merge churn.
