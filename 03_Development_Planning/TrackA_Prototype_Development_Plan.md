# Track A Prototype Development Plan

Date: 2026-04-21
Project: Oral-D Track A
Target: Native Android-first prototype on Mac

## 1. Locked Decisions

- Track A prototype scope is local video playback plus facial feature tracking.
- The app sends derived features into an in-app WebView for visualization.
- `Technical Specification V1.0` is the latest product reference, but prototype implementation should stay conservative on claims.
- Platform is Android first.
- Implementation is native Android, not cross-platform.
- Video input for v1 is a single local video file.
- Raw landmark storage is allowed for research mode.
- Environment assumptions are fixed for v1: one setup, controlled lighting, fixed position, fixed device orientation.
- Validation work is in scope from the start.
- Backend is not required for v1.
- Avoid biomarker claims in UI or outputs. Use tracking and activity language only.

## 2. Prototype Goal

Build a native Android prototype that:

- plays a local therapy video full-screen,
- runs front-camera face tracking during the session,
- computes session features from facial landmarks,
- stores session data locally,
- pushes session summary and debug features into a WebView,
- supports a fixed-environment setup and calibration flow,
- supports internal validation and annotation workflows.

## 3. Recommended Machine and Dev Environment

### Primary Machine

- Use a Mac as the primary development machine.

Reason:

- Android tooling works well on macOS.
- It keeps the path open for iOS later.
- You avoid switching machines if Track A expands beyond Android.

### Required Installs on Mac

- Xcode Command Line Tools
- Homebrew
- Git
- Android Studio latest stable
- JDK 17
- Android SDK Platform for target API
- Android SDK Build-Tools
- Android Emulator
- Android Platform-Tools (`adb`)
- Python 3.11+
- Node.js LTS
- Google Chrome

### Required Android Tooling

- Kotlin
- Jetpack Compose
- CameraX
- Media3 / ExoPlayer
- Android WebView
- Room
- MediaPipe Face Landmarker or equivalent Android MediaPipe task package

### Hardware Needed

- One physical Android phone for camera validation
- One tripod or fixed phone stand
- One controlled light source
- One measured seating distance marker for repeatable testing

## 4. Prototype Architecture

### Core Runtime Flow

1. Caregiver completes environment setup check.
2. User loads or selects the local video asset.
3. Session begins.
4. Video plays full-screen.
5. Front camera captures frames during playback.
6. MediaPipe extracts landmarks.
7. Feature layer converts landmarks into derived signals.
8. Session aggregates are computed.
9. Session summary and debug payload are sent into a WebView.
10. Session record is stored locally.

### Proposed Android Modules

- `app`
  App shell, navigation, DI, app configuration.
- `feature:player`
  Video session playback and session lifecycle.
- `feature:camera`
  CameraX capture and frame pipeline.
- `feature:tracking`
  MediaPipe integration and landmark extraction.
- `feature:features`
  Landmark-to-feature computation and session aggregation.
- `feature:webview`
  WebView bridge and local HTML/JS visualization.
- `feature:setup`
  Fixed-environment readiness and calibration flow.
- `data:session`
  Storage models, repositories, Room, JSON export.
- `tools:validation`
  Internal replay, annotation, comparison, and reporting tools.

## 5. Shared Contracts to Freeze Early

These contracts must be defined before multiple coding instances begin.

### `LandmarkFrame`

- session id
- timestamp ms
- frame index
- face present boolean
- landmark list
- tracking confidence
- optional head pose estimate

### `FeatureFrame`

- timestamp ms
- mouth openness signal
- mouth open event boolean
- head motion delta
- face tracking quality flag

### `SessionSummary`

- session id
- video id
- total duration
- playback completion percent
- face detected percent
- mouth movement event count
- head motion magnitude
- setup readiness result
- local file references for debug artifacts

### `WebViewPayload`

- session metadata
- summary metrics
- feature time series
- validation flags

### `SetupReadinessResult`

- lighting pass/fail
- face framing pass/fail
- device orientation pass/fail
- distance pass/fail
- final ready boolean

## 6. System Breakdown for Parallel Instances

## Instance 1: Core Android Shell

Owner:
- app structure and integration

Responsibilities:
- create Android project
- configure Gradle
- set package structure
- add navigation
- configure permissions
- set DI pattern
- establish debug build configuration

Deliverables:
- compilable app shell
- module registration
- navigation skeleton
- dependency versions locked

Sub-goals:
- create base app module
- define navigation routes
- add shared theme and debug menu
- wire common logging and crash-safe wrappers

Depends on:
- none

Blocks:
- all other workstreams need module integration here

## Instance 2: Video Session Player

Owner:
- local video playback session

Responsibilities:
- integrate Media3/ExoPlayer
- play one local video file
- implement start, pause, resume, end
- expose playback timing callbacks
- support full-screen session view

Deliverables:
- working local video session screen
- playback state events consumable by tracking layer

Sub-goals:
- load bundled MP4 asset
- implement playback lifecycle
- expose current playback position
- emit session completion percent

Depends on:
- Instance 1

Interface outputs:
- playback state
- playback position
- session start and stop events

## Instance 3: Camera Capture Pipeline

Owner:
- CameraX and frame ingestion

Responsibilities:
- select front camera
- configure capture resolution
- provide frame stream for inference
- handle permission and lifecycle safely
- support fixed-orientation runtime

Deliverables:
- reusable camera frame provider
- synchronized frame timestamps

Sub-goals:
- build CameraX preview-free pipeline
- lock portrait orientation for v1
- benchmark frame throughput
- expose frame objects to tracking module

Depends on:
- Instance 1

Interface outputs:
- frame stream
- timestamps
- camera status

## Instance 4: Face Tracking Pipeline

Owner:
- MediaPipe landmark extraction

Responsibilities:
- integrate MediaPipe Face Landmarker
- process CameraX frames
- output normalized landmark frames
- report face-present and confidence
- support debug sampling

Deliverables:
- landmark stream generator
- face tracking status

Sub-goals:
- add MediaPipe dependency
- map raw inference output into `LandmarkFrame`
- handle no-face frames cleanly
- benchmark CPU and latency on physical device

Depends on:
- Instance 3

Interface outputs:
- `LandmarkFrame`

## Instance 5: Feature Engineering Layer

Owner:
- derived signal computation

Responsibilities:
- compute face detected percent
- compute mouth movement event count
- compute head motion magnitude
- support per-frame and session-level calculations
- optionally persist raw landmarks in research mode

Deliverables:
- feature calculators
- session aggregator
- thresholds config

Sub-goals:
- define mouth openness signal
- define mouth open/close event logic
- define head motion formula
- aggregate session summary
- implement debug traces for WebView

Depends on:
- Instance 4
- Instance 6 for persistence schema alignment

Interface outputs:
- `FeatureFrame`
- `SessionSummary`

## Instance 6: Session Data and Local Storage

Owner:
- local data layer

Responsibilities:
- define entities and repositories
- persist session summaries
- persist feature traces
- optionally persist landmark frames in research mode
- support export to JSON/CSV

Deliverables:
- storage schema
- repository interfaces
- export utilities

Sub-goals:
- choose Room or file-plus-index hybrid
- define retention strategy
- define debug export format
- add storage-backed session history list

Depends on:
- Instance 1

Interface outputs:
- storage APIs
- session history APIs
- export artifacts

## Instance 7: Setup and Calibration Flow

Owner:
- fixed-environment readiness

Responsibilities:
- build caregiver-only setup flow
- verify framing
- verify lighting floor
- verify device orientation
- gate session start on readiness

Deliverables:
- setup wizard
- `SetupReadinessResult`

Sub-goals:
- create setup screen
- define readiness thresholds
- show pass/fail feedback before session
- persist environment profile for reuse

Depends on:
- Instance 3
- Instance 4

Interface outputs:
- setup readiness object

## Instance 8: WebView Visualization

Owner:
- in-app HTML/JS visualizer

Responsibilities:
- build local WebView page
- receive Android payloads
- render summary metrics
- render time-series plots
- support debug mode and summary mode

Deliverables:
- WebView bridge
- local dashboard

Sub-goals:
- define `WebViewPayload`
- create JS bridge API
- render session metrics
- render feature charts

Depends on:
- Instance 5
- Instance 6

Interface outputs:
- visualized session summary
- visualized traces

## Instance 9: Validation and Annotation Toolkit

Owner:
- internal evaluation tools

Responsibilities:
- define annotation schema
- support manual labeling of face present and mouth events
- compare annotations to feature output
- generate accuracy reports

Deliverables:
- annotation format
- comparison utility
- prototype validation report template

Sub-goals:
- define CSV or JSON annotation schema
- create replay utility for saved sessions
- calculate precision and recall on mouth events
- calculate frame-level face present agreement

Depends on:
- Instance 5
- Instance 6

## 7. Parallel Start Strategy

The work should not be run as strict sequential phases.

Instead use:

- one short foundation freeze,
- then parallel workstream starts,
- then staged integration merges,
- then validation and hardening.

### 7.1 Foundation Freeze

This is the only step that should happen before all other workstreams branch off.

Scope:

- create Android project
- lock SDK, JDK, Gradle, Kotlin versions
- define Android module layout
- freeze shared contracts
- choose storage strategy
- decide branch naming and integration cadence

Target duration:

- 0.5 to 1 day

Exit criteria:

- app builds
- empty modules compile
- contract models committed
- branch and merge rules documented

### 7.2 Workstreams That Can Start Immediately After Foundation Freeze

These can all start in parallel on separate instances:

- Instance 2: Video Session Player
- Instance 3: Camera Capture Pipeline
- Instance 4: Face Tracking Pipeline
- Instance 5: Feature Engineering Layer
- Instance 6: Session Data and Local Storage
- Instance 7: Setup and Calibration Flow
- Instance 8: WebView Visualization
- Instance 9: Validation and Annotation Toolkit

### 7.3 Parallel Workstream Rules

- Each instance branches from the same frozen foundation commit.
- Each instance writes only in its assigned module or contract surface.
- Shared contracts may only be changed through one designated contract owner branch.
- If a contract must change, update the contract first, then rebase downstream instances.
- No instance should wait for full implementation from another instance if a mock or stub contract can unblock it.

## 8. Integration Branch Plan

Use staged integration branches instead of merging everything directly into main.

### Branches

- `main`
  Stable, demoable branch only.
- `foundation/frozen-baseline`
  Initial branch containing the module layout and shared contracts.
- `integration/runtime`
  Video, camera, and tracking integration.
- `integration/data-ui`
  Features, storage, setup, and WebView integration.
- `integration/validation`
  Validation tools and replay utilities.

### Recommended Instance Branch Names

- `instance/core-shell`
- `instance/video-player`
- `instance/camera-pipeline`
- `instance/face-tracking`
- `instance/feature-engineering`
- `instance/data-storage`
- `instance/setup-calibration`
- `instance/webview-visualization`
- `instance/validation-toolkit`

## 9. Merge Waves

Do not merge by completion order alone. Merge by compatibility wave.

### Merge Wave 1: Runtime Spine

Target branches:

- `instance/video-player`
- `instance/camera-pipeline`
- `instance/face-tracking`

Merge destination:

- `integration/runtime`

Goal:

- prove video playback and face tracking can coexist

Exit criteria:

- local MP4 playback works
- front camera works during session
- MediaPipe returns landmarks
- no major playback lag on physical device

### Merge Wave 2: Data and Feature Spine

Target branches:

- `instance/data-storage`
- `instance/feature-engineering`

Merge destination:

- `integration/data-ui`

Goal:

- compute and persist session features from tracked frames

Exit criteria:

- `SessionSummary` generated after session
- raw and derived outputs exportable
- storage schema stable enough for UI consumption

### Merge Wave 3: Operator and Inspection Layer

Target branches:

- `instance/setup-calibration`
- `instance/webview-visualization`

Merge destination:

- `integration/data-ui`

Goal:

- make the prototype usable and inspectable without developer intervention

Exit criteria:

- setup readiness flow works
- WebView displays metrics and traces
- operator can run repeatable sessions and inspect outputs

### Merge Wave 4: Validation Layer

Target branches:

- `instance/validation-toolkit`

Merge destination:

- `integration/validation`

Goal:

- create internal evidence that the outputs are stable enough to interpret

Exit criteria:

- annotation schema exists
- replay utility works
- comparison reports run on saved sessions

### Merge Wave 5: Final Prototype Assembly

Merge sources:

- `integration/runtime`
- `integration/data-ui`
- `integration/validation`

Merge destination:

- `main`

Goal:

- produce one end-to-end internal prototype branch

Exit criteria:

- all core flows run on a physical Android device
- known limitations documented

## 10. Parallel Milestones

These milestones are designed so multiple workstreams can begin at the same time and converge later.

### Milestone A: Frozen Baseline

- native Android app created
- modules compile
- shared contracts committed
- storage strategy committed

### Milestone B: Runtime Feasibility

- front camera and MediaPipe run during playback
- landmark output available for a test session

### Milestone C: Feature and Storage Integration

- session features computed from tracked frames
- `SessionSummary` persisted locally

### Milestone D: Usable Prototype Surface

- setup flow prevents bad sessions
- WebView renders summary and traces
- operator can run repeatable sessions

### Milestone E: Internal Validation Readiness

- annotated sample set exists
- replay and comparison utilities exist
- feature accuracy and limitations documented

## 11. Phase Mapping for Parallel Execution

If you still want to think in phases, use phases as labels, not gates.

### Phase A: Foundation Freeze

Runs once before branching.

### Phase B: Parallel Build

Runs across Instances 2 through 9 at the same time.

### Phase C: Merge Waves

Runtime, then data/UI, then validation.

### Phase D: Hardening

Tune thresholds, fix regressions, document constraints.

## 12. Suggested Milestones

### Milestone A: Build Skeleton

- native Android app created
- modules compile
- one local video plays

### Milestone B: CV Feasibility

- front camera and MediaPipe run during playback
- landmark output stored for a test session

### Milestone C: Feature Summary

- `SessionSummary` generated locally
- WebView can render results

### Milestone D: Fixed-Environment Readiness

- setup flow prevents bad sessions
- repeatability improves across test runs

### Milestone E: Internal Validation

- annotated sample set exists
- feature accuracy and limitations documented

## 13. Rules for Multi-Agent / Multi-Instance Work

- Each instance owns a narrow write scope.
- Shared contracts are edited only by one designated owner.
- No instance should redefine feature formulas locally.
- No instance should change module names after the foundation freeze.
- WebView payload changes must be versioned.
- Research-mode storage of landmarks must be clearly separated from normal session summaries.
- All UI language must avoid biomarker or diagnosis claims.
- Every instance should ship against mocks first if its upstream dependency is not merged yet.
- Integration branches should absorb rebases so feature branches stay narrow.

## 14. Suggested Ownership Map

- Contract owner: Instance 1 plus Instance 6 lead
- Runtime pipeline owner: Instance 3 plus Instance 4 lead
- Feature definition owner: Instance 5 lead
- Visualization owner: Instance 8 lead
- Validation owner: Instance 9 lead

## 15. Prototype Acceptance Criteria

The Track A prototype is considered successful when:

- the app runs on a physical Android device,
- a local video session can be started and completed reliably,
- the front camera tracks face landmarks during playback,
- derived features are computed and stored locally,
- the WebView displays the resulting feature output,
- a fixed-environment setup flow reduces low-quality runs,
- internal validation data exists for at least a small annotated sample.

## 16. Recommended Next Step

Start with the foundation freeze only.

Do not begin all coding instances before these three items are frozen:

- Android module layout
- shared contracts
- storage strategy

Without those, parallel work will create merge conflicts and contract churn.
