import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useAllPokemon } from '@/hooks/usePokemon'
import { useCreateTeam, useValidateTeam } from '@/hooks/useTeams'
import { PokemonCard } from '@/components/pokemon/PokemonCard'
import { TypeBadge } from '@/components/pokemon/TypeBadge'
import type { Pokemon } from '@/types'

const FORMAT = 'GEN9OU'
const MAX_SLOTS = 6

export function TeamBuilderPage() {
  const navigate = useNavigate()
  const { data: allPokemon, isLoading } = useAllPokemon()
  const createTeam = useCreateTeam()
  const validateTeam = useValidateTeam()

  const [teamName, setTeamName] = useState('')
  const [selectedSlugs, setSelectedSlugs] = useState<string[]>([])
  const [search, setSearch] = useState('')
  const [validationErrors, setValidationErrors] = useState<string[]>([])
  const [saveError, setSaveError] = useState<string | null>(null)

  const pokemonBySlug = new Map<string, Pokemon>(
    (allPokemon ?? []).map((p) => [p.name.toLowerCase(), p])
  )

  function toggleSlug(slug: string) {
    setValidationErrors([])
    setSaveError(null)
    setSelectedSlugs((prev) =>
      prev.includes(slug) ? prev.filter((s) => s !== slug) : prev.length < MAX_SLOTS ? [...prev, slug] : prev
    )
  }

  async function handleSave() {
    setValidationErrors([])
    setSaveError(null)

    if (!teamName.trim()) { setSaveError('Team name is required.'); return }
    if (selectedSlugs.length === 0) { setSaveError('Select at least one Pokémon.'); return }

    // Step 1: validate
    const validation = await validateTeam.mutateAsync({ pokemonSlugs: selectedSlugs, formatId: FORMAT })
    if (!validation.valid) {
      setValidationErrors(validation.errors)
      return
    }

    // Step 2: save
    await createTeam.mutateAsync({ name: teamName.trim(), format: FORMAT, pokemonSlugs: selectedSlugs })
    navigate('/teams')
  }

  const filtered = (allPokemon ?? []).filter((p) =>
    p.name.toLowerCase().includes(search.toLowerCase())
  )

  const isBusy = validateTeam.isPending || createTeam.isPending

  return (
    <div className="max-w-5xl mx-auto px-4 py-8">
      <h1 className="text-2xl font-bold text-zinc-100 mb-6">Build a Team</h1>

      {/* Team name */}
      <div className="mb-6">
        <label className="block text-sm text-zinc-400 mb-1.5">Team name</label>
        <input
          type="text"
          value={teamName}
          onChange={(e) => setTeamName(e.target.value)}
          placeholder="My OU Team"
          className="w-full max-w-sm bg-elevated border border-zinc-700 rounded px-3 py-2 text-zinc-100 text-sm focus:outline-none focus:border-accent focus:ring-1 focus:ring-accent placeholder-zinc-600"
        />
      </div>

      {/* Selected slots */}
      <div className="mb-6">
        <p className="text-sm text-zinc-400 mb-2">
          Team ({selectedSlugs.length}/{MAX_SLOTS})
        </p>
        <div className="grid grid-cols-6 gap-2">
          {Array.from({ length: MAX_SLOTS }).map((_, i) => {
            const slug = selectedSlugs[i]
            const mon = slug ? pokemonBySlug.get(slug) : undefined
            return (
              <div
                key={i}
                className="bg-surface border border-zinc-800 rounded-lg p-2 aspect-square flex flex-col items-center justify-center gap-1"
              >
                {mon ? (
                  <>
                    <img
                      src={`https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/${mon.dexNumber}.png`}
                      alt={mon.name}
                      className="w-12 h-12 object-contain"
                    />
                    <div className="flex gap-0.5 flex-wrap justify-center">
                      {mon.types.map((t) => <TypeBadge key={t} type={t} />)}
                    </div>
                    <button
                      onClick={() => toggleSlug(slug)}
                      className="text-zinc-600 hover:text-accent text-xs transition-colors"
                    >
                      remove
                    </button>
                  </>
                ) : (
                  <span className="text-zinc-700 text-xs">empty</span>
                )}
              </div>
            )
          })}
        </div>
      </div>

      {/* Errors */}
      {(saveError || validationErrors.length > 0) && (
        <div className="mb-4 text-accent text-sm bg-accent-dim border border-red-900 rounded px-3 py-2 space-y-1">
          {saveError && <p>{saveError}</p>}
          {validationErrors.map((e, i) => <p key={i}>{e}</p>)}
        </div>
      )}

      {/* Save button */}
      <div className="flex gap-3 mb-8">
        <button
          onClick={handleSave}
          disabled={isBusy}
          className="bg-accent hover:bg-accent-hover disabled:opacity-50 disabled:cursor-not-allowed text-white font-medium px-6 py-2 rounded transition-colors"
        >
          {validateTeam.isPending ? 'Validating…' : createTeam.isPending ? 'Saving…' : 'Save team'}
        </button>
        <button
          onClick={() => navigate('/teams')}
          className="bg-elevated hover:bg-zinc-700 text-zinc-300 px-4 py-2 rounded transition-colors text-sm"
        >
          Cancel
        </button>
      </div>

      {/* Pokemon picker */}
      <div>
        <h2 className="text-lg font-semibold text-zinc-200 mb-3">Add Pokémon</h2>
        <input
          type="text"
          placeholder="Search…"
          value={search}
          onChange={(e) => setSearch(e.target.value)}
          className="w-full max-w-xs bg-elevated border border-zinc-700 rounded px-3 py-2 text-zinc-100 text-sm mb-4 focus:outline-none focus:border-accent placeholder-zinc-600"
        />
        {isLoading ? (
          <p className="text-zinc-500 text-sm">Loading…</p>
        ) : (
          <div className="grid grid-cols-4 sm:grid-cols-6 md:grid-cols-8 gap-2 max-h-[480px] overflow-y-auto pr-1">
            {filtered.map((p) => {
              const slug = p.name.toLowerCase()
              const isSelected = selectedSlugs.includes(slug)
              const isFull = selectedSlugs.length >= MAX_SLOTS
              return (
                <PokemonCard
                  key={p.id}
                  pokemon={p}
                  selected={isSelected}
                  onSelect={toggleSlug}
                  disabled={isFull && !isSelected}
                />
              )
            })}
          </div>
        )}
      </div>
    </div>
  )
}
