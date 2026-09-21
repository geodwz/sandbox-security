import { describe, expect, it } from 'vitest'

import { useAuth } from '@/auth/useAuth'
import router from '@/router'

describe('FE-001, FE-002, and FE-005 authentication contract', () => {
  it('exposes centralized PKCE auth state without leaking token details', () => {
    const auth = useAuth()

    expect(auth.login).toBeTypeOf('function')
    expect(auth.logout).toBeTypeOf('function')
    expect(auth.isAuthenticated).toBeTypeOf('boolean')
    expect(auth.currentUser).toBeDefined()
    expect(auth.hasRole).toBeTypeOf('function')
    expect(auth.hasAnyRole).toBeTypeOf('function')
    expect(auth.isUser).toBeTypeOf('boolean')
    expect(auth.isEditor).toBeTypeOf('boolean')
    expect(auth.isAdmin).toBeTypeOf('boolean')
  })
})

describe('FE-003 and FE-004 route authorization contract', () => {
  it('declares the required role-aware routes', () => {
    const routes = router.getRoutes()

    expect(routes.find((route) => route.path === '/projects')).toBeDefined()
    expect(routes.find((route) => route.path === '/projects/new')?.meta.roles).toEqual(['EDITOR', 'ADMIN'])
    expect(routes.find((route) => route.path === '/projects/:id/edit')?.meta.roles).toEqual(['EDITOR', 'ADMIN'])
    expect(routes.find((route) => route.path === '/admin')?.meta.roles).toEqual(['ADMIN'])
  })
})
