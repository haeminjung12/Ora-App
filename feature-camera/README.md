# `feature-camera`

Standalone draft of the Track A `worker/camera-pipeline` module.

## Scope

This scaffold covers the responsibilities assigned in the planning docs:

- CameraX-backed front-camera ingestion
- explicit camera permission state
- lifecycle-safe camera startup and shutdown
- monotonic frame timestamps for downstream inference
- preview-free frame delivery with externally observable camera status

## Current contents

- `CameraFrameProviderContract`
  Narrow module contract for camera lifecycle, permission state, frame delivery, and status updates.
- `CameraXFrameProvider`
  CameraX-backed analysis runtime that binds the front camera, emits `ImageProxy` frames, and surfaces session events.
- `CameraSessionConfig`
  Small session config model for resolution, backpressure, and optional auto-start behavior.

## Integration notes

- This module is intentionally standalone because the repo does not yet contain the frozen foundation Android project.
- Expected eventual write scope is `feature:camera`.
- The foundation branch should own app-level permission prompting, DI wiring, and session orchestration with `feature:player`.
- Consumers that receive a `CameraFrame` must call `close()` when processing is complete to release the underlying `ImageProxy`.
