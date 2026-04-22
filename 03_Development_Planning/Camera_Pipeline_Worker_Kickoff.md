# Camera Pipeline Worker Kickoff

Date: 2026-04-22
Worker: `worker/camera-pipeline`
Write scope: `feature:camera`
Target branch: `instance/camera-pipeline`

## Mission

Own the camera ingestion layer for Track A runtime.

This worker is responsible for building the front-camera capture path that runs during video playback and produces a stable frame stream for downstream face tracking.

## Source Alignment

Primary references:

- `03_Development_Planning/Parallel_Agent_Orchestration_Plan.md`
- `03_Development_Planning/TrackA_Prototype_Development_Plan.md`

Relevant product constraints:

- Android first
- native Android
- front camera runs during playback
- fixed setup environment for v1
- fixed device orientation for v1
- no backend required for v1

## Ownership

Build:

- CameraX front-camera ingestion
- permission handling
- lifecycle-safe camera startup and shutdown
- synchronized timestamps
- fixed-orientation behavior
- preview-free frame pipeline for inference

Deliver:

- reusable camera frame provider
- frame stream
- timestamps
- camera status

## Non-Goals

Do not own:

- video playback UI or playback state contracts
- MediaPipe inference
- `LandmarkFrame` generation
- feature engineering
- storage or export
- setup/calibration logic outside camera-facing hooks

## Dependencies

This worker depends on the baseline project and shared contracts being frozen first.

Required upstream foundation:

- Android project exists
- `feature:camera` module exists
- package namespace is fixed
- dependency injection approach is fixed
- session/runtime contract from `feature:player` is available

Consumes from upstream:

- session lifecycle or equivalent runtime start/stop signal

Provides to downstream:

- frame stream
- timestamps
- camera status

## Interface Boundary

The camera module should expose a narrow contract to tracking.

Minimum output shape:

- frame object/image handle suitable for inference
- timestamp ms
- frame index if needed by tracking/runtime
- camera status:
  - idle
  - requesting permission
  - ready
  - running
  - denied
  - error

The camera module should not embed MediaPipe-specific mapping logic unless required as an adapter boundary.

## Implementation Checklist

### Phase 1: Module Skeleton

- create `feature:camera`
- add CameraX dependencies
- define camera-facing interfaces
- define camera status model
- define timestamp strategy

### Phase 2: Permission and Lifecycle

- request camera permission
- expose denied and granted states
- bind and unbind safely to lifecycle owner
- handle app background and foreground transitions

### Phase 3: Front-Camera Ingestion

- select front camera explicitly
- configure target resolution
- use an analysis pipeline suitable for inference
- avoid preview as a hard dependency
- emit frames on a controlled executor

### Phase 4: Orientation and Timing

- lock portrait orientation for v1
- normalize frame rotation metadata
- attach synchronized timestamps in milliseconds
- confirm timestamp monotonicity across the session

### Phase 5: Runtime Hardening

- benchmark frame throughput on a physical Android phone
- verify camera runs concurrently with video playback
- surface recoverable camera errors
- expose camera start and stop status for integration logging

## Acceptance Criteria

The worker is complete when:

- front camera starts during a session
- permission flow is safe and explicit
- frames are available to tracking in real time
- timestamps are stable and synchronized
- portrait-only runtime behavior is enforced for v1
- camera status is externally observable
- no major playback lag is introduced on a physical device

## Open Decisions To Freeze Early

These should be decided by the foundation or integration owner before implementation starts:

- package name and module namespace
- DI pattern
- coroutine vs callback surface for frame delivery
- frame container type passed to tracking
- target capture resolution
- target FPS and backpressure policy
- exact camera status enum/sealed model

## Suggested First Commit After Baseline Exists

1. Add `feature:camera` module.
2. Add camera contract interfaces and status model.
3. Add CameraX dependency set.
4. Implement front-camera analyzer pipeline with timestamps.
5. Add permission and lifecycle integration.

## Integration Target

Merge wave:

- Wave 1: Runtime Spine

Integration branch:

- `integration/runtime`

This worker should be merged alongside:

- `instance/video-player`
- `instance/face-tracking`
