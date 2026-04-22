# Android Foundation Baseline

This directory now contains a compilable Android foundation project for the Track A prototype.

## Included modules

- `:app`
- `:core-contracts`
- `:feature-player`
- `:feature-camera`
- `:data-session`

## Locked baseline choices

- Android Gradle Plugin `8.5.2`
- Kotlin `1.9.24`
- JDK `17`
- `compileSdk = 34`
- `minSdk = 26`
- `targetSdk = 34`

## Purpose

This baseline exists so worker branches can integrate against:

- one Gradle wrapper
- one plugin and dependency graph
- one registered module set
- one Android app shell

## Next orchestration step

Workers should rebase onto this baseline rather than continuing to develop isolated module scaffolds.
