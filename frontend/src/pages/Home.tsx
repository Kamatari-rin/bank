import AccountList from '../components/AccountList'
import CreateAccountForm from '../components/CreateAccountForm'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { fetchProfile, createAccount, deleteAccount } from '../api/accounts'
import type { Currency } from '../types'
import CashForm from '../components/CashForm'
import TransferForm from '../components/TransferForm'
import RatesBlock from "../components/RatesBlock.tsx";
import RecipientsBrowser from "../components/RecipientsBrowser.tsx";
import {useState} from "react";
import NotificationsBlock from '../components/NotificationsBlock'
export default function Home() {
    const qc = useQueryClient()
    const { data, isLoading, error } = useQuery({
        queryKey: ['profile'],
        queryFn: fetchProfile
    })

    const mk = useMutation({
        mutationFn: (c: Currency) => createAccount(c),
        onSuccess: () => qc.invalidateQueries({ queryKey: ['profile'] })
    })

    const del = useMutation({
        mutationFn: (id: string) => deleteAccount(id),
        onSuccess: () => qc.invalidateQueries({ queryKey: ['profile'] })
    })

    const [toAccountId, setToAccountId] = useState<string | null>(null)
    const [toCurrency, setToCurrency]   = useState<Currency | null>(null)

    return (
        <>
            <div className="card">
                <h1 className="text-xl font-semibold">Добро пожаловать</h1>
                {isLoading && <div className="opacity-70">Загрузка…</div>}
                {error && <div className="text-red-400">Ошибка загрузки</div>}
                {data && (
                    <div className="opacity-80">
                        {data.user.firstName} {data.user.lastName} · {data.user.email}
                    </div>
                )}
            </div>
            <RecipientsBrowser
                onPick={(accId, cur) => {
                    setToAccountId(accId)
                    setToCurrency(cur)
                    // при желании — скролл к форме перевода и фокус на сумму
                }}
            />
            <NotificationsBlock />
            <RatesBlock />
            {/* Форма пополнения/снятия */}
            <CashForm accounts={data?.accounts ?? []} />
            <TransferForm
                accounts={data?.accounts ?? []}
                presetTarget={{ accountId: toAccountId ?? undefined, currency: toCurrency ?? undefined }}
            />
            <CreateAccountForm
                onCreated={(c) => mk.mutate(c)}
                creating={mk.isPending}
                errorMessage={mk.isError ? (mk.error as any)?.response?.data?.message : undefined}
            />

            <AccountList
                items={data?.accounts ?? []}
                onDelete={(id) => del.mutate(id)}
            />
        </>
    )
}
