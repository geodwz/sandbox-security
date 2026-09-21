# M2M Client Instructions

## Activation

Apply this file to all files under `m2m-client/`, especially `.java`, `pom.xml`,
and `src/**` resources and tests. Also follow the repository-root `AGENTS.md`.

## Stack and authentication flow

- Use Java 25 and Spring Boot 4.1.1 with Spring Web, Spring Security, and
  Spring OAuth2 Client.
- Use OAuth2 Client Credentials only. This is a confidential machine client:
  no browser, human login, authorization code, PKCE exchange, or ID token is
  required for its API calls.
- Use Spring Security's OAuth2 client/token management rather than manually
  minting, parsing, or caching tokens.
- Request only the least-privilege scopes required: `project:read`,
  `project:write`, and/or `project:delete`.

## Secrets and configuration

Externalize `KEYCLOAK_ISSUER_URI`, `PROJECT_CLIENT_ID`, and
`PROJECT_CLIENT_SECRET`. A client secret must never be committed, placed in
source/resources, logged, returned in errors, or copied into documentation.
Do not impersonate human roles or make M2M authorization depend on `ROLE_*`.

When calling the backend, send its acquired access token as a bearer token and
handle token/acquisition failures without leaking credential or token material.

## Testing and verification

Follow IIC: write and obtain approval for executable behavior tests before
implementation, then preserve locked tests. Cover client-credentials
registration, externalized configuration, requested scopes, bearer propagation,
and safe failures. Run `./mvnw test` from `m2m-client/`.
