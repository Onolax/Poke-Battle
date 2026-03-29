import { useState } from 'react'
import { useAllPokemon } from '@/hooks/usePokemon'
import { PokemonCard } from '@/components/pokemon/PokemonCard'

const ALL_TYPES = [
  'Normal','Fire','Water','Grass','Electric','Ice','Fighting','Poison',
  'Ground','Flying','Psychic','Bug','Rock','Ghost','Dragon','Dark','Steel','Fairy',
]

export function PokemonBrowserPage() {
  const { data: pokemon, isLoading, isError } = useAllPokemon()
  const [search, setSearch] = useState('')
  const [typeFilter, setTypeFilter] = useState<string | null>(null)

  const filtered = (pokemon ?? []).filter((p) => {
    const matchesSearch = p.name.toLowerCase().includes(search.toLowerCase())
    const matchesType = typeFilter == null || p.types.includes(typeFilter)
    return matchesSearch && matchesType
  })

  return (
    <div className="max-w-6xl mx-auto px-4 py-8">
      <h1 className="text-2xl font-bold text-zinc-100 mb-6">Pokédex</h1>

      {/* Filters */}
      <div className="space-y-3 mb-6">
        <input
          type="text"
          placeholder="Search Pokémon…"
          value={search}
          onChange={(e) => setSearch(e.target.value)}
          className="w-full max-w-sm bg-elevated border border-zinc-700 rounded px-3 py-2 text-zinc-100 text-sm placeholder-zinc-600 focus:outline-none focus:border-accent focus:ring-1 focus:ring-accent"
        />
        <div className="flex flex-wrap gap-2">
          <button
            onClick={() => setTypeFilter(null)}
            className={`text-xs px-2.5 py-1 rounded transition-colors ${
              typeFilter == null
                ? 'bg-accent text-white'
                : 'bg-elevated text-zinc-400 hover:text-zinc-200'
            }`}
          >
            All
          </button>
          {ALL_TYPES.map((t) => (
            <button
              key={t}
              onClick={() => setTypeFilter(typeFilter === t ? null : t)}
              className={`text-xs px-2.5 py-1 rounded transition-colors ${
                typeFilter === t
                  ? 'bg-accent text-white'
                  : 'bg-elevated text-zinc-400 hover:text-zinc-200'
              }`}
            >
              {t}
            </button>
          ))}
        </div>
      </div>

      {/* Results */}
      {isLoading && (
        <p className="text-zinc-500 text-sm">Loading Pokédex…</p>
      )}
      {isError && (
        <p className="text-accent text-sm">Failed to load Pokémon. Is the backend running?</p>
      )}
      {!isLoading && !isError && (
        <>
          <p className="text-zinc-600 text-sm mb-4">{filtered.length} Pokémon</p>
          <div className="grid grid-cols-3 sm:grid-cols-4 md:grid-cols-6 lg:grid-cols-8 gap-3">
            {filtered.map((p) => (
              <PokemonCard key={p.id} pokemon={p} />
            ))}
          </div>
        </>
      )}
    </div>
  )
}
