import type { LeaderboardEntry } from '@/types'

const RANK_STYLE: Record<number, string> = {
  1: 'text-yellow-400 font-bold',
  2: 'text-zinc-300 font-bold',
  3: 'text-amber-600 font-bold',
}

interface Props {
  entries: LeaderboardEntry[]
  currentUserId: string | null
}

export function Leaderboard({ entries, currentUserId }: Props) {
  return (
    <div className="bg-surface border border-zinc-800 rounded-lg overflow-hidden">
      <div className="px-4 py-3 border-b border-zinc-800">
        <p className="text-zinc-200 font-semibold">GEN 9 OU Leaderboard</p>
      </div>
      <table className="w-full text-sm">
        <thead>
          <tr className="text-zinc-600 text-xs uppercase border-b border-zinc-800">
            <th className="text-left px-4 py-2 w-10">#</th>
            <th className="text-left px-4 py-2">Player</th>
            <th className="text-right px-4 py-2">Elo</th>
            <th className="text-right px-4 py-2">W</th>
            <th className="text-right px-4 py-2">L</th>
            <th className="text-right px-4 py-2">Win%</th>
          </tr>
        </thead>
        <tbody>
          {entries.map((entry) => {
            const isMe = entry.userId === currentUserId
            const total = entry.wins + entry.losses
            const winRate = total > 0 ? Math.round((entry.wins / total) * 100) : 0
            return (
              <tr
                key={entry.userId}
                className={`border-b border-zinc-800/50 transition-colors
                  ${isMe ? 'bg-accent-dim' : 'hover:bg-elevated'}`}
              >
                <td className={`px-4 py-3 ${RANK_STYLE[entry.rank] ?? 'text-zinc-500'}`}>
                  {entry.rank}
                </td>
                <td className="px-4 py-3">
                  <span className={`${isMe ? 'text-accent font-medium' : 'text-zinc-200'}`}>
                    {entry.username}
                    {isMe && <span className="text-zinc-600 text-xs ml-2">(you)</span>}
                  </span>
                </td>
                <td className="px-4 py-3 text-right text-zinc-100 font-medium">{entry.elo}</td>
                <td className="px-4 py-3 text-right text-green-400">{entry.wins}</td>
                <td className="px-4 py-3 text-right text-accent">{entry.losses}</td>
                <td className="px-4 py-3 text-right text-zinc-400">{winRate}%</td>
              </tr>
            )
          })}
        </tbody>
      </table>
    </div>
  )
}
