# Camera Pipeline Worker

Date: 2026-04-22
Worker: `worker/camera-pipeline`
Write scope: `feature:camera`
Target branch: `instance/camera-pipeline`

## Assigned Job

Own the camera ingestion layer for Track A runtime.

This worker is responsible for:

- CameraX front-camera ingestion
- permission handling
- lifecycle-safe startup and shutdown
- synchronized timestamps
- fixed-orientation behavior
- preview-free frame delivery for downstream tracking

Required output contract:

- frame stream
- timestamps
- camera status

## What I Found

- The repo now contains a real Android foundation project with:
  - root Gradle setup
  - Gradle wrapper
  - registered `:app`, `:core-contracts`, `:feature-player`, `:feature-camera`, and `:data-session` modules
- `feature-camera` is integrated into the shared version catalog and foundation build graph.
- The immediate job shifted from scaffold creation to real module verification and lifecycle correctness inside the foundation baseline.

## What I Started

I created and verified the `feature-camera/` module inside the foundation project.

Current module contents:

- `CameraFrameProviderContract`
  Narrow camera contract with:
  - `cameraStatus`
  - `activeSession`
  - `frameStream`
  - `sessionEvents`
  - lifecycle methods for session prep, binding, start, stop, and release
- `CameraXFrameProvider`
  CameraX-backed runtime for:
  - front-camera selection
  - lifecycle binding
  - explicit permission state handling
  - preview-free `ImageAnalysis`
  - monotonic frame timestamps
  - camera session start and stop events
- model types
  - `CameraFrame`
  - `CameraSessionConfig`
  - `CameraSessionEvent`
  - `CameraStatus`
- `FrameTimestampSource`
  Timestamp abstraction with an `elapsedRealtimeNanos` implementation

First integrated fix completed:

- fixed `autoStart` coordination so camera startup now works regardless of whether `prepareSession`, `bind`, or permission grant happens first
- verified this against the real foundation build instead of the earlier standalone assumption

## Files Created

- `feature-camera/build.gradle.kts`
- `feature-camera/consumer-rules.pro`
- `feature-camera/src/main/AndroidManifest.xml`
- `feature-camera/src/main/kotlin/com/ora/feature/camera/CameraFrameProviderContract.kt`
- `feature-camera/src/main/kotlin/com/ora/feature/camera/model/CameraFrame.kt`
- `feature-camera/src/main/kotlin/com/ora/feature/camera/model/CameraSessionConfig.kt`
- `feature-camera/src/main/kotlin/com/ora/feature/camera/model/CameraSessionEvent.kt`
- `feature-camera/src/main/kotlin/com/ora/feature/camera/model/CameraStatus.kt`
- `feature-camera/src/main/kotlin/com/ora/feature/camera/runtime/FrameTimestampSource.kt`
- `feature-camera/src/main/kotlin/com/ora/feature/camera/runtime/CameraXFrameProvider.kt`
- `feature-camera/README.md`
- `03_Development_Planning/Camera_Pipeline_Validation_Plan.md`

## Current Contract

Inputs:

- `prepareSession(config)`
- `bind(lifecycleOwner)`
- `updatePermission(granted)`
- `start()`
- `stop()`
- `release()`

Outputs:

- `cameraStatus`
- `activeSession`
- `frameStream`
- `sessionEvents`

Frame model:

- `sessionId`
- `frameIndex`
- `timestampMs`
- `rotationDegrees`
- live `ImageProxy`

Status model:

- `Idle`
- `RequestingPermission`
- `Ready`
- `Running`
- `Denied`
- `Error`

## Validation Work Started

A dedicated validation plan now exists at:

- `03_Development_Planning/Camera_Pipeline_Validation_Plan.md`

It covers:

- contract validation
- local functional validation
- timing and orientation checks
- playback-plus-camera integration checks
- physical-device validation expectations

Build verification completed:

- `./gradlew :feature-camera:assembleDebug :app:assembleDebug`
- result: build successful on the integrated foundation project

## Constraints And Risks

- The final integration contract still depends on upstream decisions:
  - DI pattern
  - frame delivery surface
  - target resolution
  - FPS and backpressure policy
  - app-level permission prompting
- The app shell still does not exercise the camera runtime yet; it only registers the module in the build graph.
- CameraX currently emits a deprecation warning for `setTargetResolution(Size)`, so that API choice should be revisited during runtime hardening.

## Next Steps

1. Freeze the camera contract against the tracking worker.
2. Wire `feature-player` session lifecycle to camera start and stop in the app shell.
3. Add app-level permission prompting and observable camera status UI for runtime verification.
4. Validate the camera pipeline on a physical Android device.
5. Revisit the deprecated CameraX resolution API before runtime merge hardening.

## Integration Target

Merge wave:

- Wave 1: Runtime Spine

Integration branch:

- `integration/runtime`

This worker should be merged alongside:

- `instance/video-player`
- `instance/face-tracking`
