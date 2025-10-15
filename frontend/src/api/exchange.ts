import { api } from './http'

export type RateRow = { currency: string; buy: string; sell: string }
export async function getRates(): Promise<Record<string, RateRow>> {
    const { data } = await api.get('/api/exchange/rates')
    return data
}
