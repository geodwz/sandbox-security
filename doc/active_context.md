# Active Context — Intent Integrity Chain

## Current phase

**Phase 2 — Specification.** Phase 1 analysis was approved. This artifact and
the Phase 2 specifications now define the pending review boundary. No production
implementation or executable test construction has begun under this IIC run.
Human approval is required before Phase 3 test construction.

## Source-of-truth documents

- `requirements.md`: behavior, tutorial sequence, non-goals, naming, and
  definition of done.
- `architectures.md`: stack, authorization model, JWT validation, and package
  responsibilities.
- `intent-integrity-chain.md`: mandatory phase gates and test lock process.

## Baseline observations

- The repository contains independent `backend-api`, `frontend-vue`, and
  `m2m-client` modules plus root Docker/Keycloak configuration.
- Backend and M2M are Java 25 / Spring Boot 4.1.1 Maven skeletons. The backend
  still needs Spring Security and OAuth2 Resource Server dependencies.
- Frontend is a Vue 3.5 / TypeScript / Vite / Pinia skeleton.
- Existing Keycloak configuration uses a different realm and client naming than
  the required `tutorial`, `vue-client`, and `project-report-client` naming;
  its role/scope setup must be reconciled in the infrastructure phase.
- The working tree already has user-owned changes outside this IIC work. They
  must be preserved. Consequently, phase-end clean-tree/tag requirements cannot
  be claimed until those changes are separately resolved by their owner.

## Phase 2 acceptance assessment

Intent, boundaries, stakeholders (tutorial learner, human API user, machine
client, and local developer), constraints, risks, and acceptance outcomes are
approved. The complete behavioral and test specification is now in
`test_scenarios.md`. Pending human approval: construct executable tests in
Phase 3, confirm their expected failures, and request the mandatory test lock.
