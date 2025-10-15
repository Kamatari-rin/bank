import { useState } from 'react'
import { z } from 'zod'
import { zodResolver } from '@hookform/resolvers/zod'
import { useForm } from 'react-hook-form'
import { registerUser } from '../api/accounts'
import { kcLoginWithHint } from '../auth/keycloak' // ← добавить

const schema = z.object({
    username:  z.string().min(3, 'Минимум 3 символа'),
    password:  z.string().min(6, 'Минимум 6 символов'),
    firstName: z.string().min(1, 'Обязательно'),
    lastName:  z.string().min(1, 'Обязательно'),
    email:     z.string().email('Некорректный email'),
    birthDate: z.string().regex(/^\d{4}-\d{2}-\d{2}$/, 'Формат YYYY-MM-DD')
})
type FormData = z.infer<typeof schema>

export default function Register() {
    const [ok, setOk] = useState<string | null>(null)
    const [err, setErr] = useState<string | null>(null)

    const { register, handleSubmit, formState: { errors, isSubmitting } } =
        useForm<FormData>({ resolver: zodResolver(schema) })

    const onSubmit = async (data: FormData) => {
        setOk(null); setErr(null)
        try {
            await registerUser(data)
            setOk(`Пользователь создан: ${data.email}`)
            // Автовход: откроем Keycloak со вставленным логином
            await kcLoginWithHint(data.username)
        } catch (e: any) {
            const msg = e?.response?.data?.message ?? e?.message ?? 'Ошибка'
            setErr(`Ошибка: ${msg}`)
        }
    }

    return (
        <div className="card space-y-4">
            <h1 className="text-2xl font-semibold">Регистрация</h1>
            <form className="space-y-4" onSubmit={handleSubmit(onSubmit)}>
                <div className="grid gap-3">
                    <label className="field">
                        <span className="field-label">Логин</span>
                        <input {...register('username')} autoComplete="username" />
                        {errors.username && <span className="field-error">{errors.username.message}</span>}
                    </label>
                    <label className="field">
                        <span className="field-label">Пароль</span>
                        <input type="password" {...register('password')} autoComplete="new-password" />
                        {errors.password && <span className="field-error">{errors.password.message}</span>}
                    </label>
                    <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
                        <label className="field">
                            <span className="field-label">Имя</span>
                            <input {...register('firstName')} />
                            {errors.firstName && <span className="field-error">{errors.firstName.message}</span>}
                        </label>
                        <label className="field">
                            <span className="field-label">Фамилия</span>
                            <input {...register('lastName')} />
                            {errors.lastName && <span className="field-error">{errors.lastName.message}</span>}
                        </label>
                    </div>
                    <label className="field">
                        <span className="field-label">Email</span>
                        <input type="email" {...register('email')} autoComplete="email" />
                        {errors.email && <span className="field-error">{errors.email.message}</span>}
                    </label>
                    <label className="field">
                        <span className="field-label">Дата рождения</span>
                        <input type="date" {...register('birthDate')} />
                        <span className="field-hint">Формат: YYYY-MM-DD (выберите в календаре)</span>
                        {errors.birthDate && <span className="field-error">{errors.birthDate.message}</span>}
                    </label>
                </div>
                <div className="flex gap-2">
                    <button disabled={isSubmitting}>
                        {isSubmitting ? 'Отправка…' : 'Зарегистрироваться'}
                    </button>
                </div>
            </form>
            {ok  && <div className="text-green-400">{ok}</div>}
            {err && <div className="text-red-400">{err}</div>}
        </div>
    )
}
