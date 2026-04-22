# Camera Pipeline Validation Plan

Date: 2026-04-22
Worker: `worker/camera-pipeline`
Write scope: `feature:camera`
Verifier target: `verifier/runtime`

## Purpose

Validate that the camera-pipeline worker delivers a stable front-camera frame stream for Track A runtime without breaking playback, lifecycle safety, or fixed-orientation assumptions.

This plan stays within the camera worker boundary and assumes shared contracts are frozen by the foundation owner.

## Validation Goals

- confirm the front camera starts and stops reliably from session lifecycle signals
- confirm permission handling is explicit and externally observable
- confirm frame delivery is real time enough for downstream tracking
- confirm timestamps are monotonic and aligned to session runtime expectations
- confirm portrait-only behavior for v1
- confirm playback and camera can run together on a physical Android phone
- confirm recoverable failures surface `camera status` instead of silent stalls

## Preconditions

- baseline Android project and `feature:camera` module exist
- package namespace, DI pattern, and frame delivery contract are frozen
- `feature:player` can emit runtime start and stop signals
- one physical Android phone is available for validation
- test environment matches v1 assumptions:
  - front camera
  - controlled lighting
  - fixed phone position
  - fixed portrait orientation

## Test Layers

### 1. Contract Validation

Verify the worker-owned output contract is stable and narrow:

- frame stream emits frame/image handle plus `timestamp ms`
- camera status exposes `idle`, `requesting permission`, `ready`, `running`, `denied`, and `error`
- frame stream can be consumed without preview UI as a hard dependency
- no MediaPipe-specific mapping leaks into the camera contract unless explicitly frozen as an adapter boundary

### 2. Local Functional Validation

Run repeatable checks around worker-owned behavior:

- permission denied path reports `denied` and does not bind camera resources
- permission granted path reaches `ready` then `running`
- start and stop actions bind and unbind cleanly
- app background and foreground transitions do not leave the camera stuck or duplicated
- front camera is selected explicitly
- fixed target resolution and backpressure policy match the frozen runtime decision

### 3. Timing And Orientation Validation

Validate runtime assumptions that downstream tracking depends on:

- timestamps increase monotonically through a full session
- timestamp source remains stable after pause/resume or background/foreground transitions
- rotation metadata is normalized for portrait-only v1 behavior
- device rotation outside the supported orientation does not silently change frame interpretation

### 4. Integration Validation

Validate the runtime path expected for Merge Wave 1:

- camera starts when the session starts
- camera stops when the session ends
- camera and local video playback run concurrently without obvious jank
- camera status can be observed by integration logging or debug UI
- downstream tracking can subscribe to the frame stream without additional camera-module changes

### 5. Physical Device Validation

Use at least one real Android phone for performance and coexistence checks:

- run a full session with front camera active during video playback
- confirm no major playback lag is introduced
- confirm frame delivery remains steady for the target session length
- confirm thermal or resource pressure does not trigger frequent frame starvation
- confirm recoverable errors are surfaced and the session fails visibly rather than hanging

## Suggested Test Matrix

| Area | Scenario | Expected Result |
| --- | --- | --- |
| Permission | First launch, deny permission | `camera status = denied`; no active binding |
| Permission | First launch, grant permission | status progresses to `ready` and `running` |
| Lifecycle | Start session, end session | camera binds once and releases once |
| Lifecycle | App background then foreground | camera recovers cleanly or reports `error` explicitly |
| Camera select | Device with front and rear cameras | front camera is used consistently |
| Timing | 5+ minute session | timestamps remain monotonic; no reset |
| Orientation | Portrait-locked runtime | rotation metadata stays normalized for v1 |
| Coexistence | Playback plus camera on device | no major playback lag; frame stream stays active |
| Failure | Camera unavailable/interrupted | status moves to `error`; no silent stall |

## Acceptance Gate

The camera worker is ready for runtime verification when:

- all contract checks pass against frozen interfaces
- permission, lifecycle, timing, and orientation checks pass
- at least one physical-device playback-plus-camera run completes successfully
- no major playback lag or repeated binding failure is observed
- logs or debug traces show camera status transitions and session timing clearly enough for verifier review

## Evidence To Capture

- short test notes per scenario in the matrix
- device model and Android version used for physical validation
- timestamp and status logs from at least one successful session
- any observed frame drops, recovery events, or playback lag notes
- explicit list of unresolved issues blocked on upstream contract or integration work

## Out Of Scope

This plan does not validate:

- MediaPipe landmark accuracy
- `LandmarkFrame` correctness beyond camera input handoff
- feature engineering correctness
- storage, export, or WebView rendering
- setup or calibration UX outside camera-facing hooks
