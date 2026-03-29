import { useState } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { useBattle } from '@/hooks/useBattle'
import { BattleField } from '@/components/battle/BattleField'
import { MoveSelector } from '@/components/battle/MoveSelector'
import { TeamStatus } from '@/components/battle/TeamStatus'
import { BattleLog } from '@/components/battle/BattleLog'
import { useAuthStore } from '@/stores/authStore'

export function BattlePage() {
  const { id } = useParams<{ id: string }>()
  const navigate = useNavigate()
  const userId = useAuthStore((s) => s.userId)
  const [switchMode, setSwitchMode] = useState(false)

  const {
    battleState,
    myTeam,
    opponentTeam,
    myActiveSlot,
    opponentActiveSlot,
    myUsername,
    opponentUsername,
    log,
    phase,
    actionPending,
    endResult,
    connected,
    isLoading,
    sendMove,
    sendSwitch,
    sendForfeit,
  } = useBattle(id ?? '')

  if (!id) return <p className="p-8 text-accent">Invalid battle ID.</p>
  if (isLoading) return <p className="p-8 text-zinc-500">Loading battle…</p>

  const myActiveMon = myTeam[myActiveSlot]
  const opponentActiveMon = opponentTeam[opponentActiveSlot]

  function handleSwitch(slot: number) {
    sendSwitch(slot)
    setSwitchMode(false)
  }

  return (
    <div className="max-w-5xl mx-auto px-4 py-6 space-y-4">
      {/* Header row */}
      <div className="flex items-center justify-between">
        <div className="flex items-center gap-3">
          <span className="text-zinc-400 text-sm">{opponentUsername ?? '…'}</span>
          <TeamStatus team={opponentTeam} activeSlot={opponentActiveSlot} />
        </div>
        <div className="flex items-center gap-3">
          {connected ? (
            <span className="w-2 h-2 rounded-full bg-green-500" title="Connected" />
          ) : (
            <span className="w-2 h-2 rounded-full bg-red-500 animate-pulse" title="Reconnecting…" />
          )}
          <button
            onClick={() => { if (confirm('Forfeit this battle?')) sendForfeit() }}
            className="text-xs text-zinc-600 hover:text-accent transition-colors"
          >
            Forfeit
          </button>
        </div>
      </div>

      {/* Main grid */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
        {/* Left: battle field */}
        <div className="space-y-4">
          <BattleField
            myMon={myActiveMon}
            opponentMon={opponentActiveMon}
            myLabel={myUsername ?? 'You'}
            opponentLabel={opponentUsername ?? 'Opponent'}
            turnNumber={battleState?.turnNumber ?? 0}
            weather={battleState?.weather ?? null}
          />

          {/* My team status */}
          <div className="flex items-center gap-3">
            <span className="text-zinc-500 text-sm">{myUsername ?? 'You'}</span>
            <TeamStatus
              team={myTeam}
              activeSlot={myActiveSlot}
              onSwitch={handleSwitch}
              switchMode={switchMode}
            />
          </div>
        </div>

        {/* Right: log */}
        <div className="h-64 md:h-auto">
          <BattleLog messages={log} />
        </div>
      </div>

      {/* Move selector */}
      {phase === 'ACTIVE' && (
        <MoveSelector
          activeMon={myActiveMon}
          actionPending={actionPending}
          switchMode={switchMode}
          onSwitchMode={setSwitchMode}
          onMove={(slug) => { sendMove(slug); setSwitchMode(false) }}
          onSwitch={handleSwitch}
          myTeam={myTeam}
          activeSlot={myActiveSlot}
        />
      )}

      {phase === 'WAITING' && (
        <div className="bg-surface border border-zinc-800 rounded-lg p-6 text-center">
          <div className="w-8 h-8 border-2 border-zinc-700 border-t-accent rounded-full animate-spin mx-auto mb-3" />
          <p className="text-zinc-500 text-sm">Waiting for first turn…</p>
        </div>
      )}

      {/* Battle end overlay */}
      {phase === 'ENDED' && endResult && (
        <div className="fixed inset-0 bg-black/70 flex items-center justify-center z-50">
          <div className="bg-surface border border-zinc-700 rounded-xl p-8 text-center max-w-sm w-full mx-4 space-y-4">
            {endResult.winnerId === userId ? (
              <>
                <p className="text-4xl font-bold text-yellow-400">Victory!</p>
                <p className="text-zinc-400 text-sm">You defeated {opponentUsername}!</p>
              </>
            ) : (
              <>
                <p className="text-4xl font-bold text-accent">Defeated</p>
                <p className="text-zinc-400 text-sm">
                  {endResult.endReason === 'FORFEIT' ? 'Opponent forfeited.' : `${opponentUsername} wins!`}
                </p>
              </>
            )}
            <p className="text-zinc-600 text-xs">Turn {battleState?.turnNumber}</p>
            <div className="flex gap-3 justify-center pt-2">
              <button
                onClick={() => navigate('/profile')}
                className="bg-elevated hover:bg-zinc-700 text-zinc-300 text-sm px-4 py-2 rounded transition-colors"
              >
                View Profile
              </button>
              <button
                onClick={() => navigate('/lobby')}
                className="bg-accent hover:bg-accent-hover text-white text-sm px-4 py-2 rounded transition-colors"
              >
                Battle Again
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  )
}
