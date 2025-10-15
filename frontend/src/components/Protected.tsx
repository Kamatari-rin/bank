import { useEffect } from 'react'
import type { ReactNode } from 'react'
import { useAuth } from '../store/auth'
import { useNavigate } from 'react-router-dom'

export default function Protected({ children }: { children: ReactNode }) {
    const authenticated = useAuth(s => s.authenticated)
    const navigate = useNavigate()

    useEffect(() => {
        if (!authenticated) navigate('/login', { replace: true })
    }, [authenticated, navigate])

    if (!authenticated) return null
    return <>{children}</>
}