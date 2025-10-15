import { useState } from 'react'
import type { Currency } from '../types'

export default function CreateAccountForm({
                                              onCreated,
                                              creating,
                                              errorMessage
                                          }:{
    onCreated: (currency: Currency) => void
    creating?: boolean
    errorMessage?: string
}) {
    const [currency, setCurrency] = useState<Currency>('RUB')

    const submit = (e: React.FormEvent) => {
        e.preventDefault()
        onCreated(currency)
    }

    return (
        <form className="card space-y-3" onSubmit={submit}>
            <h2 className="text-lg font-medium">Открыть счёт</h2>
            <select value={currency} onChange={(e)=>setCurrency(e.target.value as Currency)}>
                <option value="RUB">RUB</option>
                <option value="USD">USD</option>
                <option value="CNY">CNY</option>
            </select>
            <button type="submit" disabled={creating}>
                {creating ? 'Создаём…' : 'Создать'}
            </button>
            {errorMessage && <div className="text-red-400">{errorMessage}</div>}
        </form>
    )
}
