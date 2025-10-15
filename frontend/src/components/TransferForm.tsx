// src/components/TransferForm.tsx
import { useEffect, useMemo, useState } from 'react'
import { useMutation, useQueryClient } from '@tanstack/react-query'
import type { BankAccountDto, Currency } from '../types'
import { transferOwn, transferExternal } from '../api/transfer'

type PresetTarget = { accountId?: string, currency?: Currency }

export default function TransferForm({
                                         accounts,
                                         presetTarget
                                     }: {
    accounts: BankAccountDto[],
    presetTarget?: PresetTarget
}) {
    const qc = useQueryClient()

    const [mode, setMode] = useState<'own' | 'external'>('own')
    const [fromId, setFromId] = useState(accounts[0]?.id ?? '')
    const [toId, setToId] = useState(accounts[1]?.id ?? '')           // для "own"
    const [extToId, setExtToId] = useState<string>('')                 // для "external"
    const [extToCurrency, setExtToCurrency] = useState<Currency>('RUB')
    const [amount, setAmount] = useState('')

    const byId = useMemo(() => new Map(accounts.map(a => [a.id, a])), [accounts])
    const from = byId.get(fromId)
    const toOwn = byId.get(toId)

    // Если пришёл пресет из RecipientsBrowser — подставим в external‑форму
    useEffect(() => {
        if (presetTarget?.accountId && presetTarget?.currency) {
            setMode('external')
            setExtToId(presetTarget.accountId)
            setExtToCurrency(presetTarget.currency)
        }
    }, [presetTarget?.accountId, presetTarget?.currency])

    const canSubmitOwn =
        mode === 'own' &&
        !!fromId && !!toId && fromId !== toId &&
        Number(amount) > 0

    const canSubmitExternal =
        mode === 'external' &&
        !!fromId && !!extToId &&
        Number(amount) > 0

    const mx = useMutation({
        mutationFn: async () => {
            const amt = Number(amount)
            const fromCur: Currency = (from?.currency ?? 'RUB') as Currency

            if (mode === 'own') {
                const toCur: Currency = (toOwn?.currency ?? 'RUB') as Currency
                return transferOwn({
                    fromAccountId: fromId,
                    fromCurrency: fromCur,
                    toAccountId: toId,
                    toCurrency: toCur,
                    amount: amt
                })
            } else {
                return transferExternal({
                    fromAccountId: fromId,
                    fromCurrency: fromCur,
                    toAccountId: extToId,
                    toCurrency: extToCurrency,
                    amount: amt
                })
            }
        },
        onSuccess: () => {
            setAmount('')
            // обновим профиль/балансы
            qc.invalidateQueries({ queryKey: ['profile'] })
        }
    })

    return (
        <form className="card grid gap-3" onSubmit={(e)=>e.preventDefault()}>
            <div className="flex items-center justify-between">
                <h2 className="text-lg font-medium">Перевод</h2>
                <div className="flex gap-2">
                    <button type="button"
                            className={mode==='own' ? 'bg-neutral-700 px-3 py-2 rounded-xl' : 'px-3 py-2 rounded-xl'}
                            onClick={()=>setMode('own')}>
                        Между своими
                    </button>
                    <button type="button"
                            className={mode==='external' ? 'bg-neutral-700 px-3 py-2 rounded-xl' : 'px-3 py-2 rounded-xl'}
                            onClick={()=>setMode('external')}>
                        Другому пользователю
                    </button>
                </div>
            </div>

            <div className="grid sm:grid-cols-2 gap-3">
                <label className="field">
                    <span className="field-label">Со счёта</span>
                    <select value={fromId} onChange={(e)=>setFromId(e.target.value)}>
                        {accounts.map(a=>(
                            <option key={a.id} value={a.id}>
                                {a.currency} · {a.balance}
                            </option>
                        ))}
                    </select>
                </label>

                {mode === 'own' ? (
                    <label className="field">
                        <span className="field-label">На счёт (мой)</span>
                        <select value={toId} onChange={(e)=>setToId(e.target.value)}>
                            {accounts.map(a=>(
                                <option key={a.id} value={a.id} disabled={a.id===fromId}>
                                    {a.currency} · {a.balance}
                                </option>
                            ))}
                        </select>
                    </label>
                ) : (
                    <div className="grid gap-1.5">
                        <div className="text-sm text-neutral-300">Счёт получателя</div>
                        {extToId ? (
                            <div className="list-item">
                                <div className="mono">{extToCurrency}</div>
                                <div className="opacity-70 text-xs">{extToId}</div>
                                <button type="button" onClick={() => { setExtToId('') }}>
                                    Очистить выбор
                                </button>
                            </div>
                        ) : (
                            <div className="opacity-70 text-sm">
                                Выберите получателя в блоке «Поиск получателей»
                            </div>
                        )}
                    </div>
                )}
            </div>

            <label className="field">
                <span className="field-label">Сумма (в валюте отправителя)</span>
                <input type="number" min="0" step="0.01"
                       value={amount}
                       onChange={(e)=>setAmount(e.target.value)} />
            </label>

            <div className="flex gap-2">
                <button disabled={mx.isPending || !(canSubmitOwn || canSubmitExternal)}
                        onClick={()=>mx.mutate()}>
                    {mx.isPending ? 'Переводим…' : 'Перевести'}
                </button>
            </div>

            {mx.isError && (
                <div className="text-red-400">
                    Ошибка: {(mx.error as any)?.response?.data?.message ?? (mx.error as any)?.message}
                </div>
            )}
        </form>
    )
}
