import { useQuery } from '@tanstack/react-query'
import { api } from '@/lib/axios'
import type { Pokemon } from '@/types'

export function useAllPokemon() {
  return useQuery<Pokemon[]>({
    queryKey: ['pokemon'],
    queryFn: () => api.get<Pokemon[]>('/api/pokemon').then((r) => r.data),
    staleTime: Infinity,
  })
}
