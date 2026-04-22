# Oral-D Mac Portable Pack

Date: 2026-04-21
Purpose: Self-contained project handoff folder for moving the current Track A planning materials to a Mac.

## What This Pack Contains

### `00_Project_Overview`

- `Ora_System_Integration_Map.md`
  Best short project-level framing doc.
  Read this first for scope boundaries, what Track A and Track B actually do, and what claims are out of bounds.

### `01_Technical_Specifications`

- `Technical Specification V1.0.md`
  Latest core project technical specification.
  Use this as the primary product reference.

### `02_Clinical_Resources`

- `Example HEP Prescriptions.md`
  Practical prescription examples and what the app should log.
- `SLP Interview Script.md`
  Clinical decision and guardrail questions.
- `Take-home exercise library.md`
  Exercise context and mapping reference.

### `03_Development_Planning`

- `TrackA_Prototype_Development_Plan.md`
  Current Android-first Track A development plan.
  Includes module split, parallel workstream strategy, merge waves, and acceptance criteria.

### `04_Supporting_Context`

- `Abstract.md`
  Short external-facing summary of the project.
- `Venture Well-Proposal-V3.md`
  Longer narrative context, market framing, and historical Track A language.
  Useful for context, but not the implementation source of truth.

## Read Order

1. `00_Project_Overview/Ora_System_Integration_Map.md`
2. `01_Technical_Specifications/Technical Specification V1.0.md`
3. `03_Development_Planning/TrackA_Prototype_Development_Plan.md`
4. `02_Clinical_Resources/Example HEP Prescriptions.md`
5. Remaining clinical and supporting docs as needed

## Current Track A Working Position

- Android first
- native Android
- local video file for v1
- front-camera facial tracking during playback
- derived features only in normal product flow
- raw landmarks allowed in research mode
- fixed setup environment for prototype testing
- no backend required for v1
- no biomarker, diagnosis, or therapy recommendation claims in UI

## Transfer Notes

This folder is intended to be copied directly to the Mac.

Suggested next step on the Mac:

1. Place this folder in the project workspace.
2. Read the documents in the order above.
3. Use `TrackA_Prototype_Development_Plan.md` as the execution doc for branching and parallel instance setup.

## Source of Truth Hierarchy

When documents disagree, use this order:

1. `Technical Specification V1.0.md`
2. `Ora_System_Integration_Map.md`
3. `TrackA_Prototype_Development_Plan.md`
4. Clinical resource docs
5. Grant and proposal narrative docs

The supporting grant documents are included for context, not for final implementation authority.
