import { api } from './http'

export async function transferOwn(input: {
    fromAccountId: string; fromCurrency: string;
    toAccountId: string;   toCurrency: string;
    amount: number;
}) {
    const { data } = await api.post('/api/transfer/own', input)
    return data as {
        fromAccountId: string; fromNewBalance: string;
        toAccountId: string;   toNewBalance: string;
        amountFrom: string;    amountTo: string;
    }
}
export async function transferExternal(payload: {
    fromAccountId: string
    fromCurrency: 'RUB'|'USD'|'CNY'
    toAccountId: string       // счёт другого пользователя
    toCurrency: 'RUB'|'USD'|'CNY'
    amount: number
}) {
    const { data } = await api.post('/api/transfer/external', payload)
    return data
}