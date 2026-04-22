# Codex: Video-Player Worker

Date: 2026-04-22
Role: `worker/video-player`

## Assigned Job

Based on the current repo planning docs, the `video-player` worker owns:

- local video playback with Media3 or ExoPlayer
- full-screen session UI
- playback lifecycle
- timing callbacks

Required output contract:

- playback state
- playback position
- session start and stop events

Expected write scope:

- `feature:player`

Integration target:

- branch `instance/video-player`
- Merge Wave 1 into `integration/runtime`

## What I Found

- This repository currently contains planning and specification documents only.
- There is no Android foundation project yet.
- There is no frozen baseline app, Gradle setup, shared contract module, or `feature:player` module to extend.

This means the player worker cannot complete normal in-module integration yet without stepping outside the assigned write scope or inventing the missing foundation layer.

## What I Started

To avoid blocking the runtime wave completely, I created a narrow standalone scaffold at `feature-player/` that can be moved into the eventual `feature:player` module once the foundation branch exists.

Scaffold contents:

- `PlayerSessionContract`
  Defines the worker-owned outputs:
  - playback state
  - playback position
  - completion percent
  - session start and stop events
- `Media3PlayerSessionController`
  Media3-backed controller for:
  - local media item attachment
  - start, pause, stop
  - position tracking
  - completion tracking
  - start and stop event emission
- `VideoSessionScreen`
  Full-screen Compose player surface with:
  - Media3 `PlayerView`
  - play/pause/stop controls
  - playback status display
  - completion progress bar
- `build.gradle.kts`
  Standalone draft library module definition

## Files Created

- `feature-player/build.gradle.kts`
- `feature-player/consumer-rules.pro`
- `feature-player/src/main/AndroidManifest.xml`
- `feature-player/src/main/kotlin/com/ora/feature/player/PlayerSessionContract.kt`
- `feature-player/src/main/kotlin/com/ora/feature/player/model/PlaybackState.kt`
- `feature-player/src/main/kotlin/com/ora/feature/player/model/SessionEvent.kt`
- `feature-player/src/main/kotlin/com/ora/feature/player/model/VideoSessionConfig.kt`
- `feature-player/src/main/kotlin/com/ora/feature/player/runtime/Media3PlayerSessionController.kt`
- `feature-player/src/main/kotlin/com/ora/feature/player/ui/VideoSessionScreen.kt`
- `feature-player/README.md`

## Constraints And Risks

- This scaffold is not wired into an app shell because no foundation app exists in this repo yet.
- Dependency versions are local placeholders because no shared version catalog or root Gradle config exists yet.
- The contract owner may still change shared runtime expectations before foundation freeze.
- No compile verification was possible in this repo because there is no Gradle wrapper or Android project root.

## Recommended Next Step

The foundation owner should create the frozen Android baseline first:

1. root Gradle setup
2. Android `app` module
3. real `feature:player` module registration
4. shared contract freeze

After that, the scaffold in `feature-player/` should be moved into the official `feature:player` module and wired to app navigation and DI.
