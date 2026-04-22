# Agent Git Rules

Date: 2026-04-22
Project: Oral-D Track A
Purpose: short git policy to paste into worker and verifier agent prompts

## Branching

- Branch from the current frozen baseline branch, not from another worker branch.
- Use an ownership-specific branch name.

Examples:
- `instance/video-player`
- `instance/camera-pipeline`
- `instance/face-tracking`
- `instance/feature-engineering`
- `instance/data-storage`
- `instance/setup-calibration`
- `instance/webview-visualization`
- `instance/validation-toolkit`
- `verifier/runtime`
- `verifier/data-ui`
- `verifier/final-integration`

## Allowed Git Actions

- Create your own branch.
- Commit your own changes on your own branch.
- Rebase your branch onto the latest frozen baseline when instructed.
- Push your own branch after reporting build and status results.

## Disallowed Git Actions

- Do not merge into `main`.
- Do not merge other worker branches.
- Do not rewrite another agent's branch.
- Do not force-push shared integration branches unless explicitly instructed by the orchestrator.
- Do not amend or rewrite shared history just to improve commit-message aesthetics.
- Do not create ad hoc integration branches without orchestrator approval.

## Reporting Requirement

Before asking for review or merge consideration, report:
- branch name
- `HEAD` commit hash
- whether the working tree is clean
- exact build or test command run
- whether the branch was pushed and to which remote

## Push Rule

- If no remote exists, report that clearly and stop.
- Do not invent a remote name or URL.
- Once a remote exists, push with upstream tracking:

```bash
git push -u origin <branch-name>
```

## Readiness Rule

A branch is ready for verifier review when:
- write scope stayed within ownership boundaries
- working tree is clean
- relevant build commands passed
- blockers and caveats were reported clearly

That does not mean it is ready to merge into `main`.
It means it is ready for verifier review and integration-branch consideration.
