import type { BattlePokemon } from '@/types'

interface Props {
  team: BattlePokemon[]
  activeSlot: number
  onSwitch?: (slot: number) => void
  switchMode?: boolean
}

export function TeamStatus({ team, activeSlot, onSwitch, switchMode = false }: Props) {
  if (team.length === 0) {
    return (
      <div className="flex gap-1.5">
        {Array.from({ length: 6 }).map((_, i) => (
          <div key={i} className="w-6 h-6 rounded-full bg-zinc-800 border border-zinc-700" />
        ))}
      </div>
    )
  }

  return (
    <div className="flex gap-1.5">
      {team.map((mon, i) => {
        const isActive = i === activeSlot
        const base = mon.fainted
          ? 'bg-zinc-800 border-zinc-700 opacity-40'
          : isActive
          ? 'bg-accent border-accent'
          : 'bg-green-600 border-green-500'

        return (
          <button
            key={i}
            type="button"
            disabled={!switchMode || mon.fainted || isActive}
            onClick={() => onSwitch?.(i)}
            title={mon.name}
            className={`w-6 h-6 rounded-full border-2 transition-all ${base}
              ${switchMode && !mon.fainted && !isActive ? 'hover:scale-110 cursor-pointer' : 'cursor-default'}
            `}
          />
        )
      })}
    </div>
  )
}
