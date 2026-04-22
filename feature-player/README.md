# `feature-player`

Standalone draft of the Track A `worker/video-player` module.

## Scope

This scaffold covers the responsibilities assigned in the planning docs:

- local video playback with Media3 / ExoPlayer
- full-screen session UI
- playback lifecycle
- playback timing callbacks
- output contracts for playback state, playback position, and session start/stop events

## Intended integration

This module is designed to be embedded by a host Android app, not to act as its own shell.

- The host app is expected to construct a `PlayerSessionContract` implementation and pass it into `VideoSessionScreen`.
- The host app owns controller lifecycle and should call `release()` when the surrounding screen or activity is disposed.
- The host app owns navigation, dependency injection, analytics forwarding, permissions, and any surrounding coaching or camera overlays.
- `VideoSessionScreen` is intentionally limited to the player surface plus a lightweight session HUD based only on the controller contract.
- The screen can be placed inside a full-screen destination, modal flow, or split runtime surface as long as the host manages lifecycle correctly.

## Current contents

- `PlayerSessionContract`
  Narrow module contract for the rest of the runtime pipeline.
- `Media3PlayerSessionController`
  Media3-backed implementation that emits playback state, position, completion, and session events.
- `VideoSessionScreen`
  Full-screen Compose player surface with session metadata, progress, timing stats, and basic transport controls.

## Session UI behavior

The current player screen uses only the existing controller contract and does not depend on any app-shell types.

- Renders a full-screen `PlayerView` with a Compose overlay.
- Shows session id, video id, autoplay mode, playback state, completion percent, elapsed time, remaining time, and total duration.
- Collects `sessionEvents` to display the latest start or stop event inside the session HUD.
- Exposes `Play`, `Pause`, and `Stop` controls without assuming external navigation callbacks.

## Integration notes

- This module is intentionally standalone because the repo does not yet contain the frozen foundation Android project.
- Expected eventual write scope is `feature:player`.
- The foundation branch should own plugin management, version catalogs, app navigation, and DI wiring.

## Current limits

- No app-shell navigation or dismissal hooks are included here.
- No error recovery UI exists beyond showing the `ERROR` playback state.
- No camera/tracking composition is implemented in this standalone scaffold.
- Styling is module-local and may need to be aligned with the eventual product design system.
- Runtime validation on device has not been completed from this scaffold alone.
