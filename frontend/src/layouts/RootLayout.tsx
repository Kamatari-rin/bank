import { Outlet, Link, useLocation } from 'react-router-dom'
import { useAuth } from '../store/auth'
import { kcLogin, kcLogout } from '../auth/keycloak'

export default function RootLayout() {
    const { authenticated, username } = useAuth()
    const loc = useLocation()
    const isHome = loc.pathname === '/'

    return (
        <div className="container">
            <header className="flex items-center justify-between mb-4">
                <nav className="flex items-center gap-3">
                    <Link to="/" className="link">Главная</Link>
                    {!authenticated && <Link to="/register" className="link">Регистрация</Link>}
                </nav>
                <div className="flex items-center gap-3">
                    {authenticated ? (
                        <>
                            <div className="opacity-80">{username}</div>
                            <button onClick={() => kcLogout()}>Выйти</button>
                        </>
                    ) : (
                        !isHome && <button onClick={() => kcLogin()}>Войти</button>
                    )}
                </div>
            </header>
            <Outlet />
        </div>
    )
}
