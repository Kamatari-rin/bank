import { useMemo, useState } from 'react'
import { useMutation, useQueryClient } from '@tanstack/react-query'
import type { BankAccountDto } from '../types'
import * as cash from '../api/cash'

type Props = {
    accounts: BankAccountDto[]
}

export default function CashForm({ accounts }: Props) {
    const qc = useQueryClient()

    const firstId = useMemo(() => accounts[0]?.id ?? '', [accounts])
    const [accountId, setAccountId] = useState<string>(firstId)
    const [amount, setAmount] = useState<string>('')

    const canSubmit = accounts.length > 0 && accountId && Number(amount) > 0

    const onSuccess = () => {
        setAmount('')
        // обновить профиль (балансы)
        qc.invalidateQueries({ queryKey: ['profile'] })
    }

    const depositMx = useMutation({
        mutationFn: () => cash.deposit(accountId, Number(amount)),
        onSuccess
    })

    const withdrawMx = useMutation({
        mutationFn: () => cash.withdraw(accountId, Number(amount)),
        onSuccess
    })

    const pending = depositMx.isPending || withdrawMx.isPending
    const errorMsg =
        (depositMx.isError && (depositMx.error as any)?.response?.data?.message) ||
        (withdrawMx.isError && (withdrawMx.error as any)?.response?.data?.message) ||
        undefined

    return (
        <form className="card grid gap-3" onSubmit={(e) => e.preventDefault()}>
            <h2 className="text-lg font-medium">Внесение / Снятие</h2>

            <label className="field">
                <span className="field-label">Счёт</span>
                <select
                    value={accountId}
                    onChange={(e) => setAccountId(e.target.value)}
                    disabled={accounts.length === 0}
                >
                    {accounts.map(a => (
                        <option key={a.id} value={a.id}>
                            {a.currency} · баланс {a.balance}
                        </option>
                    ))}
                </select>
                {accounts.length === 0 && (
                    <span className="field-hint">Сначала создайте хотя бы один счёт</span>
                )}
            </label>

            <label className="field">
                <span className="field-label">Сумма</span>
                <input
                    type="number"
                    step="0.01"
                    min="0"
                    placeholder="0.00"
                    value={amount}
                    onChange={(e) => setAmount(e.target.value)}
                />
            </label>

            <div className="flex gap-2">
                <button
                    onClick={() => depositMx.mutate()}
                    disabled={!canSubmit || pending}
                >
                    {depositMx.isPending ? 'Вносим…' : 'Положить'}
                </button>
                <button
                    onClick={() => withdrawMx.mutate()}
                    disabled={!canSubmit || pending}
                >
                    {withdrawMx.isPending ? 'Снимаем…' : 'Снять'}
                </button>
            </div>

            {errorMsg && <div className="text-red-400">Ошибка: {errorMsg}</div>}
        </form>
    )
}
