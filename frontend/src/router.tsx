import { createBrowserRouter } from 'react-router-dom'
import RootLayout from './layouts/RootLayout'
import Home from './pages/Home'
import Register from './pages/Register'
import LoginGate from './pages/LoginGate'
import Protected from './components/Protected'

export const router = createBrowserRouter([
    {
        path: '/',
        element: <RootLayout />,
        children: [
            { index: true, element: <Protected><Home /></Protected> },
            { path: 'register', element: <Register /> },
            { path: 'login', element: <LoginGate /> }
        ]
    }
])
