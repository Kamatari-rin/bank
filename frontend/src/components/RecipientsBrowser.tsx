// src/components/RecipientsBrowser.tsx
import { useState } from 'react'
import { useQuery } from '@tanstack/react-query'
import { fetchUsers, fetchUserAccounts } from '../api/accounts'
import type {Currency, PublicAccountDto, PublicUserDto} from '../types'

export default function RecipientsBrowser({
                                              onPick
                                          }: { onPick: (accountId: string, currency: Currency) => void }) {

    const [open, setOpen] = useState(false)
    const [expanded, setExpanded] = useState<string | null>(null)

    const { data: users, isFetching, refetch, error } = useQuery({
        queryKey: ['public-users'],
        queryFn: fetchUsers,
        enabled: false // загружаем по кнопке
    })

    const [accountsCache, setAccountsCache] = useState<Record<string, PublicAccountDto[]>>({})

    const toggleOpen = async () => {
        if (!open && !users) await refetch()
        setOpen(o => !o)
    }

    const loadAccounts = async (u: PublicUserDto) => {
        if (accountsCache[u.id]) {
            setExpanded(prev => prev === u.id ? null : u.id)
            return
        }
        const list = await fetchUserAccounts(u.id)
        setAccountsCache(prev => ({ ...prev, [u.id]: list }))
        setExpanded(u.id)
    }

    return (
        <div className="card">
            <div className="flex items-center justify-between">
                <h2 className="text-lg font-medium">Поиск получателей</h2>
                <button onClick={toggleOpen}>
                    {open ? 'Скрыть' : 'Показать получателей'}
                </button>
            </div>

            {open && (
                <div className="mt-4">
                    {isFetching && <div className="opacity-70">Загрузка…</div>}
                    {error && <div className="text-red-400">Ошибка загрузки</div>}

                    {users && users.length === 0 && (
                        <div className="opacity-70">Пользователей нет</div>
                    )}

                    {users && users.map(u => (
                        <div key={u.id} className="border border-neutral-800 rounded-xl p-3 mb-3">
                            <div className="flex items-center justify-between">
                                <div>
                                    <div className="font-medium">{u.firstName} {u.lastName}</div>
                                    <div className="text-sm opacity-70">{u.email}</div>
                                </div>
                                <button onClick={()=>loadAccounts(u)}>
                                    {expanded === u.id ? 'Свернуть счета' : 'Показать счета'}
                                </button>
                            </div>

                            {expanded === u.id && (
                                <div className="mt-3 grid gap-2">
                                    {(accountsCache[u.id] ?? []).map(a => (
                                        <div key={a.id} className="list-item">
                                            <div className="mono">{a.currency}</div>
                                            <div className="opacity-70 text-xs">{a.id}</div>
                                            <button onClick={() => onPick(a.id, a.currency as Currency)}>
                                                Выбрать
                                            </button>
                                        </div>
                                    ))}
                                    {(accountsCache[u.id] ?? []).length === 0 && (
                                        <div className="opacity-70">Счета отсутствуют</div>
                                    )}
                                </div>
                            )}
                        </div>
                    ))}
                </div>
            )}
        </div>
    )
}
