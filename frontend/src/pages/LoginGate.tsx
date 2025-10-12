import { useEffect } from 'react'
import { Link, useLocation, useNavigate } from 'react-router-dom'
import { kcLogin, processLoginResponseIfPresent, getClaims } from '../auth/keycloak'
import { useAuth } from '../store/auth'

export default function LoginGate() {
    const navigate = useNavigate()
    const location = useLocation()
    const setAuthenticated = useAuth(s => s.setAuthenticated)
    const refreshFromClaims = useAuth(s => s.refreshFromClaims)

    useEffect(() => {
        (async () => {
            const handled = await processLoginResponseIfPresent()
            if (handled) {
                const has = !!getClaims()
                setAuthenticated(has)
                refreshFromClaims()
                navigate('/', { replace: true })
            }
        })()
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [location.key])

    return (
        <div className="card">
            <h1 className="text-xl font-semibold mb-2">Вход в систему</h1>
            <p className="mb-4 opacity-80">У вас уже есть аккаунт? Выполните вход через Keycloak.</p>
            <div className="flex gap-2">
                <button onClick={() => kcLogin()}>Войти</button>
                <Link to="/register" className="link">Зарегистрироваться</Link>
            </div>
        </div>
    )
}
