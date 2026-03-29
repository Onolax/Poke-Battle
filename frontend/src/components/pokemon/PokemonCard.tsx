import { TypeBadge } from './TypeBadge'
import type { Pokemon } from '@/types'

interface Props {
  pokemon: Pokemon
  selected?: boolean
  onSelect?: (slug: string) => void
  disabled?: boolean
}

export function PokemonCard({ pokemon, selected = false, onSelect, disabled = false }: Props) {
  const spriteUrl = `https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/${pokemon.dexNumber}.png`

  return (
    <button
      type="button"
      onClick={() => onSelect?.(pokemon.name.toLowerCase())}
      disabled={disabled}
      className={`
        bg-surface border rounded-lg p-3 text-left w-full transition-all
        ${selected
          ? 'border-accent ring-1 ring-accent'
          : 'border-zinc-800 hover:border-zinc-600'}
        ${disabled && !selected ? 'opacity-40 cursor-not-allowed' : 'cursor-pointer'}
        ${onSelect ? '' : 'cursor-default'}
      `}
    >
      <div className="flex flex-col items-center gap-2">
        <img
          src={spriteUrl}
          alt={pokemon.name}
          className="w-16 h-16 object-contain pixelated"
          loading="lazy"
        />
        <div className="text-center">
          <p className="text-zinc-100 text-sm font-medium capitalize">{pokemon.name}</p>
          <p className="text-zinc-600 text-xs mb-1.5">#{String(pokemon.dexNumber).padStart(3, '0')}</p>
          <div className="flex gap-1 justify-center flex-wrap">
            {pokemon.types.map((t) => (
              <TypeBadge key={t} type={t} />
            ))}
          </div>
        </div>
      </div>
    </button>
  )
}
