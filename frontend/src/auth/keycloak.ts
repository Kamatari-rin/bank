import Keycloak, {type KeycloakLoginOptions } from 'keycloak-js'

export const keycloak = new Keycloak({
    url: 'http://localhost:8082',
    realm: 'bank',
    clientId: 'frontend'
})

let initPromise: Promise<boolean> | null = null

export function initKeycloakOnce(): Promise<boolean> {
    if (initPromise) return initPromise
    initPromise = keycloak.init({
        // без onLoad, чтобы не было авто-редиректа
        pkceMethod: 'S256',
        flow: 'standard',
        silentCheckSsoRedirectUri: window.location.origin + '/silent-check-sso.html',
        checkLoginIframe: false,
        silentCheckSsoFallback: false
    })
    keycloak.onTokenExpired = async () => { try { await keycloak.updateToken(30) } catch {} }
    return initPromise
}

/** Признаки, что мы вернулись с KC: в query или в hash */
function isKeycloakCallback(): boolean {
    const qs = new URLSearchParams(window.location.search)
    if (qs.has('code') && qs.has('state')) return true
    const hash = window.location.hash?.replace(/^#/, '') ?? ''
    if (!hash) return false
    const hs = new URLSearchParams(hash)
    // KC иногда кладёт state/session_state/… во фрагмент
    return hs.has('code') || hs.has('state') || hs.has('access_token') || hs.has('session_state')
}

/** Обработать ответ KC, если он есть (query или hash) */
export async function processLoginResponseIfPresent(): Promise<boolean> {
    if (!isKeycloakCallback()) return false
    await initKeycloakOnce() // keycloak-js сам распарсит query/hash и установит токены
    // подчистим URL (уберём ?… и #… без перезагрузки)
    window.history.replaceState({}, document.title, window.location.origin + '/login')
    return true
}

/** Логин с явным редиректом обратно на /login (где мы ждём колбэк) */
export const kcLogin = async () => {
    await initKeycloakOnce()
    const opts: KeycloakLoginOptions = {
        redirectUri: window.location.origin + '/login',
        prompt: 'login'
    }
    return keycloak.login(opts)
}

/** Логин с подсказкой логина (после регистрации) */
export const kcLoginWithHint = async (loginHint: string) => {
    await initKeycloakOnce()
    const opts: KeycloakLoginOptions = {
        redirectUri: window.location.origin + '/login',
        prompt: 'login',
        loginHint
    }
    return keycloak.login(opts)
}

export const kcLogout = () => keycloak.logout({ redirectUri: window.location.origin })
export const getToken = () => keycloak.token ?? undefined
export const getClaims = () => (keycloak.tokenParsed ?? {}) as Record<string, unknown>
export async function ensureToken() {
    if (!keycloak.token) return
    try { await keycloak.updateToken(30) } catch {}
}
