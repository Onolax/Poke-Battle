import type { BattlePokemon } from '@/types'
import { TypeBadge } from '@/components/pokemon/TypeBadge'

const CATEGORY_ICON: Record<string, string> = {
  PHYSICAL: '⚔',
  SPECIAL: '✦',
  STATUS: '●',
}

interface Props {
  activeMon: BattlePokemon | undefined
  actionPending: boolean
  switchMode: boolean
  onSwitchMode: (v: boolean) => void
  onMove: (slug: string) => void
  onSwitch: (slot: number) => void
  myTeam: BattlePokemon[]
  activeSlot: number
}

export function MoveSelector({
  activeMon,
  actionPending,
  switchMode,
  onSwitchMode,
  onMove,
  onSwitch,
  myTeam,
  activeSlot,
}: Props) {
  return (
    <div className="bg-surface border border-zinc-800 rounded-lg p-4">
      {/* Tab switcher */}
      <div className="flex gap-2 mb-4">
        <button
          onClick={() => onSwitchMode(false)}
          className={`text-sm px-3 py-1.5 rounded transition-colors ${
            !switchMode ? 'bg-accent text-white' : 'bg-elevated text-zinc-400 hover:text-zinc-200'
          }`}
        >
          Moves
        </button>
        <button
          onClick={() => onSwitchMode(true)}
          className={`text-sm px-3 py-1.5 rounded transition-colors ${
            switchMode ? 'bg-accent text-white' : 'bg-elevated text-zinc-400 hover:text-zinc-200'
          }`}
        >
          Switch
        </button>
      </div>

      {!switchMode ? (
        <div className="grid grid-cols-2 gap-2">
          {activeMon?.moves.map((move) => {
            const noPP = move.currentPp === 0
            return (
              <button
                key={move.slug}
                onClick={() => onMove(move.slug)}
                disabled={actionPending || noPP}
                className={`
                  flex flex-col items-start gap-1 p-3 rounded border text-left transition-all
                  ${noPP
                    ? 'border-zinc-800 opacity-40 cursor-not-allowed'
                    : actionPending
                    ? 'border-zinc-800 opacity-60 cursor-not-allowed'
                    : 'border-zinc-700 hover:border-zinc-500 hover:bg-elevated cursor-pointer'}
                `}
              >
                <div className="flex items-center justify-between w-full">
                  <span className="text-zinc-100 text-sm font-medium">{move.name}</span>
                  <span className="text-zinc-600 text-xs">{CATEGORY_ICON[move.category]}</span>
                </div>
                <div className="flex items-center gap-2">
                  <TypeBadge type={move.type} />
                  <span className="text-zinc-500 text-xs">
                    {move.basePower > 0 ? `${move.basePower} BP` : '—'}
                  </span>
                  <span className={`text-xs ml-auto ${noPP ? 'text-accent' : 'text-zinc-500'}`}>
                    {move.currentPp}/{move.pp} PP
                  </span>
                </div>
              </button>
            )
          })}

          {/* Padding if fewer than 4 moves */}
          {activeMon && activeMon.moves.length < 4 &&
            Array.from({ length: 4 - activeMon.moves.length }).map((_, i) => (
              <div key={i} className="border border-zinc-800 rounded p-3 opacity-20" />
            ))
          }

          {!activeMon && (
            <p className="col-span-2 text-zinc-600 text-sm text-center py-4">
              Waiting for battle data…
            </p>
          )}
        </div>
      ) : (
        <div className="grid grid-cols-3 gap-2">
          {myTeam.map((mon, i) => {
            const isActive = i === activeSlot
            return (
              <button
                key={i}
                onClick={() => onSwitch(i)}
                disabled={actionPending || mon.fainted || isActive}
                className={`
                  flex flex-col items-center gap-1 p-2 rounded border text-sm transition-all
                  ${isActive
                    ? 'border-accent bg-accent-dim opacity-60 cursor-default'
                    : mon.fainted
                    ? 'border-zinc-800 opacity-30 cursor-not-allowed'
                    : actionPending
                    ? 'border-zinc-800 opacity-60 cursor-not-allowed'
                    : 'border-zinc-700 hover:border-zinc-500 hover:bg-elevated cursor-pointer'}
                `}
              >
                <span className="text-zinc-100 capitalize">{mon.name}</span>
                <span className="text-xs text-zinc-500">
                  {mon.fainted ? 'Fainted' : isActive ? 'Active' : `${mon.currentHp}/${mon.maxHp}`}
                </span>
              </button>
            )
          })}
        </div>
      )}
    </div>
  )
}
