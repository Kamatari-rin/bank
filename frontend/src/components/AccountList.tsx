import type { BankAccountDto } from '../types'

export default function AccountList({ items, onDelete }: {
    items: BankAccountDto[]
    onDelete: (id: string) => void
}) {
    return (
        <div className="card">
            <h2 className="text-lg font-medium mb-2">Мои счета</h2>
            <ul className="space-y-2">
                {items.map(a => (
                    <li key={a.id} className="flex items-center justify-between border border-neutral-800 rounded-xl p-3">
                        <div className="font-mono">{a.currency}</div>
                        <div className="opacity-80">{a.balance}</div>
                        <button onClick={()=>onDelete(a.id)}>Удалить</button>
                    </li>
                ))}
                {items.length === 0 && <div className="opacity-60">Пока пусто</div>}
            </ul>
        </div>
    )
}
