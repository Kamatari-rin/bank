import { api } from './http'

export async function deposit(accountId: string, amount: number, idempotencyKey?: string) {
    const { data } = await api.post('/api/cash/deposit', { accountId, amount, idempotencyKey })
    return data as { accountId: string; newBalance: string }
}

export async function withdraw(accountId: string, amount: number, idempotencyKey?: string) {
    const { data } = await api.post('/api/cash/withdraw', { accountId, amount, idempotencyKey })
    return data as { accountId: string; newBalance: string }
}
