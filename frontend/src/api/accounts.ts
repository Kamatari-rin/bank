import { api } from './http'
import type {UserProfileDto, BankAccountDto, Currency, PublicUserDto, PublicAccountDto} from '../types'

export async function fetchProfile(): Promise<UserProfileDto> {
    const { data } = await api.get('/api/accounts/me')
    return data
}

export async function createAccount(currency: Currency): Promise<BankAccountDto> {
    const { data } = await api.post('/api/accounts/me/accounts', { currency })
    return data
}

export async function deleteAccount(id: string): Promise<void> {
    await api.delete(`/api/accounts/me/accounts/${id}`)
}

export async function registerUser(payload: {
    username: string
    password: string
    firstName: string
    lastName: string
    email: string
    birthDate: string
}) {
    // регистрация публичная — токен не нужен; но общий api всё равно ок
    await api.post('/api/accounts/register', payload)
}
export async function fetchUsers(): Promise<PublicUserDto[]> {
    const { data } = await api.get('/api/accounts/public/users')
    return data
}

export async function fetchUserAccounts(userId: string): Promise<PublicAccountDto[]> {
    const { data } = await api.get(`/api/accounts/public/users/${userId}/accounts`)
    return data
}