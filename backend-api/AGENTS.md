# Backend API Instructions

## Activation

Apply this file to all files under `backend-api/`, especially `.java`, `pom.xml`,
and `src/**` resources and tests. Also follow the repository-root `AGENTS.md`.

## Stack and boundaries

- Use Java 25 and Spring Boot 4.1.1 with Spring MVC/servlet APIs.
- Build a stateless REST OAuth2 Resource Server. Do not use WebFlux, HTTP
  session authentication, custom password authentication, Spring Authorization
  Server, or deprecated Keycloak adapters.
- Keep Keycloak-specific JWT claim parsing in a dedicated converter in the
  security package. Domain code must not parse raw JWT claims.
- Keep controllers thin; services own business operations and sensitive service
  methods use method security where appropriate.

## Authorization contract

Validate JWT signature, issuer, expiration, and audience `project-api`. Map
`resource_access.project-api.roles` to `ROLE_USER`, `ROLE_EDITOR`, and
`ROLE_ADMIN`; rely on Spring's scope mapping for `SCOPE_project:read`,
`SCOPE_project:write`, and `SCOPE_project:delete`.

Apply the explicit authorization matrix:

| Operation | Human authority | Machine authority |
| --- | --- | --- |
| Read projects | `ROLE_USER` or higher | `SCOPE_project:read` |
| Create/update projects | `ROLE_EDITOR` or `ROLE_ADMIN` | `SCOPE_project:write` |
| Delete projects | `ROLE_ADMIN` | `SCOPE_project:delete` |
| `/api/me` | authenticated human | deny/not applicable |
| `/api/admin/**` | `ROLE_ADMIN` | deny by default |
| `/api/public/**` | public | public |

Use `SecurityFilterChain` for HTTP policy and `@EnableMethodSecurity` plus
`@PreAuthorize` for business-sensitive operations. `/api/me` returns normalized
backend-owned identity data; do not expose raw token structure.

## Testing and verification

Follow IIC strictly: create approved, executable tests before implementation and
do not edit locked tests. Test authorization independently of Keycloak and test
the role converter for each role, composite roles, missing/empty/unknown claims.
Cover 401 vs 403 behavior, role and scope paths, wrong audience, and insufficient
permissions. Include a real-Keycloak token path when adding integration coverage.

Run `./mvnw test` from `backend-api/`. Do not weaken security assertions merely
to make tests pass.
