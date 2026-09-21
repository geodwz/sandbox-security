import { computed, reactive } from 'vue'

type CurrentUser = { username: string; roles: string[] } | null

const state = reactive({ currentUser: null as CurrentUser, accessToken: null as string | null })

const config = {
  issuer: import.meta.env.VITE_KEYCLOAK_URL ?? 'http://localhost:8090/auth',
  realm: import.meta.env.VITE_KEYCLOAK_REALM ?? 'tutorial',
  clientId: import.meta.env.VITE_KEYCLOAK_CLIENT_ID ?? 'vue-client',
}

export function useAuth() {
  const roles = computed(() => state.currentUser?.roles ?? [])
  const hasRole = (role: string) => roles.value.includes(role)

  return {
    currentUser: state.currentUser,
    isAuthenticated: Boolean(state.accessToken),
    roles: roles.value,
    isUser: hasRole('USER'),
    isEditor: hasRole('EDITOR'),
    isAdmin: hasRole('ADMIN'),
    hasRole,
    hasAnyRole: (...required: string[]) => required.some(hasRole),
    async login() {
      const verifier = crypto.randomUUID() + crypto.randomUUID()
      const bytes = new TextEncoder().encode(verifier)
      const digest = await crypto.subtle.digest('SHA-256', bytes)
      const challenge = btoa(String.fromCharCode(...new Uint8Array(digest))).replaceAll('+', '-').replaceAll('/', '_').replaceAll('=', '')
      sessionStorage.setItem('pkce_verifier', verifier)
      const authorization = new URL(`${config.issuer}/realms/${config.realm}/protocol/openid-connect/auth`)
      authorization.search = new URLSearchParams({ client_id: config.clientId, response_type: 'code', redirect_uri: location.origin, code_challenge: challenge, code_challenge_method: 'S256', scope: 'openid' }).toString()
      location.assign(authorization.toString())
    },
    logout() {
      state.currentUser = null
      state.accessToken = null
      sessionStorage.removeItem('pkce_verifier')
    },
  }
}
