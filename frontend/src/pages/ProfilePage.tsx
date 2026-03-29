import { useAuthStore } from '@/stores/authStore'
import { useMyRating, useLeaderboard } from '@/hooks/useRatings'
import { RatingCard } from '@/components/rating/RatingCard'
import { Leaderboard } from '@/components/rating/Leaderboard'

export function ProfilePage() {
  const { userId } = useAuthStore()
  const { data: rating, isLoading: ratingLoading } = useMyRating(userId)
  const { data: leaderboard, isLoading: lbLoading } = useLeaderboard()

  return (
    <div className="max-w-3xl mx-auto px-4 py-8 space-y-8">
      <h1 className="text-2xl font-bold text-zinc-100">Profile</h1>

      {/* Rating card */}
      {ratingLoading && <p className="text-zinc-500 text-sm">Loading rating…</p>}
      {!ratingLoading && !rating && (
        <div className="bg-surface border border-zinc-800 rounded-lg p-6 text-zinc-500 text-sm">
          No battles played yet. Head to the{' '}
          <a href="/lobby" className="text-accent hover:text-red-400">lobby</a> to start competing!
        </div>
      )}
      {rating && <RatingCard rating={rating} />}

      {/* Leaderboard */}
      {lbLoading && <p className="text-zinc-500 text-sm">Loading leaderboard…</p>}
      {leaderboard && leaderboard.length > 0 && (
        <Leaderboard entries={leaderboard} currentUserId={userId} />
      )}
    </div>
  )
}
