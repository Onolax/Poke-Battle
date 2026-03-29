import { useState, type FormEvent } from 'react'
import { Link, Navigate } from 'react-router-dom'
import { useAuthStore } from '@/stores/authStore'
import { useRegisterMutation, extractErrorMessage } from '@/hooks/useAuth'

export function RegisterPage() {
  const token = useAuthStore((s) => s.token)
  const registerMutation = useRegisterMutation()

  const [username, setUsername] = useState('')
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')

  if (token) return <Navigate to="/pokemon" replace />

  function handleSubmit(e: FormEvent) {
    e.preventDefault()
    registerMutation.mutate({ username, email, password })
  }

  return (
    <div className="min-h-[80vh] flex items-center justify-center px-4">
      <div className="w-full max-w-sm">
        <h1 className="text-2xl font-bold text-zinc-100 mb-1">Create account</h1>
        <p className="text-zinc-500 text-sm mb-8">Join PokeBattle and start competing</p>

        <form onSubmit={handleSubmit} className="space-y-4">
          <div>
            <label className="block text-sm text-zinc-400 mb-1.5">Username</label>
            <input
              type="text"
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              required
              autoFocus
              className="w-full bg-elevated border border-zinc-700 rounded px-3 py-2 text-zinc-100 text-sm placeholder-zinc-600 focus:outline-none focus:border-accent focus:ring-1 focus:ring-accent transition-colors"
              placeholder="ash_ketchum"
            />
          </div>

          <div>
            <label className="block text-sm text-zinc-400 mb-1.5">Email</label>
            <input
              type="email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              required
              className="w-full bg-elevated border border-zinc-700 rounded px-3 py-2 text-zinc-100 text-sm placeholder-zinc-600 focus:outline-none focus:border-accent focus:ring-1 focus:ring-accent transition-colors"
              placeholder="ash@pallet.town"
            />
          </div>

          <div>
            <label className="block text-sm text-zinc-400 mb-1.5">Password</label>
            <input
              type="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              required
              minLength={6}
              className="w-full bg-elevated border border-zinc-700 rounded px-3 py-2 text-zinc-100 text-sm placeholder-zinc-600 focus:outline-none focus:border-accent focus:ring-1 focus:ring-accent transition-colors"
              placeholder="••••••••"
            />
          </div>

          {registerMutation.isError && (
            <p className="text-accent text-sm bg-accent-dim border border-red-900 rounded px-3 py-2">
              {extractErrorMessage(registerMutation.error)}
            </p>
          )}

          <button
            type="submit"
            disabled={registerMutation.isPending}
            className="w-full bg-accent hover:bg-accent-hover disabled:opacity-50 disabled:cursor-not-allowed text-white font-medium py-2 rounded transition-colors"
          >
            {registerMutation.isPending ? 'Creating account…' : 'Create account'}
          </button>
        </form>

        <p className="text-zinc-500 text-sm mt-6 text-center">
          Already have an account?{' '}
          <Link to="/login" className="text-accent hover:text-red-400 transition-colors">
            Log in
          </Link>
        </p>
      </div>
    </div>
  )
}
