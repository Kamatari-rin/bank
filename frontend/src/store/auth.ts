import { create } from 'zustand'
import { getClaims } from '../auth/keycloak'

type AuthState = {
    authenticated: boolean
    username?: string
    setAuthenticated: (v: boolean) => void
    refreshFromClaims: () => void
}

export const useAuth = create<AuthState>((set) => ({
    authenticated: false,
    username: undefined,
    setAuthenticated: (v) => set({ authenticated: v }),
    refreshFromClaims: () => {
        const c = getClaims()
        const username = (c['preferred_username'] || c['email']) as string | undefined
        set({ username })
    }
}))
