# System Architecture — Initial Outline

```text
                 Authorization Code + PKCE
Vue 3.5 SPA  --------------------------------> Keycloak (realm: tutorial)
     |                                             |
     | bearer access token                         | JWT / JWKS / OIDC metadata
     v                                             v
Spring Boot Project API <-------------------- Spring Boot M2M client
  Resource Server             Client Credentials     OAuth2 Client
```

## Components

| Component | Responsibility |
| --- | --- |
| Keycloak | Authenticate users and confidential clients; issue JWTs; own client registrations, roles, and scopes. |
| Vue SPA | Redirect login, PKCE handling, UI state and UX route guards, access-token API calls. |
| Backend API | Validate JWTs, map authorities, enforce HTTP and service policy, serve projects and normalized `/api/me`. |
| M2M client | Acquire and manage Client Credentials tokens through Spring Security, then call the API. |

## Authorization paths

Project reads authorize `ROLE_USER` or higher **or** `SCOPE_project:read`.
Create/update authorize `ROLE_EDITOR`/`ROLE_ADMIN` **or**
`SCOPE_project:write`; deletion authorizes `ROLE_ADMIN` or
`SCOPE_project:delete`. `/api/me` and `/api/admin/**` remain human-only by
default. `/api/public/**` is public.

## Design constraints

Use Spring MVC/servlet APIs—not WebFlux. Keep backend claim mapping in a
dedicated converter and authorization in both HTTP security and sensitive
service methods. Keep frontend raw Keycloak claims behind a centralized auth
abstraction and use backend-normalized identity data. Configuration and M2M
secrets are externalized.
