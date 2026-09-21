# architectures.md

## 1. Purpose

This document defines the technical architecture and hard implementation constraints for the Spring Boot Security tutorial project.

It is optimized for use by an AI development agent.

The architecture supports:

- U2M authentication with OIDC/OAuth2 Authorization Code + PKCE.
- U2M authorization with Keycloak application roles.
- M2M authentication with OAuth2 Client Credentials.
- M2M authorization with OAuth2 scopes.
- A single Spring Boot Resource Server accepting both human and machine bearer tokens.

---

## 2. Hard Technical Constraints

### 2.1 Backend API

Required stack:

```text
Java: 25
Spring Boot: 4.1.1
Spring Web
Spring Security
Spring OAuth2 Resource Server
Keycloak
Docker
```

Architecture type:

```text
Spring MVC
Servlet stack
Stateless REST API
JWT bearer-token authentication
```

Do not use:

- WebFlux
- server-side HTTP sessions for API authentication
- custom password authentication
- Spring authorization server
- deprecated Keycloak Spring adapters

---

### 2.2 U2M Frontend

Required stack:

```text
Vue: 3.5
OAuth2 / OpenID Connect
Authorization Code Flow
PKCE S256
```

Client type:

```text
Public browser client
```

Security rule:

```text
No client secret may exist in the Vue application.
```

---

### 2.3 M2M Client

Required stack:

```text
Java: 25
Spring Boot: 4.1.1
Spring Web
Spring Security
Spring OAuth2 Client
```

Authentication flow:

```text
OAuth2 Client Credentials
```

Client type:

```text
Confidential client
```

Client secret must be supplied externally.

---

## 3. System Context

```text
                           ┌──────────────────────┐
                           │       Keycloak       │
                           │                      │
                           │ OAuth2 / OIDC        │
                           │ Authorization Server │
                           └──────────┬───────────┘
                                      │
                    ┌─────────────────┴──────────────────┐
                    │                                    │
                    │                                    │
          Authorization Code + PKCE              Client Credentials
                    │                                    │
                    ▼                                    ▼
          ┌──────────────────┐                 ┌───────────────────┐
          │ Vue 3.5 frontend │                 │ Spring Boot M2M   │
          │                  │                 │ client            │
          │ Human identity   │                 │ Machine identity  │
          └────────┬─────────┘                 └─────────┬─────────┘
                   │                                     │
                   │ Bearer JWT                          │ Bearer JWT
                   │                                     │
                   └──────────────────┬──────────────────┘
                                      ▼
                            ┌─────────────────────┐
                            │ Spring Boot 4 API   │
                            │ Java 25             │
                            │ Resource Server     │
                            └─────────────────────┘
```

---

## 4. Component Responsibilities

### 4.1 Keycloak

Responsible for:

- Authenticating users.
- Authenticating confidential M2M clients.
- Issuing access tokens.
- Issuing ID tokens for OIDC user login.
- Defining U2M roles.
- Defining M2M scope grants.
- Exposing OIDC metadata.
- Exposing JWKS/public signing keys.
- Maintaining client registrations.

Keycloak must not contain application business logic.

---

### 4.2 Vue Frontend

Responsible for:

- Redirecting users to Keycloak for login.
- Completing Authorization Code + PKCE.
- Holding current authentication state.
- Sending access token to backend.
- Rendering role-aware UI.
- Applying route guards for UX.
- Calling `/api/me` for normalized backend user information.

Vue is not responsible for enforcing security.

---

### 4.3 Backend API

Responsible for:

- Validating JWT access tokens.
- Validating issuer.
- Validating signature.
- Validating expiration.
- Validating audience.
- Mapping Keycloak client roles to Spring authorities.
- Mapping OAuth2 scopes to Spring authorities.
- Enforcing endpoint authorization.
- Enforcing service/business authorization.
- Returning normalized authenticated-user information.
- Producing safe 401/403 responses.

The backend is the authoritative security boundary.

---

### 4.4 M2M Client

Responsible for:

- Authenticating to Keycloak using Client Credentials.
- Managing machine access tokens using Spring Security.
- Sending bearer tokens to backend API.
- Requesting only required scopes.

The M2M client must not impersonate human roles.

---

## 5. Identity and Authorization Model

### 5.1 U2M Identity

Human identity example:

```text
alice
bob
charlie
```

U2M roles:

```text
user
editor
admin
```

Mapped Spring authorities:

```text
ROLE_USER
ROLE_EDITOR
ROLE_ADMIN
```

---

### 5.2 U2M Role Hierarchy

Business hierarchy:

```text
ADMIN
  includes EDITOR
    includes USER
```

Keycloak hierarchy:

```text
admin
  includes editor
    includes user
```

This should be implemented with Keycloak composite client roles.

---

### 5.3 M2M Identity

Machine identity examples:

```text
project-report-client
data-importer
maintenance-service
```

M2M scopes:

```text
project:read
project:write
project:delete
```

Mapped Spring authorities:

```text
SCOPE_project:read
SCOPE_project:write
SCOPE_project:delete
```

---

## 6. Authorization Model

### 6.1 Endpoint Matrix

| Endpoint | U2M | M2M |
|---|---|---|
| `GET /api/projects` | `ROLE_USER` or higher | `SCOPE_project:read` |
| `GET /api/projects/{id}` | `ROLE_USER` or higher | `SCOPE_project:read` |
| `POST /api/projects` | `ROLE_EDITOR` or `ROLE_ADMIN` | `SCOPE_project:write` |
| `PUT /api/projects/{id}` | `ROLE_EDITOR` or `ROLE_ADMIN` | `SCOPE_project:write` |
| `DELETE /api/projects/{id}` | `ROLE_ADMIN` | `SCOPE_project:delete` |
| `GET /api/me` | authenticated human | not needed |
| `/api/admin/**` | `ROLE_ADMIN` | denied unless explicitly added later |
| `/api/public/**` | public | public |

---

## 7. Authentication Flows

### 7.1 U2M Flow

```text
Human user
   │
   ▼
Vue SPA
   │
   │ authorization request + PKCE challenge
   ▼
Keycloak
   │
   │ user authentication
   ▼
Authorization code
   │
   ▼
Vue SPA
   │
   │ code + PKCE verifier
   ▼
Keycloak token endpoint
   │
   ▼
Access token + ID token
```

API call:

```text
Vue
  │
  │ Authorization: Bearer <access-token>
  ▼
Backend API
```

Security constraints:

- Must use Authorization Code.
- Must use PKCE S256.
- Must not use Implicit Flow.
- Must not use Resource Owner Password grant.
- Must not use a client secret in Vue.

---

### 7.2 M2M Flow

```text
Spring Boot M2M client
   │
   │ client_id
   │ client_secret
   │ grant_type=client_credentials
   ▼
Keycloak token endpoint
   │
   ▼
Access token
   │
   ▼
Backend API
```

Security constraints:

- No browser.
- No user login.
- No authorization code.
- No ID token required for API access.
- Client secret must be externalized.

---

## 8. JWT Architecture

### 8.1 Required Validation

Backend must validate:

```text
signature
issuer
expiration
audience
```

Expected audience:

```text
project-api
```

---

### 8.2 Important Claims

Expected claims may include:

```text
iss
sub
aud
exp
iat
scope
azp
client_id
preferred_username
resource_access
```

Do not depend on unvalidated claims.

---

### 8.3 Role Claim Mapping

Expected conceptual Keycloak structure:

```json
{
  "resource_access": {
    "project-api": {
      "roles": [
        "user",
        "editor"
      ]
    }
  }
}
```

Backend mapping:

```text
resource_access.project-api.roles.user
  → ROLE_USER

resource_access.project-api.roles.editor
  → ROLE_EDITOR

resource_access.project-api.roles.admin
  → ROLE_ADMIN
```

Implement this in a dedicated converter.

---

### 8.4 Scope Mapping

OAuth2 scopes are mapped by Spring Security to authorities such as:

```text
project:read
  → SCOPE_project:read

project:write
  → SCOPE_project:write

project:delete
  → SCOPE_project:delete
```

---

## 9. Spring Security Architecture

### 9.1 HTTP Security

Use a `SecurityFilterChain`.

Responsibilities:

- public paths
- authenticated paths
- role/scope endpoint rules
- Resource Server JWT setup
- CORS integration

Do not place business logic inside security configuration.

---

### 9.2 Method Security

Enable:

```text
@EnableMethodSecurity
```

Use method-level authorization for business-sensitive service methods.

Examples:

```text
create/update:
EDITOR or ADMIN or project:write scope

delete:
ADMIN or project:delete scope
```

---

### 9.3 Authorization Responsibility Split

```text
SecurityFilterChain
  → transport/HTTP-level security

@PreAuthorize
  → service/business operation security
```

Important business operations should not rely exclusively on controller path protection.

---

## 10. Vue Authorization Architecture

Use a centralized abstraction:

```text
AuthService
```

or:

```text
useAuth()
```

Expose:

```text
currentUser
isAuthenticated
roles
hasRole(role)
hasAnyRole(...)
isUser
isEditor
isAdmin
login()
logout()
```

Do not parse raw Keycloak role claims inside UI components.

---

## 11. Vue Route Model

Routes:

```text
/projects
  USER | EDITOR | ADMIN

/projects/new
  EDITOR | ADMIN

/projects/:id/edit
  EDITOR | ADMIN

/admin
  ADMIN
```

Route guards are for UX only.

Backend authorization remains authoritative.

---

## 12. `/api/me` Contract

Backend should expose:

```text
GET /api/me
```

Example:

```json
{
  "username": "bob",
  "roles": [
    "USER",
    "EDITOR"
  ]
}
```

Purpose:

- Normalize identity information.
- Hide Keycloak token structure from frontend.
- Provide backend-owned authorization representation.

---

## 13. Error Model

### 13.1 401

Use when authentication fails.

Examples:

- no token
- invalid token
- expired token
- invalid signature
- wrong issuer
- wrong audience

---

### 13.2 403

Use when authentication succeeds but authorization fails.

Examples:

- Alice attempts project creation.
- Bob attempts delete.
- Read-only M2M client attempts write.

---

### 13.3 Error Response Constraints

Safe example:

```json
{
  "status": 403,
  "error": "forbidden",
  "message": "Insufficient permissions"
}
```

Never expose:

- full access token
- refresh token
- client secret
- Keycloak credentials
- stack trace
- internal signing details

---

## 14. CORS Architecture

Development example:

```text
Vue:
http://localhost:5173

Backend:
separate origin
```

Requirements:

- Explicit allowed origin.
- Explicit methods.
- Explicit headers.
- Avoid wildcard production configuration.

---

## 15. Secret Management

### 15.1 Local Development

May use environment variables.

Example:

```text
PROJECT_CLIENT_SECRET
```

### 15.2 Production

Discuss/use appropriate secret store:

- Kubernetes Secrets
- Vault
- cloud secret managers
- CI/CD secret stores

Never commit secrets.

---

## 16. Docker Architecture

At minimum, Docker must run Keycloak.

Recommended local topology:

```text
Docker
└── Keycloak
```

Optional future extension:

```text
Docker Compose
├── Keycloak
├── backend-api
├── frontend-vue
└── m2m-client
```

The initial tutorial should keep infrastructure understandable for junior developers.

---

## 17. Testing Architecture

### 17.1 Authorization Unit/Integration Tests

Test role policy independently:

```text
USER
EDITOR
ADMIN
```

Test scope policy independently:

```text
SCOPE_project:read
SCOPE_project:write
SCOPE_project:delete
```

---

### 17.2 Converter Tests

Test mapping from Keycloak claims to authorities.

Required cases:

- user
- editor
- admin
- composite effective roles
- empty roles
- missing claim
- unknown role

---

### 17.3 Full Keycloak Integration

At least one test path must use:

```text
real Keycloak token issuance
    ↓
real JWT validation
    ↓
real backend authorization
```

Test wrong audience and insufficient permissions.

---

## 18. Observability Architecture

Safe logging fields:

```text
subject
client_id
username when appropriate
request path
authorization result
correlation id
```

Forbidden logging:

```text
access token
refresh token
client secret
password
```

---

## 19. Production Topology

Recommended conceptual deployment:

```text
                    Internet
                       │
                       ▼
              Reverse Proxy / Gateway
                 │               │
                 ▼               ▼
             Vue SPA        Spring Boot API
                                 │
                                 ▼
                              Keycloak
```

Production requirements to discuss:

- HTTPS everywhere.
- Secure reverse-proxy forwarding.
- Persistent Keycloak database.
- Key rotation.
- Token lifetime policy.
- Audience validation.
- Explicit CORS.
- Secret management.
- Restricted administration endpoints.

---

## 20. Package Responsibility Guidance

Suggested backend structure:

```text
backend-api/
└── src/main/java/.../
    ├── security/
    │   ├── SecurityConfiguration
    │   ├── KeycloakRoleConverter
    │   └── SecurityConstants
    │
    ├── project/
    │   ├── ProjectController
    │   ├── ProjectService
    │   └── Project
    │
    ├── identity/
    │   └── CurrentUserController
    │
    └── admin/
        └── AdminController
```

Rules:

- Security package owns claim mapping and framework security configuration.
- Domain packages do not parse Keycloak JWT claims directly.
- Controllers remain thin.
- Service layer owns business operations.
- Authorization annotations may protect service methods.

---

## 21. Configuration Rules

Externalize configuration.

Backend:

```text
KEYCLOAK_ISSUER_URI
EXPECTED_AUDIENCE
```

M2M client:

```text
KEYCLOAK_ISSUER_URI
PROJECT_CLIENT_ID
PROJECT_CLIENT_SECRET
```

Frontend:

```text
KEYCLOAK_URL
KEYCLOAK_REALM
KEYCLOAK_CLIENT_ID
API_BASE_URL
```

Never hardcode production secrets.

---

## 22. Security Invariants

The AI agent must never violate these rules:

1. Backend is stateless.
2. API accepts bearer access tokens.
3. U2M uses OIDC Authorization Code + PKCE.
4. Vue has no client secret.
5. M2M uses Client Credentials.
6. M2M secrets are externalized.
7. JWT signature is validated.
8. JWT issuer is validated.
9. JWT expiration is validated.
10. JWT audience is validated.
11. U2M permissions use roles.
12. M2M permissions use scopes.
13. Roles become `ROLE_*`.
14. Scopes become `SCOPE_*`.
15. Frontend authorization is not trusted for backend security.
16. Backend is authoritative.
17. `/api/me` returns normalized identity data.
18. Raw Keycloak claim parsing is isolated.
19. Unknown claims fail safely.
20. Tokens and secrets are never logged.
21. No Implicit Flow.
22. No password grant.
23. No deprecated Keycloak Spring adapter.
24. Do not invent a second identity system inside the application.
25. Do not mix human roles and machine scopes without explicit authorization policy.

---

## 23. Architecture Decision Summary

### ADR-001 — Keycloak as Authorization Server

Decision:

Use Keycloak as the OAuth2/OIDC Authorization Server.

Reason:

- Supports OIDC.
- Supports Authorization Code + PKCE.
- Supports Client Credentials.
- Supports client roles.
- Supports service accounts.
- Supports JWT signing and discovery metadata.

---

### ADR-002 — Spring Boot API as Resource Server

Decision:

The backend is an OAuth2 Resource Server.

Reason:

- Backend validates bearer access tokens.
- Backend does not perform interactive login.
- Backend remains stateless.

---

### ADR-003 — Roles for U2M Authorization

Decision:

Use Keycloak client roles:

```text
user
editor
admin
```

Reason:

These represent human business roles inside the application.

---

### ADR-004 — Scopes for M2M Authorization

Decision:

Use:

```text
project:read
project:write
project:delete
```

Reason:

Scopes represent machine-client capabilities and least-privilege access.

---

### ADR-005 — Composite Roles

Decision:

Use:

```text
admin → editor → user
```

Reason:

Avoid repetitive manual assignment and reflect business privilege hierarchy.

---

### ADR-006 — Dedicated JWT Role Converter

Decision:

Map Keycloak role claims in one dedicated backend component.

Reason:

- Isolate identity-provider-specific token structure.
- Keep business code independent from raw JWT claims.
- Make mapping testable.

---

### ADR-007 — `/api/me` as Normalization Boundary

Decision:

Frontend reads normalized user/role data from backend.

Reason:

- Avoid spreading Keycloak-token parsing throughout Vue.
- Keep frontend coupled to application concepts rather than IAM token layout.

---

### ADR-008 — Backend Authorization Is Authoritative

Decision:

All security-sensitive actions must be authorized in backend.

Reason:

A browser UI can be modified or bypassed.

---

## 24. AI Agent Constraints

When implementing this architecture, the AI agent must:

- Follow `requirements.md`.
- Keep the stack exactly within stated constraints.
- Prefer native Spring Security OAuth2 mechanisms.
- Avoid deprecated APIs.
- Keep security configuration explicit and teachable.
- Add tests for every authorization rule.
- Preserve all role and scope names exactly.
- Preserve client IDs unless a change is explicitly requested.
- Keep Keycloak integration replaceable by isolating provider-specific mapping.
- Keep code readable for junior developers.
- Add comments only where they explain security intent, not obvious syntax.
- Never reduce security requirements to simplify implementation.

---

## 25. Architecture Definition of Done

Architecture is correctly implemented when:

- Vue authenticates humans through Keycloak using Authorization Code + PKCE.
- Spring Boot API validates access tokens as Resource Server.
- Keycloak U2M roles map to Spring `ROLE_*`.
- M2M scopes map to Spring `SCOPE_*`.
- Backend supports both authorization models simultaneously.
- Role hierarchy works.
- Audience validation works.
- `/api/me` returns normalized role information.
- Project endpoints respect the authorization matrix.
- Admin endpoints are restricted to admin users.
- M2M client uses Client Credentials.
- M2M client secret is externalized.
- CORS is explicit.
- Tests cover roles, scopes, claim conversion, 401, 403, and real Keycloak integration.
