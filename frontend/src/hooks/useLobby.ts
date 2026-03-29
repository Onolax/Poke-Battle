import { useQuery, useMutation } from '@tanstack/react-query'
import { api } from '@/lib/axios'
import type { LobbyStatus } from '@/types'

export function useLobbyStatus(enabled: boolean) {
  return useQuery<LobbyStatus>({
    queryKey: ['lobby', 'status'],
    queryFn: () => api.get<LobbyStatus>('/api/lobby/status').then((r) => r.data),
    enabled,
    refetchInterval: (query) =>
      query.state.data?.status === 'MATCHED' ? false : 2000,
    refetchIntervalInBackground: false,
  })
}

export function useJoinQueue() {
  return useMutation({
    mutationFn: (body: { teamId: string; format: string }) =>
      api.post('/api/lobby/queue', body),
  })
}

export function useLeaveQueue() {
  return useMutation({
    mutationFn: () => api.delete('/api/lobby/queue'),
  })
}
