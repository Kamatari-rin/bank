import { useQuery } from '@tanstack/react-query'
import { getRates } from '../api/exchange'

export default function RatesBlock() {
    const { data, isLoading, error } = useQuery({
        queryKey: ['rates'],
        queryFn: getRates,
        refetchInterval: 2000
    })

    const rows = data
        ? Object.values(data).filter((r: any) => r.currency !== 'RUB')
        : []

    return (
        <div className="card">
            <h2 className="text-lg font-medium mb-2">Курсы валют (к RUB)</h2>
            {isLoading && <div className="opacity-70">Загрузка…</div>}
            {error && <div className="text-red-400">Ошибка: {(error as any).message}</div>}
            {rows.length > 0 && (
                <table className="w-full text-sm">
                    <thead className="opacity-70">
                    <tr>
                        <th className="text-left py-1">Пара</th>
                        <th className="text-left py-1">Покупка</th>
                        <th className="text-left py-1">Продажа</th>
                    </tr>
                    </thead>
                    <tbody>
                    {rows.map((r: any) => (
                        <tr key={r.currency}>
                            <td className="py-1">{r.currency}/RUB</td>
                            <td className="py-1">{r.buy}</td>
                            <td className="py-1">{r.sell}</td>
                        </tr>
                    ))}
                    </tbody>
                </table>
            )}
            {rows.length === 0 && !isLoading && !error && (
                <div className="opacity-70">Данных пока нет</div>
            )}
        </div>
    )
}
