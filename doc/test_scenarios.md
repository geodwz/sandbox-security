# Test Scenarios — Phase 2 Specification

Each ID must link to executable test(s) in Phase 3 and implemented component(s)
in Phase 4. Tests marked **real Keycloak** use real token issuance; the remaining
authorization tests must not depend on Keycloak availability.

## Authentication and safe errors

| ID | Given / when | Then | Test level |
| --- | --- | --- | --- |
| AUTH-001 | No token → `GET /api/public/health` | 200 | MVC |
| AUTH-002 | No token → protected project, admin, or `/api/me` endpoint | safe 401 | MVC |
| AUTH-003 | Malformed, expired, or untrusted-issuer token → protected endpoint | safe 401 | JWT/MVC |
| AUTH-004 | Validly signed token without `project-api` audience → protected endpoint | safe 401 | JWT/MVC |
| AUTH-005 | Valid token with insufficient authority → protected endpoint | safe 403, no sensitive data | MVC |
| ERR-001 | 401 or 403 occurs | documented status/error/message shape; no token, secret, or stack trace | MVC |

## Role conversion and normalized identity

| ID | Given / when | Then | Test level |
| --- | --- | --- | --- |
| ROLE-001 | Roles `[user]` are converted | exactly `ROLE_USER` | unit |
| ROLE-002 | Effective `[editor,user]` roles are converted | `ROLE_EDITOR`, `ROLE_USER` | unit |
| ROLE-003 | Effective `[admin,editor,user]` roles are converted | all matching `ROLE_*` authorities | unit |
| ROLE-004 | `resource_access`, `project-api`, or role list is missing/empty | no role authority and no exception | unit |
| ROLE-005 | Unknown role is converted | no privileged authority | unit |
| IDENTITY-001 | Authenticated human requests `GET /api/me` | normalized username and application roles | MVC |
| IDENTITY-002 | Machine-only caller requests `/api/me` | denied; no human profile | MVC |

## Project authorization matrix

| ID | Caller | Request | Expected | Test level |
| --- | --- | --- | --- | --- |
| U2M-001 | anonymous | every protected operation | 401 | MVC |
| U2M-002 | `ROLE_USER` | GET list/detail | 200 | MVC + service |
| U2M-003 | `ROLE_USER` | POST, PUT, DELETE, admin | 403 | MVC + service |
| U2M-004 | `ROLE_EDITOR` | GET, POST, PUT | 200/201 | MVC + service |
| U2M-005 | `ROLE_EDITOR` | DELETE, admin | 403 | MVC + service |
| U2M-006 | `ROLE_ADMIN` | every project operation and admin | success; DELETE 204 | MVC + service |
| M2M-001 | `SCOPE_project:read` | GET list/detail | 200 | MVC + service |
| M2M-002 | `SCOPE_project:read` | POST, PUT, DELETE | 403 | MVC + service |
| M2M-003 | `SCOPE_project:write` | POST and PUT | 200/201 | MVC + service |
| M2M-004 | `SCOPE_project:write` | DELETE | 403 | MVC + service |
| M2M-005 | `SCOPE_project:delete` | DELETE | 204 | MVC + service |
| M2M-006 | machine-only scopes | `/api/me`, `/api/admin/**` | denied by default | MVC + service |

Service-level variants prove a later controller cannot accidentally bypass the
business authorization policy.

## Keycloak, frontend, and M2M behavior

| ID | Given / when | Then | Test level |
| --- | --- | --- | --- |
| KC-001 | Fresh Docker import | realm `tutorial`, Alice/Bob/Charlie, required clients, roles, and scopes exist | integration/manual |
| KC-002 | Alice, Bob, Charlie authenticate | role hierarchy is user; editor+user; admin+editor+user | integration |
| KC-003 | API token is inspected | `aud` contains `project-api`; roles use documented client-role claim path | integration |
| KC-004 | `vue-client` login begins | redirect Auth Code flow with PKCE S256 and no secret | frontend/integration |
| KC-005 | read-only `project-report-client` gets a token | machine identity has only `project:read` | integration |
| KC-006 | real Alice/read-only M2M token calls API | real token validation and authorization behave as specified | real Keycloak |
| FE-001 | anonymous selects Login | centralized auth begins redirect-based PKCE login | unit/component |
| FE-002 | authenticated app loads | username and roles originate from `/api/me` | component/API mock |
| FE-003 | roles visit protected routes | guards permit only documented roles or redirect/block | router unit |
| FE-004 | non-admin UI renders | no delete or admin controls | component |
| FE-005 | protected frontend API call | access token is sent as bearer credential | API-client unit |
| FE-006 | auth/API call fails | safe 401/403 UX without token/secret exposure | component/API mock |
| M2MC-001 | external M2M registration starts | Client Credentials config works without source-embedded secret | context/unit |
| M2MC-002 | M2M invokes API | Spring OAuth2 Client acquires/reuses a bearer token | client integration |
| M2MC-003 | token acquisition fails | error is safe and contains no credential material | client integration |

## Non-functional and documentation checks

| ID | Check | Evidence |
| --- | --- |
| NFR-001 | CORS permits only known development frontend origin | backend configuration test |
| NFR-002 | Logs exclude tokens, passwords, refresh tokens, and client secrets | logging test/review |
| NFR-003 | frontend contains no secret, implicit flow, or password grant | source/configuration review |
| NFR-004 | README teaches U2M, M2M, 401/403, token-storage trade-offs, and frontend-bypass demonstration | documentation review |
| NFR-005 | no persistence, WebFlux, custom auth server, or other stated non-goal appears | architecture review |

## Phase 3 traceability rule

Name tests after the IDs (for example, `ROLE_004_missingClaimProducesNoRoles`).
At Phase 3 completion, append a **scenario → test → component** mapping here
and record expected failures in `progress.md`. Locked tests do not change during
implementation; a changed specification reopens this phase.

## Phase 3 scenario → test → component mapping

| Scenario IDs | Executable contract | Intended component(s) |
| --- | --- | --- |
| AUTH-001, AUTH-002, ERR-001, U2M-001…U2M-006, M2M-001…M2M-006, NFR-001 | `ProjectAuthorizationContractTests` | project controllers/service, security filter chain, CORS, error handlers |
| ROLE-001…ROLE-005 | `KeycloakRoleConverterContractTests` | `security.KeycloakRoleConverter` |
| FE-001…FE-005 | `auth-and-routing.contract.test.ts` | `auth/useAuth`, router, API client, role-aware views |
| M2MC-001…M2MC-003 | `M2mClientContractTests` | M2M OAuth2 configuration and project API client |
| AUTH-003, AUTH-004, KC-001, NFR-002 | `InfrastructureAndJwtContractTests` | security configuration and Keycloak realm export |
| KC-006 | `RealKeycloakContractTests` (enabled with `KEYCLOAK_INTEGRATION=true`) | running Keycloak OIDC metadata |

The real-Keycloak test is intentionally environment-gated because it requires a
running local Keycloak instance. Before release, it must be run with the
documented environment variables and extended with token-issuance coverage.
