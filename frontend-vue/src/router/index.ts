import { createMemoryHistory, createRouter, createWebHistory } from 'vue-router'
import HomeView from '../views/HomeView.vue'

const router = createRouter({
  history: typeof window === 'undefined' ? createMemoryHistory() : createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/projects',
      name: 'projects',
      component: HomeView,
      meta: { roles: ['USER', 'EDITOR', 'ADMIN'] },
    },
    {
      path: '/projects/new', name: 'project-new', component: HomeView, meta: { roles: ['EDITOR', 'ADMIN'] },
    },
    { path: '/projects/:id/edit', name: 'project-edit', component: HomeView, meta: { roles: ['EDITOR', 'ADMIN'] } },
    { path: '/admin', name: 'admin', component: HomeView, meta: { roles: ['ADMIN'] } },
  ],
})

export default router
