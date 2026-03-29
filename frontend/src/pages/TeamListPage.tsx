import { Link } from 'react-router-dom'
import { useTeams } from '@/hooks/useTeams'
import { TeamCard } from '@/components/team/TeamCard'

export function TeamListPage() {
  const { data: teams, isLoading, isError } = useTeams()

  return (
    <div className="max-w-3xl mx-auto px-4 py-8">
      <div className="flex items-center justify-between mb-6">
        <h1 className="text-2xl font-bold text-zinc-100">My Teams</h1>
        <Link
          to="/teams/new"
          className="bg-accent hover:bg-accent-hover text-white text-sm font-medium px-4 py-2 rounded transition-colors"
        >
          + New Team
        </Link>
      </div>

      {isLoading && <p className="text-zinc-500 text-sm">Loading teams…</p>}
      {isError && <p className="text-accent text-sm">Failed to load teams.</p>}

      {!isLoading && !isError && teams?.length === 0 && (
        <div className="text-center py-16 text-zinc-600">
          <p className="text-lg mb-2">No teams yet</p>
          <p className="text-sm">
            <Link to="/teams/new" className="text-accent hover:text-red-400">
              Build your first team
            </Link>
          </p>
        </div>
      )}

      <div className="space-y-3">
        {teams?.map((team) => (
          <TeamCard key={team.id} team={team} />
        ))}
      </div>
    </div>
  )
}
