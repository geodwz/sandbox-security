# Codex Instructions — sandbox-security

## Scope and activation

This file applies to the whole repository. The more specific instruction file in
`backend-api/`, `m2m-client/`, or `frontend-vue/` applies in addition when a
task touches that directory. Use the rules selected by the changed path and
file type:

| Paths / file types | Use these instructions |
| --- | --- |
| `backend-api/**`, Java source, backend Maven configuration | this file + `backend-api/AGENTS.md` |
| `m2m-client/**`, Java source, M2M Maven configuration | this file + `m2m-client/AGENTS.md` |
| `frontend-vue/**`, `.vue`, `.ts`, `.tsx`, Vite/npm configuration | this file + `frontend-vue/AGENTS.md` |
| `docker/**`, `compose.yaml`, `docker-compose*.yml`, Keycloak realm/config files | this file; preserve the architecture and security invariants below |
| `doc/**`, `README.md`, Markdown | this file; do not change the authoritative requirements or architecture without explicit authorization |

For a cross-module change, apply every relevant module guide and keep the API,
frontend, M2M client, Keycloak configuration, and documentation consistent.

## Authoritative sources

- `doc/requirements.md` defines the product behavior and tutorial scope.
- `doc/architectures.md` defines the technical architecture and security
  constraints.
- `doc/intent-integrity-chain.md` defines the required delivery methodology.

Do not modify `doc/requirements.md` or `doc/architectures.md` without explicit
user authorization. Do not silently replace a stated architecture decision with
a different framework, flow, or identity model.

## Intent Integrity Chain (IIC)

Use the gated, test-first workflow from `doc/intent-integrity-chain.md` for
substantive behavior changes:

1. Analyze intent, boundaries, risks, and acceptance criteria; obtain the
   required Phase 1 approval before proceeding.
2. Write human-readable, testable specifications and a test hierarchy; obtain
   Phase 2 approval.
3. Construct and run executable tests before implementation. Confirm they fail
   for the intended missing behavior, then stop for human review and test lock
   (Phase 3).
4. After approval, implement only the minimum code needed to pass locked tests.
   Do not alter locked tests; a required test/specification change reopens the
   relevant earlier phase.
5. Harden with security, reliability, lint, dependency, and integration checks.

Maintain scenario → test → code traceability. At phase completion, require a
clean working tree, a commit, and a tag in the form
`phase-{n}-{name}-{status}`. Treat a missing/out-of-order tag or uncommitted
phase output as a gate failure. Do not claim a phase is complete without its
human approval where IIC requires one.

## Repository architecture

The modules are independent; do not introduce shared runtime code, framework
wiring, or a different authentication system between them.

- `backend-api/`: Java 25, Spring Boot 4.1.1 servlet/MVC REST API and OAuth2
  Resource Server.
- `frontend-vue/`: Vue 3.5, TypeScript, Vite, Vue Router, and Pinia public SPA.
- `m2m-client/`: Java 25, Spring Boot 4.1.1 OAuth2 Client using Client
  Credentials.
- `docker/keycloak/realm-export.json` and `compose.yaml`: local Keycloak
  infrastructure and identity configuration.

Run commands from the applicable module:

```bash
cd backend-api && ./mvnw test
cd m2m-client && ./mvnw test
cd frontend-vue && npm run type-check && npm run build
```

Use the module wrapper rather than assuming a system Maven installation. Run
the smallest relevant checks first, then appropriate wider checks for shared
behavior.

## Non-negotiable security rules

- Keycloak is the OAuth2/OIDC Authorization Server; the API is the authoritative
  security boundary and remains stateless.
- U2M uses Authorization Code with PKCE S256. Never use Implicit or password
  grants, or a client secret in the Vue application.
- M2M uses Client Credentials. Its secret is externally supplied, never
  committed, hardcoded, or logged.
- The backend accepts bearer access tokens and validates signature, issuer,
  expiration, and audience (`project-api`). Do not use deprecated Keycloak
  Spring adapters or create a second identity system.
- Keycloak client roles map only through a dedicated converter to `ROLE_*`;
  OAuth2 scopes map to `SCOPE_*`. Unknown or malformed claims fail safely.
- Human roles and machine scopes remain distinct. Project operations may allow
  their explicitly corresponding role *or* scope; `/api/me` and admin endpoints
  are human-only unless the architecture is explicitly changed.
- Never log or expose access tokens, refresh tokens, client secrets, passwords,
  signing details, or stack traces. Return safe 401/403 responses.
- Frontend guards and role-aware rendering are UX only. Backend endpoint and
  service-level authorization enforce the policy.
- Keep CORS origins, methods, and headers explicit; do not use permissive
  production wildcards.

## Change discipline

Keep controllers and UI components thin, isolate identity-provider-specific
claim handling, externalize configuration, and favor small reversible changes.
Update learner-facing documentation when a behavior, setup step, endpoint, or
security rationale changes. Preserve unrelated user changes in a dirty tree.
