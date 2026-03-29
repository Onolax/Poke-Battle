import { useQuery } from '@tanstack/react-query'
import { api } from '@/lib/axios'
import type { Rating, LeaderboardEntry } from '@/types'

const FORMAT = 'GEN9OU'

export function useMyRating(userId: string | null) {
  return useQuery<Rating>({
    queryKey: ['rating', userId, FORMAT],
    queryFn: () => api.get<Rating>(`/api/ratings/${userId}/${FORMAT}`).then((r) => r.data),
    enabled: !!userId,
  })
}

export function useLeaderboard() {
  return useQuery<LeaderboardEntry[]>({
    queryKey: ['leaderboard', FORMAT],
    queryFn: () =>
      api.get<LeaderboardEntry[]>(`/api/ratings/leaderboard/${FORMAT}`).then((r) => r.data),
    staleTime: 30_000,
  })
}
