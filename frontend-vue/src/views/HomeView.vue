<script setup lang="ts">
import { computed, ref } from 'vue'
import { useAuth } from '@/auth/useAuth'
const auth = useAuth()
const projects = ref([{ id: 1, name: 'Security tutorial', description: 'In-memory Project API example' }])
const name = ref('')
const canEdit = computed(() => auth.isEditor || auth.isAdmin)
function createProject() { if (name.value.trim()) projects.value.push({ id: Date.now(), name: name.value, description: 'Created in the tutorial UI' }); name.value = '' }
</script>

<template><main><h1>Projects</h1><p>Route guards are UX; the Spring API enforces authorization.</p><form v-if="canEdit" @submit.prevent="createProject"><input v-model="name" required placeholder="Project name" /><button>Create project</button></form><ul><li v-for="project in projects" :key="project.id"><strong>{{ project.name }}</strong> — {{ project.description }} <button v-if="auth.isAdmin">Delete</button></li></ul></main></template>
