# Technical Context — Phase 2 Specification

## Runtime topology

| Component | Local endpoint / role | Required technology |
| --- | --- | --- |
| Keycloak | Docker, development port `8090`, realm `tutorial` | Keycloak OIDC/OAuth2 Authorization Server |
| Backend | independent servlet REST API | Java 25, Spring Boot 4.1.1, Spring MVC, Spring Security Resource Server |
| Frontend | `http://localhost:5173` | Vue 3.5, TypeScript, Vite, Vue Router, Pinia |
| M2M client | independent Spring application | Java 25, Spring Boot 4.1.1, Spring OAuth2 Client |

## Configuration contract

| Component | External configuration |
| --- | --- |
| Backend | `KEYCLOAK_ISSUER_URI`, `EXPECTED_AUDIENCE=project-api`, explicit frontend CORS origin |
| Frontend | `KEYCLOAK_URL`, `KEYCLOAK_REALM=tutorial`, `KEYCLOAK_CLIENT_ID=vue-client`, `API_BASE_URL` |
| M2M client | `KEYCLOAK_ISSUER_URI`, `PROJECT_CLIENT_ID=project-report-client`, `PROJECT_CLIENT_SECRET` |

No confidential value is checked into application code, frontend configuration,
or documentation examples. Local Keycloak administrator values are development
infrastructure inputs only.

## Token and authority contract

The backend accepts only Keycloak-issued access tokens. It validates signature,
issuer, expiry, and audience `project-api` before authorization. Keycloak client
roles under `resource_access.project-api.roles` map exclusively to `ROLE_USER`,
`ROLE_EDITOR`, and `ROLE_ADMIN`; OAuth2 `scope` values map to
`SCOPE_project:read`, `SCOPE_project:write`, and `SCOPE_project:delete`.

Vue sends an access token in an `Authorization: Bearer` header. It must not use
an ID token as an API credential. The M2M client obtains and renews access
tokens through Spring Security's Client Credentials support.
