# Project API Security Tutorial

This tutorial demonstrates a Spring Boot Resource Server accepting two OAuth2 callers issued by Keycloak: a Vue 3.5 SPA using Authorization Code + PKCE S256, and a Spring Boot M2M client using Client Credentials.

The API is the security boundary. Vue guards improve UX only; project operations require the backend to authorize either a human role or machine scope.

## Start locally

```bash
docker compose up -d
cd backend-api && ./mvnw spring-boot:run
cd frontend-vue && npm install && npm run dev
```

Keycloak runs at `http://localhost:8090/auth` and imports realm `tutorial`. Local tutorial users are `alice`, `bob`, and `charlie`; use password `password`.

## Authorization

| Caller | Permission |
| --- | --- |
| Alice (`user`) | Read projects and `/api/me` |
| Bob (`editor`) | Read, create, update |
| Charlie (`admin`) | All project operations and `/api/admin/**` |
| M2M `project:read` | Read projects only |

Missing or invalid tokens produce 401. Authenticated callers without the right role/scope receive 403. Hidden Vue controls do not secure backend operations.

## M2M call

Keep the confidential-client secret external:

```bash
export PROJECT_CLIENT_SECRET='local-development-secret'
cd m2m-client && ./mvnw spring-boot:run
curl http://localhost:8081/client/projects
```

Never commit or log client secrets or access tokens.

## Verification

```bash
cd backend-api && ./mvnw test
cd m2m-client && ./mvnw test
cd frontend-vue && npm run type-check && npm test
```

With Docker Compose running, verify live Keycloak OIDC metadata:

```bash
cd backend-api
KEYCLOAK_INTEGRATION=true KEYCLOAK_ISSUER_URI=http://localhost:8090/auth/realms/tutorial ./mvnw test -Dtest=RealKeycloakContractTests
```

See [`doc/`](./doc/) for requirements, architecture, test traceability, and IIC records.
