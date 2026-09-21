# Frontend Instructions

## Activation

Apply this file to all files under `frontend-vue/`, including `.vue`, `.ts`,
`.tsx`, `package.json`, Vite, router, store, and test/configuration files. Also
follow the repository-root `AGENTS.md`.

## Stack and authentication flow

- Use Vue 3.5, TypeScript, Vite, Vue Router, and Pinia.
- This is a public browser client. Implement OpenID Connect/OAuth2
  Authorization Code Flow with PKCE S256 only.
- Never add a client secret, password-grant flow, or implicit flow to browser
  code, client-side configuration, build variables, examples, or documentation.
- Centralize authentication in `AuthService`/`useAuth()` (or an equivalent
  single abstraction). Components must not parse raw Keycloak claims.

Expose normalized authentication state and role helpers such as `currentUser`,
`isAuthenticated`, `roles`, `hasRole`, `hasAnyRole`, `login`, and `logout`.
Use the backend `GET /api/me` response for the application's normalized user
representation. Send the access token—not the ID token—as a bearer token to the
API.

## Routing and UI authorization

Use route guards and role-aware UI solely for user experience:

- `/projects`: user, editor, or admin
- `/projects/new` and `/projects/:id/edit`: editor or admin
- `/admin`: admin only

Do not treat any frontend check as a security boundary; the backend remains the
authoritative enforcer. Avoid exposing raw token contents in UI, logs, error
messages, state inspection aids, or analytics.

## Testing and verification

Follow IIC: construct approved executable tests before implementation and do
not modify locked tests. Cover PKCE login/logout state, route-guard UX,
role-aware rendering, API bearer-token use, and safe authentication failures.
Run `npm run type-check` and `npm run build` from `frontend-vue/`; run the
smallest relevant test/lint command when the project has it. Avoid auto-fixing
the whole source tree unless that broad rewrite is intentional.
