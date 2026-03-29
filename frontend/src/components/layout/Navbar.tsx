import { Link, useNavigate } from 'react-router-dom'
import { useAuthStore } from '@/stores/authStore'

export function Navbar() {
  const { username, token, logout } = useAuthStore()
  const navigate = useNavigate()

  function handleLogout() {
    logout()
    navigate('/login')
  }

  return (
    <nav className="bg-surface border-b border-zinc-800 px-4 py-3">
      <div className="max-w-6xl mx-auto flex items-center justify-between">
        <Link to="/" className="text-accent font-bold text-xl tracking-tight">
          PokeBattle
        </Link>

        {token ? (
          <div className="flex items-center gap-6">
            <Link to="/pokemon" className="text-zinc-400 hover:text-zinc-100 text-sm transition-colors">
              Pokédex
            </Link>
            <Link to="/teams" className="text-zinc-400 hover:text-zinc-100 text-sm transition-colors">
              Teams
            </Link>
            <Link to="/lobby" className="text-zinc-400 hover:text-zinc-100 text-sm transition-colors">
              Battle
            </Link>
            <Link to="/profile" className="text-zinc-400 hover:text-zinc-100 text-sm transition-colors">
              Profile
            </Link>
            <span className="text-zinc-500 text-sm">{username}</span>
            <button
              onClick={handleLogout}
              className="text-sm bg-elevated hover:bg-zinc-700 text-zinc-300 px-3 py-1.5 rounded transition-colors"
            >
              Logout
            </button>
          </div>
        ) : (
          <div className="flex items-center gap-3">
            <Link
              to="/login"
              className="text-sm text-zinc-400 hover:text-zinc-100 transition-colors"
            >
              Login
            </Link>
            <Link
              to="/register"
              className="text-sm bg-accent hover:bg-accent-hover text-white px-3 py-1.5 rounded transition-colors"
            >
              Register
            </Link>
          </div>
        )}
      </div>
    </nav>
  )
}
