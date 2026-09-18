# AGENTS.md — sandbox-security

Follow every phase from `doc/requirements.md`, implements all steps, respects the all security invariants, 
and uses `doc/architectures.md` as the authoritative technical blueprint for this project.

Use skills: 
- /java-convention to apply Java 25 conventions
- /bce as architercture rules for je Java project
- /sbce as Spec-Driven BCE Workflow

Do not modify the file without express authorisation to:
- `doc/requirements.md`
- `doc/architectures.md`


## Architecture

Three independent modules in a flat layout:

| Module | Stack | Role | Entry |
|---|---|---|---|
| `backend-api/` | Spring Boot 4.1 + Java 25, Maven | OAuth 2 resource server (Keycloak) | `src/main/java/BackendApplication.java` |
| `frontend-vue/` | Vue 3.5 + Vite + Pinia | Auth Code + PKCE flow (ID token → localStorage) | `src/main.ts` |
| `m2m-client/` | Spring Boot 4.1 + OAuth 2 client | Client Credentials flow | `src/main/java/com/geodwz/m2mclient/ClientApplication.java` |

No framework wiring between modules. All auth connects to a Keycloak instance via :
- `docker/keycloak/keycloak.conf`
- `docker/keycloak/realm-export.json`

## Commands

```bash
# Start Keycloak (required by all modules)
cd docker/keycloak  # edit realm, clients, or ports here first
docker compose up -d

# Run individual modules
cd backend-api  && mvn spring-boot:run
cd frontend-vue && npm run dev
cd m2m-client   && mvn spring-boot:run
```

## Security setup (critical)

- **Keycloak realm** and **users** are created via docker setup scripts. Default users: `alice`/`password`, `bob`/`password`.
- **M2M client** credentials (`client_id`, `client_secret`) are in `docker/keycloak/realm-export.json`. Do not hardcode — the m2m-client loads them from environment at startup.
- **Backend authorization** uses a custom scope-to-authority mapper: OAuth2 scopes become `SCOPE_<name>` Spring Authority entries. Roles (U2M) map to `resource_access.project-api.roles` claims → lowercase role names as authorities.
- **Vue guards**: UI route guards in `frontend-vue/src/router/index.ts` check for ID token presence; they are UX-only, not security boundaries. Actual enforcement is on the backend via `@PreAuthorize`.

## Gotchas

- Modules have no shared dependencies or code generation between them — each builds independently.
- Tests use Spring Boot test slices and Vitest; no E2E/browser tests exist.
- Docker compose for Keycloak is in `docker/keycloak/` (not root) with an empty `compose.yaml` at the root placeholder.
