import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { api } from '@/lib/axios'
import type { Team, ValidationResult } from '@/types'

export function useTeams() {
  return useQuery<Team[]>({
    queryKey: ['teams'],
    queryFn: () => api.get<Team[]>('/api/teams').then((r) => r.data),
  })
}

export function useCreateTeam() {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: (body: { name: string; format: string; pokemonSlugs: string[] }) =>
      api.post<Team>('/api/teams', body).then((r) => r.data),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['teams'] }),
  })
}

export function useDeleteTeam() {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: (id: string) => api.delete(`/api/teams/${id}`),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['teams'] }),
  })
}

export function useValidateTeam() {
  return useMutation({
    mutationFn: (body: { pokemonSlugs: string[]; formatId: string }) =>
      api.post<ValidationResult>('/api/validate/team', body).then((r) => r.data),
  })
}
