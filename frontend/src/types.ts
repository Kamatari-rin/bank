export type Currency = 'RUB' | 'USD' | 'CNY'

export type UserDto = {
    id: string
    keycloakId: string
    firstName: string
    lastName: string
    email: string
    birthDate: string
    createdAt?: string | null
    updatedAt?: string | null
}

export type BankAccountDto = {
    id: string
    userId: string
    currency: Currency
    balance: string
    createdAt?: string | null
    updatedAt?: string | null
}

export type UserProfileDto = {
    user: UserDto
    accounts: BankAccountDto[]
}
export type PublicUserDto = {
    id: string
    keycloakId: string
    firstName: string
    lastName: string
    email: string
}
export type PublicAccountDto = {
    id: string
    currency: 'RUB' | 'USD' | 'CNY'
}