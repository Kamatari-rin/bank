import React from 'react'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { RouterProvider } from 'react-router-dom'
import { router } from './router'
import './index.css'

const qc = new QueryClient()

export default function App() {
    return (
        <React.StrictMode>
            <QueryClientProvider client={qc}>
                <RouterProvider router={router} />
            </QueryClientProvider>
        </React.StrictMode>
    )
}
