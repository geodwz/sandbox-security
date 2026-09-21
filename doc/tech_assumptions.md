# Technical Assumptions and Early Risks

## Fixed technology choices

- Java 25, Spring Boot 4.1.1, Maven, Spring MVC, Spring Security, and Spring
  OAuth2 Resource Server for the API.
- Java 25, Spring Boot 4.1.1, Spring OAuth2 Client, and Client Credentials for
  the M2M application.
- Vue 3.5, TypeScript, Vite, Vue Router, and Pinia for the public SPA.
- Keycloak in Docker, with PostgreSQL used only for Keycloak persistence.

## Local configuration assumptions

- The expected local Vue origin is `http://localhost:5173`; backend CORS will
  explicitly allow it for development.
- Keycloak issuer/client URLs, API base URL, M2M client ID, and M2M secret will
  be externalized. Local administrator credentials may be Docker environment
  values, while production guidance covers proper secret stores.
- The tutorial's project data stays in memory and is reset on API restart.

## Risks to resolve in later phases

1. Keycloak import must emit `project-api` in `aud` and client roles in the
   `resource_access.project-api.roles` path while maintaining the required
   composite role hierarchy.
2. Scope configuration must yield the exact `project:read`, `project:write`,
   and `project:delete` authorities for service accounts without giving them
   human roles.
3. The browser flow requires a PKCE-capable OIDC client implementation that
   does not persist a secret or treat the ID token as an API credential.
4. Audience validation, safe 401/403 responses, CORS, and real-Keycloak
   integration tests require careful configuration and must not be disabled for
   test convenience.
5. A real Keycloak integration path may need Docker/Testcontainers availability;
   its test strategy is specified and approved before implementation.
