import type { Team } from '@/types'
import { useDeleteTeam } from '@/hooks/useTeams'

interface Props {
  team: Team
}

export function TeamCard({ team }: Props) {
  const deleteTeam = useDeleteTeam()

  return (
    <div className="bg-surface border border-zinc-800 rounded-lg p-4 flex items-center justify-between gap-4">
      <div className="flex-1 min-w-0">
        <div className="flex items-center gap-2 mb-1">
          <span className="text-zinc-100 font-medium truncate">{team.name}</span>
          {team.validated && (
            <span className="text-green-400 text-xs bg-green-950 border border-green-900 rounded px-1.5 py-0.5">
              ✓ Valid
            </span>
          )}
          <span className="text-zinc-600 text-xs bg-elevated rounded px-1.5 py-0.5 uppercase tracking-wide">
            {team.format}
          </span>
        </div>
        <div className="flex gap-1.5 flex-wrap">
          {team.pokemonSlugs.map((slug) => (
            <span
              key={slug}
              className="text-zinc-400 text-xs bg-elevated rounded px-2 py-0.5 capitalize"
            >
              {slug}
            </span>
          ))}
        </div>
      </div>

      <button
        onClick={() => deleteTeam.mutate(team.id)}
        disabled={deleteTeam.isPending}
        className="text-zinc-600 hover:text-accent transition-colors text-sm px-2 py-1 rounded hover:bg-elevated shrink-0"
        title="Delete team"
      >
        {deleteTeam.isPending ? '…' : '✕'}
      </button>
    </div>
  )
}
