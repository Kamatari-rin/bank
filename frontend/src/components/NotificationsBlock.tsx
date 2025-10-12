
import { useQuery } from '@tanstack/react-query'
import { api } from '../api/http'

type Notification = { id: string; ts: string; title: string; message: string }

export default function NotificationsBlock() {
    const { data, isLoading, error } = useQuery({
        queryKey: ['notifications'],
        queryFn: async (): Promise<Notification[]> => {
            const { data } = await api.get('/api/notifications/public/mine')
            return data
        },
        refetchInterval: 5000
    })

    return (
        <div className="card">
            <h2 className="text-lg font-medium mb-2">Уведомления</h2>
            {isLoading && <div className="opacity-70">Загрузка…</div>}
            {error && <div className="text-red-400">Ошибка загрузки</div>}
            <ul className="space-y-2">
                {(data ?? []).map(n => (
                    <li key={n.id} className="list-item">
                        <div>
                            <div className="font-medium">{n.title}</div>
                            <div className="opacity-80 text-sm">{n.message}</div>
                        </div>
                        <div className="text-xs opacity-60">{new Date(n.ts).toLocaleString()}</div>
                    </li>
                ))}
                {(!data || data.length === 0) && <div className="opacity-60">Пока пусто</div>}
            </ul>
        </div>
    )
}
