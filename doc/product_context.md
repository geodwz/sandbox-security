# Product Context — Project API Security Tutorial

## Intent

Build an end-to-end, junior-developer-friendly tutorial that demonstrates how
one Spring Boot 4.1.1 API accepts and authorizes two distinct bearer-token
callers issued by Keycloak:

- A human using a Vue 3.5 SPA with OpenID Connect Authorization Code Flow and
  PKCE S256 (U2M).
- A confidential Spring Boot M2M client using OAuth2 Client Credentials.

The tutorial uses a small in-memory Project API domain so its focus remains on
security behavior rather than persistence.

## User-visible outcomes

- Alice (`user`) may read projects and view her normalized profile.
- Bob (`editor`) may read, create, and update projects.
- Charlie (`admin`) may also delete projects and access administration endpoints.
- A machine caller receives only the project actions its OAuth2 scopes allow.
- Authentication failure produces a safe 401; an authenticated but unauthorized
  caller receives a safe 403.
- The Vue UI reflects role permissions for usability, while the backend remains
  the only security authority.

## Boundaries

In scope: Keycloak Docker setup, tutorial realm and clients, in-memory CRUD,
JWT validation, role/scope mapping, Vue login and project UI, M2M token
acquisition, safe errors/logging, explicit CORS, tests, and run documentation.

Out of scope: persistence and Spring Data, databases for projects,
multi-tenancy, Keycloak Authorization Services as application policy, social or
enterprise login, SAML, WebFlux/reactive security, a custom authorization
server, password or implicit grants, and any browser client secret.

## Security decisions that cannot be traded away

- Keycloak is the authorization server; the Spring API is a stateless Resource
  Server and the authoritative authorization boundary.
- The API accepts access tokens only and validates signature, issuer, expiry,
  and `project-api` audience.
- U2M permissions map Keycloak client roles to `ROLE_USER`, `ROLE_EDITOR`, and
  `ROLE_ADMIN`; M2M permissions map scopes to `SCOPE_project:*`.
- Claim parsing is isolated in the backend. Tokens, passwords, and client
  secrets are never committed, exposed, or logged.
