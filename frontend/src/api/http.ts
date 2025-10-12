import axios from 'axios'
import { ensureToken, getToken, kcLogin } from '../auth/keycloak'

export const api = axios.create({
    baseURL: 'http://localhost:8080',
    headers: { 'Content-Type': 'application/json' }
})

api.interceptors.request.use(async (config) => {
    await ensureToken()
    const t = getToken()
    if (t) {
        config.headers = config.headers ?? {}
        config.headers.Authorization = `Bearer ${t}`
    }
    return config
})

// единоразовый retry на 401
let retrying = false
api.interceptors.response.use(
    r => r,
    async (error) => {
        const status = error?.response?.status
        if (status === 401 && !retrying) {
            retrying = true
            try {
                await ensureToken()
                const t = getToken()
                if (t) {
                    error.config.headers = error.config.headers ?? {}
                    error.config.headers.Authorization = `Bearer ${t}`
                    return api.request(error.config)
                }
            } finally {
                retrying = false
            }
            // если токена нет — отправляем на логин
            kcLogin()
            return
        }
        return Promise.reject(error)
    }
)
