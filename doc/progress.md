# IIC Progress

| Phase | Status | Evidence | Gate |
| --- | --- | --- | --- |
| 0 — Init | Existing repository baseline | Independent module skeletons and documentation present | Historical baseline; no IIC tag asserted |
| 1 — Analysis | Approved | `product_context.md`, `active_context.md`, `system_arch.md`, `tech_assumptions.md` | Approved by user on 2026-09-21 |
| 2 — Specification | Pending human approval | `tech_context.md`, `test_scenarios.md` | Review specs and coverage before Phase 3 |
| 3 — Test construction | Ready for human test-lock review | Backend, frontend, M2M, infrastructure, and conditional real-Keycloak contracts created and run | Hard stop: approve test intent/sufficiency and lock tests before implementation |
| 4 — Implementation | In progress | Backend authorization, Keycloak import, frontend auth shell, and M2M registration are implemented | Complete full U2M/M2M API-call flows and documentation |
| 5 — Hardening/integration | In progress | Local module suites pass; real Keycloak OIDC metadata check passes | Complete end-to-end token issuance/authorization evidence and request release approval |

## Phase 2 coverage statement

The scenario set covers the complete endpoint matrix, role and scope conversion,
JWT validation, authorization failures, frontend UX boundaries, M2M token
handling, Keycloak import/real-token integration, CORS, secret/token safety, and
the tutorial walkthrough. It intentionally excludes every stated non-goal.

## Repository caveat

The working tree contains pre-existing user changes. They are not phase output
and have not been modified or cleared by this IIC run. A clean-tree commit/tag
cannot be truthfully asserted until their owner resolves them.

## Phase 3 execution evidence

- `backend-api/./mvnw test`: contract tests compile and fail as expected for
  missing endpoints/security policy, `KeycloakRoleConverter`, security
  configuration, and required Keycloak realm names. The real-Keycloak test is
  skipped until `KEYCLOAK_INTEGRATION=true` and `KEYCLOAK_ISSUER_URI` are set.
- `frontend-vue/npm test`: runner is installed and executes; the expected
  `@/auth/useAuth` module does not exist yet, so the contract suite fails before
  tests execute.
- `m2m-client/./mvnw test`: compiled and ran 3 tests; 2 expected failures for
  the absent OAuth2 configuration and backend client classes.

No production implementation was added. The expected failures establish that
the current skeleton does not satisfy the specified behavior. The test suite is
ready for the mandatory human review and lock; no locked test may change during
implementation without reopening Phase 3.

## Phase 5 integration evidence

- `docker compose up -d` starts PostgreSQL and Keycloak 26.7 with
  `start-dev --import-realm`.
- The updated export imports realm `tutorial` successfully.
- `KEYCLOAK_INTEGRATION=true KEYCLOAK_ISSUER_URI=http://localhost:8090/auth/realms/tutorial ./mvnw test -Dtest=RealKeycloakContractTests`
  passes against the running Keycloak OIDC metadata endpoint.
