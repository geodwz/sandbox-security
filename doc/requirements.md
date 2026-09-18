# requirements.md

## 1. Purpose

Build a complete, didactic, end-to-end tutorial project that teaches how to secure a Spring Boot 4 API with Keycloak for two distinct OAuth2/OIDC scenarios:

1. **U2M (User-to-Machine)**
   - Human user authenticates through Keycloak.
   - Frontend is a Vue 3.5 SPA.
   - Authentication uses OpenID Connect and OAuth2 Authorization Code Flow with PKCE.
   - Authorization is role-based using three application roles:
     - `user`
     - `editor`
     - `admin`

2. **M2M (Machine-to-Machine)**
   - A Spring Boot application authenticates as a machine client.
   - Authentication uses OAuth2 Client Credentials.
   - Authorization is scope-based using:
     - `project:read`
     - `project:write`
     - `project:delete`

The backend API must accept JWT access tokens issued by Keycloak and authorize both human and machine callers.

The tutorial must be understandable by junior developers and must explain why each step exists, not only how to configure it.

---

## 2. Learning Objectives

At the end of the tutorial, the learner must be able to explain and implement:

- The difference between authentication and authorization.
- The difference between OAuth2 and OpenID Connect.
- The roles of:
  - Resource Owner
  - Client
  - Authorization Server
  - Resource Server
- Why U2M and M2M use different OAuth2 flows.
- Why a Vue SPA must use Authorization Code + PKCE.
- Why a browser-based SPA must not contain a client secret.
- Why M2M should use Client Credentials.
- The difference between:
  - ID token
  - access token
  - refresh token
- How Spring Security validates JWT access tokens.
- How Keycloak roles are mapped to Spring Security authorities.
- How OAuth2 scopes are mapped to Spring Security authorities.
- How to distinguish HTTP `401 Unauthorized` and `403 Forbidden`.
- Why frontend authorization is a UX concern, not a security boundary.
- Why the API must validate issuer, signature, expiration, and audience.
- How to test authorization independently from identity-provider integration.
- How to test the complete Keycloak integration end to end.

---

## 3. Repository Structure

Create one repository with the following structure:

```text
./
├── compose.yaml
├── README.md
├── doc/
│   ├── requirements.md
│   └── architectures.md
│
├── docker/
│   └── keycloak/realm-export.json
│
├── backend-api/
│   ├── pom.xml
│   └── src/
│
├── frontend-vue/
│   ├── package.json
│   └── src/
│
└── m2m-client/
    ├── pom.xml
    └── src/
```

The repository must be runnable locally with Docker for Keycloak and standard local development commands for the applications.

---

## 4. Tutorial Domain

Use a small domain named **Project API**.

### 4.1 Backend endpoints

Implement:

```text
GET    /api/projects
GET    /api/projects/{id}
POST   /api/projects
PUT    /api/projects/{id}
DELETE /api/projects/{id}

GET    /api/me

GET    /api/admin/users
GET    /api/admin/security
```

### 4.2 Public endpoint

Also expose:

```text
GET /api/public/health
```

This endpoint must not require authentication.

---

## 5. U2M Authorization Model

### 5.1 Keycloak client roles

Create client roles for the backend API client:

```text
user
editor
admin
```

These must be application-specific client roles, not general-purpose realm roles.

### 5.2 Role semantics

#### `user`

Can:

- View project list.
- View project details.
- View own authenticated profile.

Cannot:

- Create projects.
- Update projects.
- Delete projects.
- Access administration endpoints.

#### `editor`

Can:

- Perform every `user` action.
- Create projects.
- Update projects.

Cannot:

- Delete projects.
- Access administration endpoints.

#### `admin`

Can:

- Perform every `editor` action.
- Delete projects.
- Access administration endpoints.

### 5.3 Composite-role model

Configure Keycloak role inheritance:

```text
editor
└── includes user

admin
└── includes editor
    └── includes user
```

The resulting effective role hierarchy is:

```text
USER
  ↑
EDITOR
  ↑
ADMIN
```

### 5.4 Tutorial users

Create three users:

```text
alice
  role: user

bob
  role: editor

charlie
  role: admin
```

---

## 6. M2M Authorization Model

Create OAuth2 scopes:

```text
project:read
project:write
project:delete
```

Suggested machine clients:

```text
report-generator
  project:read

data-importer
  project:read
  project:write

maintenance-service
  project:read
  project:write
  project:delete
```

Machine identities must be authorized with scopes rather than human business roles.

---

## 7. Combined Authorization Rules

The backend must support both role-based U2M authorization and scope-based M2M authorization.

### 7.1 Authorization matrix

| Endpoint | U2M roles | M2M scopes |
|---|---|---|
| `GET /api/projects` | `user`, `editor`, `admin` | `project:read` |
| `GET /api/projects/{id}` | `user`, `editor`, `admin` | `project:read` |
| `POST /api/projects` | `editor`, `admin` | `project:write` |
| `PUT /api/projects/{id}` | `editor`, `admin` | `project:write` |
| `DELETE /api/projects/{id}` | `admin` | `project:delete` |
| `GET /api/me` | authenticated U2M user | not required for M2M tutorial |
| `/api/admin/**` | `admin` | no default M2M access |
| `/api/public/**` | public | public |

The backend policy must intentionally support either the corresponding U2M role or M2M scope for project operations.

---

## 8. Implementation Phases

Implement the tutorial in the exact progression below.

---

# Phase 1 — Security Concepts

## Step 1. Explain Authentication vs Authorization

Teach:

```text
Authentication:
Who or what is making the request?

Authorization:
What may the authenticated identity do?
```

Use examples:

```text
Human:
alice

Machine:
report-generator
```

Introduce:

```text
Authentication
    ↓
Principal
    ↓
Authorities
    ↓
Authorization decision
```

Acceptance criteria:

- The tutorial clearly distinguishes authentication and authorization before introducing Spring Security configuration.
- The learner understands that a valid identity does not automatically imply permission.

---

## Step 2. Explain OAuth2 Actors

For U2M:

```text
Resource Owner: Alice
Client: Vue SPA
Authorization Server: Keycloak
Resource Server: Spring Boot API
```

For M2M:

```text
Client: Spring Boot M2M client
Authorization Server: Keycloak
Resource Server: Spring Boot API
```

Acceptance criteria:

- U2M and M2M actors are shown separately.
- The tutorial explicitly notes that M2M has no interactive human resource owner.

---

## Step 3. Explain OAuth2 vs OpenID Connect

Explain:

- OAuth2 focuses on delegated authorization.
- OpenID Connect adds an identity layer.
- U2M uses OIDC + OAuth2.
- M2M uses OAuth2 Client Credentials.

Explain token types:

```text
ID token:
Represents authenticated user identity to the client.

Access token:
Credential sent to the protected API.

Refresh token:
Allows a client to obtain a new access token.
```

Security invariant:

```text
The API consumes access tokens, not ID tokens.
```

---

# Phase 2 — Keycloak Infrastructure

## Step 4. Start Keycloak with Docker

Create `compose.yaml`.

Requirements:

- Keycloak runs in Docker.
- The tutorial uses a predictable development port.
- Development administrator credentials may be defined through environment variables for local use.
- Production-secret handling must be discussed separately.

Acceptance criteria:

- `docker compose up` starts Keycloak.
- Keycloak administration console is reachable.
- Keycloak can import the tutorial realm.

---

## Step 5. Create Realm

Create:

```text
realm: tutorial
```

Acceptance criteria:

- Realm exists.
- Realm can be exported as `keycloak/realm-export.json`.

---

## Step 6. Create Backend API Client

Create a Keycloak client representing the protected API:

```text
client-id: project-api
```

Requirements:

- Configure an audience representing `project-api`.
- Define client roles:
  - `user`
  - `editor`
  - `admin`
- Configure role hierarchy:
  - `editor` includes `user`
  - `admin` includes `editor`

Acceptance criteria:

- Effective roles appear in access tokens for assigned users.
- Access tokens intended for the backend include the expected audience.

---

## Step 7. Create Tutorial Users

Create:

```text
alice → user
bob → editor
charlie → admin
```

Acceptance criteria:

- Alice gets effective role `user`.
- Bob gets effective roles `editor` and `user`.
- Charlie gets effective roles `admin`, `editor`, and `user`.

---

# Phase 3 — Backend API Base

## Step 8. Create Spring Boot Backend

Create `backend-api`.

Technical dependencies:

- Spring Web
- Spring Security
- OAuth2 Resource Server

Use Java 25 and Spring Boot 4.

Create the project endpoints with an initially simple in-memory project service.

Acceptance criteria:

- API starts successfully.
- Project CRUD works before security is introduced.
- Public health endpoint exists.

---

## Step 9. Enable Resource Server Security

Configure Spring Security so that:

```text
/api/public/** → permitAll
/api/**        → authenticated
```

Configure:

```text
spring.security.oauth2.resourceserver.jwt.issuer-uri
```

Also configure expected audience validation for:

```text
project-api
```

Acceptance criteria:

- Public endpoint works without a token.
- Protected endpoints return 401 without a token.
- Valid Keycloak access token is accepted.
- Wrong issuer token is rejected.
- Wrong audience token is rejected.
- Expired token is rejected.

---

## Step 10. Explain JWT Validation Pipeline

Teach:

```text
HTTP request
   ↓
Bearer token extraction
   ↓
JWT decoder
   ↓
Signature validation
   ↓
Issuer validation
   ↓
Expiration validation
   ↓
Audience validation
   ↓
Authentication
   ↓
SecurityContext
```

Acceptance criteria:

- Tutorial explains each validation step in plain language.
- Tutorial explains that decoding is not equivalent to validating.

---

# Phase 4 — U2M Authentication

## Step 11. Register Vue Client

Create a Keycloak client:

```text
client-id: vue-client
```

Requirements:

- Public client.
- No client secret.
- Authorization Code flow enabled.
- PKCE S256 used.
- Development redirect URI points to the Vue development application.

Example:

```text
http://localhost:5173/*
```

Acceptance criteria:

- Vue client authenticates through Keycloak.
- No client secret appears in frontend source code.

---

## Step 12. Create Vue 3.5 Frontend

Create `frontend-vue`.

Required UI:

- Login button.
- Logout button.
- Current username display.
- Current roles display.
- Project list.
- Project creation form.
- Project editing UI.
- Delete action visible only to admins.
- Admin section visible only to admins.

Acceptance criteria:

- App starts locally.
- Anonymous user can initiate login.
- Authenticated user information is visible.

---

## Step 13. Implement Authorization Code + PKCE Flow

Teach the flow:

```text
User
  ↓
Vue
  ↓
Keycloak authorization endpoint
  ↓
User authenticates
  ↓
Authorization code
  ↓
PKCE validation
  ↓
Access token
```

Acceptance criteria:

- Login uses redirect-based Authorization Code flow.
- PKCE S256 is enabled.
- No Implicit Flow is used.
- No password grant is used.

---

## Step 14. Call Backend with Bearer Token

Vue must send:

```http
Authorization: Bearer <access-token>
```

for protected API calls.

Acceptance criteria:

- Authenticated Vue user can call protected backend.
- Missing token returns 401.

---

# Phase 5 — U2M Role Mapping

## Step 15. Inspect Keycloak Role Claims

Teach how Keycloak client roles are represented in JWT claims.

Expected conceptual structure:

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

Acceptance criteria:

- Tutorial shows the token claim path.
- Tutorial explains why the backend should isolate Keycloak-specific claim parsing.

---

## Step 16. Implement Keycloak Role Converter

Create a dedicated converter that maps:

```text
resource_access.project-api.roles
```

to Spring authorities:

```text
user   → ROLE_USER
editor → ROLE_EDITOR
admin  → ROLE_ADMIN
```

Use a Spring Security JWT authentication converter.

Acceptance criteria:

- Converter is isolated in a dedicated class.
- Converter does not contain endpoint logic.
- Unknown roles do not produce privileged authorities.
- Missing claim does not crash the application.

---

## Step 17. Implement `/api/me`

Return a backend-owned representation of the authenticated user.

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

Acceptance criteria:

- Frontend does not need to parse raw Keycloak token structure to discover roles.
- Backend response contains normalized application role names.

---

# Phase 6 — U2M Role Authorization

## Step 18. Configure HTTP Authorization

Implement equivalent behavior:

```text
GET project endpoints:
  USER or EDITOR or ADMIN

POST project endpoints:
  EDITOR or ADMIN

PUT project endpoints:
  EDITOR or ADMIN

DELETE project endpoints:
  ADMIN

/admin/**:
  ADMIN
```

Acceptance criteria:

- Alice can read but cannot create, update, delete, or use admin endpoints.
- Bob can read/create/update but cannot delete or use admin endpoints.
- Charlie can perform all operations.

---

## Step 19. Add Method Security

Enable method-level authorization.

Protect business operations, for example:

```text
create/update:
  EDITOR or ADMIN

delete:
  ADMIN
```

Acceptance criteria:

- Important business operations remain protected if later exposed through a different controller.
- Tutorial explains HTTP-level vs service-level authorization.

---

## Step 20. Add Vue Authorization Helpers

Implement a central authentication/authorization abstraction such as:

```text
AuthService
```

or:

```text
useAuth()
```

Expose normalized role helpers:

```text
isUser
isEditor
isAdmin
hasRole(role)
hasAnyRole(...)
```

Do not scatter raw Keycloak token parsing throughout Vue components.

Acceptance criteria:

- Components consume normalized authorization helpers.
- Keycloak-specific implementation is isolated.

---

## Step 21. Add Route Guards

Frontend route rules:

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

Acceptance criteria:

- Unauthorized navigation is blocked or redirected appropriately.
- Tutorial explicitly states that Vue route guards are not security boundaries.

---

## Step 22. Prove Frontend Is Not the Security Boundary

Exercise:

- Login as Alice.
- Confirm delete button is not shown.
- Manually send a `DELETE` request using Alice's valid token.

Expected result:

```text
403 Forbidden
```

Acceptance criteria:

- Tutorial demonstrates that hiding UI elements does not secure backend operations.

---

# Phase 7 — M2M Authentication

## Step 23. Explain Client Credentials

Teach:

```text
M2M client
   ↓
client_id + client_secret
   ↓
Keycloak token endpoint
   ↓
access token
   ↓
backend API
```

Explicitly contrast with U2M:

```text
No browser
No redirect
No login page
No human user
No authorization code
```

---

## Step 24. Create M2M Client in Keycloak

Create:

```text
project-report-client
```

Requirements:

- Confidential client.
- Client authentication enabled.
- Service account enabled.
- Only required scopes are granted.

Initial scope:

```text
project:read
```

Acceptance criteria:

- Client secret exists.
- Service account can obtain access token.

---

## Step 25. Obtain Token Manually

Before using Spring OAuth2 Client support, teach raw protocol behavior with `curl`.

Use:

```text
grant_type=client_credentials
client_id=project-report-client
client_secret=<secret>
```

Acceptance criteria:

- Access token is returned.
- Learner inspects JWT claims.
- Tutorial explains that the token represents a machine identity.

---

## Step 26. Test M2M Scope Authorization

With a `project:read` token:

```text
GET /api/projects
→ 200
```

Then:

```text
POST /api/projects
→ 403
```

Acceptance criteria:

- Tutorial proves that valid authentication does not imply authorization.

---

# Phase 8 — M2M Spring Boot Client

## Step 27. Create M2M Application

Create `m2m-client`.

Dependencies:

- Spring Web
- Spring Security
- OAuth2 Client

Use Java 25 and Spring Boot 4.

Acceptance criteria:

- Application starts.
- Application configuration contains OAuth2 Client registration.

---

## Step 28. Configure Client Credentials

Configure the M2M client with:

```text
client-id: project-report-client
client-secret: environment variable
authorization-grant-type: client_credentials
scope:
  - project:read
```

Security requirements:

- Secret must not be committed.
- Secret must not appear in source code.
- Secret must not appear in Vue frontend.

Acceptance criteria:

- Application can request tokens from Keycloak.

---

## Step 29. Implement Automatic Token Acquisition

Use Spring Security OAuth2 Client infrastructure.

The M2M application must:

1. Request an access token.
2. Reuse valid token where appropriate.
3. Obtain a new token when necessary.
4. Attach bearer token to backend API request.

Do not manually duplicate token logic across services.

Acceptance criteria:

- M2M client successfully calls backend without manually copying tokens.
- Token lifecycle is managed by Spring Security infrastructure.

---

# Phase 9 — Combined Role and Scope Authorization

## Step 30. Support U2M Roles and M2M Scopes in Backend

The backend must support both:

```text
ROLE_USER
ROLE_EDITOR
ROLE_ADMIN
```

and:

```text
SCOPE_project:read
SCOPE_project:write
SCOPE_project:delete
```

Authorization behavior:

```text
Read:
ROLE_USER
OR ROLE_EDITOR
OR ROLE_ADMIN
OR SCOPE_project:read
```

```text
Create/update:
ROLE_EDITOR
OR ROLE_ADMIN
OR SCOPE_project:write
```

```text
Delete:
ROLE_ADMIN
OR SCOPE_project:delete
```

Acceptance criteria:

- Human and machine callers can use the same project API.
- Their permissions are represented differently but converge to Spring `GrantedAuthority`.

---

# Phase 10 — Token Security Deep Dive

## Step 31. Explain JWT Anatomy

Teach:

```text
Header
Payload
Signature
```

Relevant claims:

```text
iss
sub
aud
exp
iat
scope
azp/client_id
preferred_username
resource_access
```

Acceptance criteria:

- Tutorial explains what each relevant claim means.
- Tutorial warns that visible claims are not proof of authenticity until signature validation succeeds.

---

## Step 32. Explain Signature Validation

Teach:

```text
Keycloak private key
   ↓
signs JWT

Spring API
   ↓
uses Keycloak public key/JWKS
   ↓
verifies JWT
```

Acceptance criteria:

- Tutorial explains why backend does not need user password or M2M client secret to validate JWT.

---

## Step 33. Explain Issuer Validation

Teach:

```text
iss
```

as:

```text
Who issued this token?
```

Acceptance criteria:

- Wrong issuer is rejected.

---

## Step 34. Explain Audience Validation

Teach:

```text
aud
```

as:

```text
Which resource/API is this token intended for?
```

Required audience:

```text
project-api
```

Acceptance criteria:

- Token issued for another audience is rejected.

---

# Phase 11 — Error Handling

## Step 35. Demonstrate 401

Cases:

- Missing token.
- Malformed token.
- Expired token.
- Invalid signature.
- Wrong issuer.
- Wrong audience.

Expected:

```text
401 Unauthorized
```

Acceptance criteria:

- Tutorial explains that authentication failed.

---

## Step 36. Demonstrate 403

Case:

```text
Valid Alice token
DELETE /api/projects/{id}
```

Expected:

```text
403 Forbidden
```

Acceptance criteria:

- Tutorial explains that caller is authenticated but lacks required authority.

---

## Step 37. Implement Safe Error Responses

Use a consistent API error representation.

Example:

```json
{
  "status": 403,
  "error": "forbidden",
  "message": "Insufficient permissions"
}
```

Security rule:

Do not expose:

- JWT content
- client secrets
- refresh tokens
- stack traces
- internal authorization logic details

---

# Phase 12 — CORS and Browser Security

## Step 38. Configure CORS

Development origins:

```text
Vue:
http://localhost:5173

API:
configured separately
```

Requirements:

- Explicitly allow known frontend origin.
- Do not recommend wildcard origins as production default.

Acceptance criteria:

- Browser can call API.
- Unauthorized origins are not broadly permitted.

---

## Step 39. Explain Browser Token Storage Risks

Discuss:

- in-memory storage
- session storage
- local storage
- BFF pattern

Required teaching point:

```text
Long-lived bearer tokens stored in browser-accessible persistent storage increase XSS impact.
```

The tutorial may keep the SPA implementation simple but must explain the trade-offs.

---

# Phase 13 — Testing

## Step 40. Create U2M Authorization Matrix Tests

Test matrix:

| Request | Anonymous | USER | EDITOR | ADMIN |
|---|---:|---:|---:|---:|
| `GET /api/projects` | 401 | 200 | 200 | 200 |
| `POST /api/projects` | 401 | 403 | 201 | 201 |
| `PUT /api/projects/{id}` | 401 | 403 | 200 | 200 |
| `DELETE /api/projects/{id}` | 401 | 403 | 403 | 204 |
| `GET /api/admin/security` | 401 | 403 | 403 | 200 |

Acceptance criteria:

- Every cell is covered by an automated test or justified equivalent.

---

## Step 41. Test Role Converter Separately

Test:

```text
Keycloak role user → ROLE_USER
Keycloak role editor → ROLE_EDITOR
Keycloak role admin → ROLE_ADMIN
```

Also test:

- missing `resource_access`
- missing `project-api`
- empty role list
- unknown role

Acceptance criteria:

- Converter is deterministic and defensive.

---

## Step 42. Test M2M Scope Authorization

Test:

```text
project:read
  GET → success
  POST → forbidden

project:write
  POST → success

project:delete
  DELETE → success
```

Acceptance criteria:

- Scope checks are covered independently from Keycloak availability.

---

## Step 43. Integration Test with Real Keycloak

Use Docker/Testcontainers-compatible infrastructure if practical.

Integration cases:

- Alice can read.
- Alice cannot create.
- Bob can create.
- Bob cannot delete.
- Charlie can delete.
- M2M read-only client can read.
- M2M read-only client cannot write.
- Expired token fails.
- Wrong audience fails.

Acceptance criteria:

- At least one test exercises real token issuance and real backend token validation.

---

# Phase 14 — Observability

## Step 44. Add Safe Security Logging

May log:

- subject
- client id
- endpoint
- authorization result
- correlation/request id

Must not log:

- access token
- refresh token
- password
- client secret

Example:

```text
principal=project-report-client
operation=GET /api/projects
result=AUTHORIZED
```

Acceptance criteria:

- Secrets and raw bearer tokens never appear in application logs.

---

# Phase 15 — Production Guidance

## Step 45. Explain Production Topology

Discuss:

```text
Internet
   ↓
Reverse proxy / API gateway
   ├── Vue frontend
   └── Spring Boot API
           ↓
        Keycloak
```

Required concerns:

- HTTPS
- secure DNS
- trusted proxy configuration
- Keycloak persistence
- CORS
- token lifetime
- key rotation
- audience validation
- secret management

---

## Step 46. Explain Secret Management

For local tutorial:

- environment variables are acceptable.

For production, discuss:

- Kubernetes Secrets
- HashiCorp Vault
- cloud secret managers
- CI/CD secret stores

Security invariant:

```text
Client secrets belong to confidential machine clients,
not Java source code,
not Git,
not Vue applications.
```

---

# Phase 16 — Final End-to-End Walkthrough

## Step 47. Complete U2M Flow

Run:

1. Start Keycloak.
2. Import realm.
3. Start backend API.
4. Start Vue frontend.
5. Login as Alice.
6. Call `GET /api/projects`.
7. Verify 200.
8. Attempt `POST /api/projects`.
9. Verify 403.
10. Login as Bob.
11. Create/update project.
12. Attempt delete.
13. Verify 403.
14. Login as Charlie.
15. Delete project.
16. Access admin endpoint.
17. Verify success.

---

## Step 48. Complete M2M Flow

Run:

1. Start M2M client.
2. Obtain Client Credentials token automatically.
3. Call `GET /api/projects`.
4. Verify 200.
5. Call `POST /api/projects`.
6. Verify 403 with read-only scope.
7. Grant `project:write`.
8. Obtain a fresh token.
9. Retry.
10. Verify success.

---

## Step 49. Final Security Demonstration

Prove:

```text
Valid token != unlimited permissions
```

Demonstrate:

- authenticated human user denied by role
- authenticated machine denied by scope
- frontend UI restriction bypass attempt still denied by backend
- wrong-audience token rejected before authorization

---

## 9. Security Invariants

The implementation must always preserve the following rules:

1. Vue must not contain client secrets.
2. U2M uses Authorization Code + PKCE.
3. M2M uses Client Credentials.
4. No Resource Owner Password flow.
5. No Implicit Flow.
6. Backend consumes access tokens, not ID tokens.
7. JWT signature must be validated.
8. JWT issuer must be validated.
9. JWT expiration must be validated.
10. JWT audience must be validated.
11. U2M authorization is based on `user`, `editor`, `admin`.
12. M2M authorization is based on OAuth2 scopes.
13. Frontend role checks are never considered a security boundary.
14. Backend authorization is authoritative.
15. Secrets are never committed to source control.
16. Tokens and secrets are never logged.
17. Keycloak-specific claim parsing is isolated in a converter.
18. Business authorization should be protected at service level where relevant.
19. `401` and `403` must be handled distinctly.
20. Unknown or malformed claims must fail safely.

---

## 10. Naming Conventions

Use:

```text
Realm:
tutorial

Backend API client:
project-api

Vue client:
vue-client

M2M client:
project-report-client

Roles:
user
editor
admin

Spring authorities:
ROLE_USER
ROLE_EDITOR
ROLE_ADMIN

Scopes:
project:read
project:write
project:delete

Spring scope authorities:
SCOPE_project:read
SCOPE_project:write
SCOPE_project:delete
```

---

## 11. Non-Goals

Do not add unless explicitly introduced as an advanced extension:

- Spring Data persistence.
- Database-backed projects.
- Multi-tenancy.
- Fine-grained Keycloak Authorization Services.
- Social login.
- LDAP integration.
- SAML.
- WebFlux.
- Reactive security.
- Custom authorization server implementation.
- Password grant.
- Implicit grant.
- Browser-stored client secrets.

---

## 12. AI Agent Execution Rules

An AI implementation agent must:

1. Follow the tutorial phases in order.
2. Keep each phase buildable and testable.
3. Prefer small commits or logically isolated changes.
4. Explain every security configuration choice in documentation.
5. Never weaken security merely to make a test pass.
6. Never replace audience validation with issuer-only validation.
7. Never move backend authorization responsibility into Vue.
8. Keep Keycloak-specific JWT mapping isolated.
9. Use framework-native Spring Security OAuth2 support.
10. Keep U2M role authorization and M2M scope authorization conceptually distinct.
11. Add tests before considering a phase complete.
12. Keep examples understandable by junior developers.
13. Avoid deprecated OAuth2 flows and obsolete Keycloak Spring adapters.
14. Keep all secrets externalized.
15. Preserve the endpoint and authorization matrices defined in this document.

---

## 13. Definition of Done

The tutorial is complete when all of the following are true:

- Keycloak starts with Docker.
- Realm configuration can be imported.
- Alice, Bob, and Charlie authenticate through Vue.
- Their effective roles map to Spring Security authorities.
- Vue uses Authorization Code + PKCE.
- Backend validates JWT signature, issuer, expiration, and audience.
- Alice, Bob, and Charlie receive the expected authorization outcomes.
- M2M application authenticates with Client Credentials.
- M2M client uses Spring Security OAuth2 Client support.
- Scope authorization works.
- Role and scope authorization coexist in the backend.
- 401 and 403 scenarios are demonstrated.
- CORS is configured explicitly.
- Secrets are externalized.
- Security-sensitive values are not logged.
- Authorization unit tests exist.
- JWT-role conversion tests exist.
- M2M scope tests exist.
- At least one end-to-end Keycloak integration test exists.
- README explains how to run the complete system.
