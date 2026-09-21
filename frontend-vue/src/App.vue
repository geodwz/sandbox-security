<script setup lang="ts">
import { RouterLink, RouterView } from 'vue-router'
import { useAuth } from '@/auth/useAuth'
const auth = useAuth()
</script>

<template>
  <header>
    <RouterLink to="/projects">Project API Security Tutorial</RouterLink>
    <nav v-if="auth.isAuthenticated"><RouterLink to="/projects">Projects</RouterLink><RouterLink v-if="auth.isAdmin" to="/admin">Admin</RouterLink></nav>
    <div>
      <template v-if="auth.isAuthenticated"><span>{{ auth.currentUser?.username }} ({{ auth.roles.join(', ') }})</span><button @click="auth.logout">Log out</button></template>
      <button v-else @click="auth.login">Log in with Keycloak</button>
    </div>
  </header>
  <RouterView />
</template>

<style scoped>header { display:flex; gap:1rem; padding:1rem; border-bottom:1px solid var(--color-border); } header > :first-child { margin-right:auto; font-weight:700; } nav, div { display:flex; gap:.75rem; }</style>
