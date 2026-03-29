import type { Rating } from '@/types'

interface Props {
  rating: Rating
}

export function RatingCard({ rating }: Props) {
  const total = rating.wins + rating.losses
  const winRate = total > 0 ? Math.round((rating.wins / total) * 100) : 0

  return (
    <div className="bg-surface border border-zinc-800 rounded-lg p-6">
      <div className="flex items-center justify-between mb-4">
        <div>
          <p className="text-zinc-100 font-bold text-xl">{rating.username}</p>
          <p className="text-zinc-600 text-xs uppercase tracking-wide">{rating.format}</p>
        </div>
        <div className="text-right">
          <p className="text-4xl font-bold text-accent">{rating.elo}</p>
          <p className="text-zinc-500 text-xs">Elo rating</p>
        </div>
      </div>

      <div className="grid grid-cols-3 gap-4 pt-4 border-t border-zinc-800">
        <div className="text-center">
          <p className="text-2xl font-bold text-zinc-100">{rating.wins}</p>
          <p className="text-zinc-500 text-xs">Wins</p>
        </div>
        <div className="text-center">
          <p className="text-2xl font-bold text-zinc-100">{rating.losses}</p>
          <p className="text-zinc-500 text-xs">Losses</p>
        </div>
        <div className="text-center">
          <p className="text-2xl font-bold text-zinc-100">{winRate}%</p>
          <p className="text-zinc-500 text-xs">Win rate</p>
        </div>
      </div>
    </div>
  )
}
