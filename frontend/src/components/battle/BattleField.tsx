import type { BattlePokemon } from '@/types'
import { TypeBadge } from '@/components/pokemon/TypeBadge'

const STATUS_COLORS: Record<string, string> = {
  BRN: 'bg-orange-700 text-orange-200',
  PSN: 'bg-purple-700 text-purple-200',
  TOX: 'bg-purple-800 text-purple-200',
  PAR: 'bg-yellow-700 text-yellow-100',
  SLP: 'bg-zinc-600 text-zinc-200',
  FRZ: 'bg-cyan-700 text-cyan-100',
}

interface PokemonSideProps {
  mon: BattlePokemon | undefined
  label: string
  flip?: boolean
}

function hpColor(pct: number) {
  if (pct > 0.5) return 'bg-hp-high'
  if (pct > 0.2) return 'bg-hp-mid'
  return 'bg-hp-low'
}

function PokemonSide({ mon, label, flip = false }: PokemonSideProps) {
  const spriteUrl = mon?.dexNumber
    ? `https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/${flip ? 'back/' : ''}${mon.dexNumber}.png`
    : null

  const hpPct = mon ? mon.currentHp / mon.maxHp : 0

  return (
    <div className={`flex flex-col gap-2 ${flip ? 'items-start' : 'items-end'}`}>
      <p className="text-xs text-zinc-500 uppercase tracking-wide">{label}</p>

      {/* Name + status + types */}
      <div className={`flex items-center gap-2 ${flip ? '' : 'flex-row-reverse'}`}>
        <span className="text-zinc-100 font-medium capitalize">{mon?.name ?? '—'}</span>
        {mon?.status && (
          <span className={`text-xs px-1.5 py-0.5 rounded font-bold ${STATUS_COLORS[mon.status] ?? 'bg-zinc-700 text-zinc-300'}`}>
            {mon.status}
          </span>
        )}
        <div className="flex gap-1">
          {mon?.types.map((t) => <TypeBadge key={t} type={t} />)}
        </div>
      </div>

      {/* HP bar */}
      <div className="w-40">
        <div className="flex justify-between text-xs text-zinc-500 mb-0.5">
          <span>HP</span>
          <span>{mon ? `${mon.currentHp}/${mon.maxHp}` : '—'}</span>
        </div>
        <div className="h-2 bg-zinc-800 rounded-full overflow-hidden">
          <div
            className={`h-full rounded-full transition-all duration-500 ${hpColor(hpPct)}`}
            style={{ width: `${Math.max(0, hpPct * 100)}%` }}
          />
        </div>
      </div>

      {/* Sprite */}
      {spriteUrl && (
        <img
          src={spriteUrl}
          alt={mon?.name}
          className="w-28 h-28 object-contain"
          style={{ imageRendering: 'pixelated' }}
        />
      )}
    </div>
  )
}


interface Props {
  myMon: BattlePokemon | undefined
  opponentMon: BattlePokemon | undefined
  myLabel: string
  opponentLabel: string
  turnNumber: number
  weather: string | null
}

export function BattleField({ myMon, opponentMon, myLabel, opponentLabel, turnNumber, weather }: Props) {
  return (
    <div className="bg-surface border border-zinc-800 rounded-lg p-4">
      <div className="flex justify-between items-start mb-4">
        <div className="flex gap-3 items-center">
          <span className="text-zinc-600 text-xs">Turn {turnNumber}</span>
          {weather && (
            <span className="text-xs bg-elevated text-zinc-400 px-2 py-0.5 rounded">{weather}</span>
          )}
        </div>
      </div>

      <div className="flex justify-between items-end gap-4">
        <PokemonSide mon={myMon} label={myLabel} flip />
        <div className="text-zinc-700 font-bold text-lg">VS</div>
        <PokemonSide mon={opponentMon} label={opponentLabel} />
      </div>
    </div>
  )
}
