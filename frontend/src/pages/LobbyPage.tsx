import { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import { useTeams } from '@/hooks/useTeams'
import { useLobbyStatus, useJoinQueue, useLeaveQueue } from '@/hooks/useLobby'

export function LobbyPage() {
  const navigate = useNavigate()
  const { data: teams } = useTeams()
  const [selectedTeamId, setSelectedTeamId] = useState('')
  const [isQueued, setIsQueued] = useState(false)
  const [elapsed, setElapsed] = useState(0)

  const statusQuery = useLobbyStatus(isQueued)
  const joinQueue = useJoinQueue()
  const leaveQueue = useLeaveQueue()

  // Auto-redirect when match is found
  useEffect(() => {
    if (statusQuery.data?.status === 'MATCHED' && statusQuery.data.battleId) {
      navigate(`/battle/${statusQuery.data.battleId}`)
    }
  }, [statusQuery.data, navigate])

  // Elapsed timer while queued
  useEffect(() => {
    if (!isQueued) { setElapsed(0); return }
    const id = setInterval(() => setElapsed((s) => s + 1), 1000)
    return () => clearInterval(id)
  }, [isQueued])

  async function handleFindBattle() {
    if (!selectedTeamId) return
    await joinQueue.mutateAsync({ teamId: selectedTeamId, format: 'GEN9OU' })
    setIsQueued(true)
  }

  async function handleCancel() {
    await leaveQueue.mutateAsync()
    setIsQueued(false)
  }

  const formatElapsed = (s: number) =>
    `${Math.floor(s / 60)}:${String(s % 60).padStart(2, '0')}`

  return (
    <div className="max-w-md mx-auto px-4 py-16 text-center">
      <h1 className="text-2xl font-bold text-zinc-100 mb-2">Find a Battle</h1>
      <p className="text-zinc-500 text-sm mb-10">GEN 9 OU — 1v1</p>

      {!isQueued ? (
        <div className="space-y-4">
          <div className="text-left">
            <label className="block text-sm text-zinc-400 mb-1.5">Select team</label>
            <select
              value={selectedTeamId}
              onChange={(e) => setSelectedTeamId(e.target.value)}
              className="w-full bg-elevated border border-zinc-700 rounded px-3 py-2 text-zinc-100 text-sm focus:outline-none focus:border-accent"
            >
              <option value="">— choose a team —</option>
              {teams?.map((t) => (
                <option key={t.id} value={t.id}>{t.name}</option>
              ))}
            </select>
          </div>

          {joinQueue.isError && (
            <p className="text-accent text-sm">Failed to join queue. Try again.</p>
          )}

          <button
            onClick={handleFindBattle}
            disabled={!selectedTeamId || joinQueue.isPending}
            className="w-full bg-accent hover:bg-accent-hover disabled:opacity-50 disabled:cursor-not-allowed text-white font-medium py-2.5 rounded transition-colors"
          >
            {joinQueue.isPending ? 'Joining…' : 'Find Battle'}
          </button>
        </div>
      ) : (
        <div className="space-y-6">
          {/* Spinner */}
          <div className="flex justify-center">
            <div className="w-14 h-14 border-4 border-zinc-700 border-t-accent rounded-full animate-spin" />
          </div>

          <div>
            <p className="text-zinc-200 font-medium">Searching for opponent…</p>
            <p className="text-zinc-500 text-sm mt-1">{formatElapsed(elapsed)}</p>
          </div>

          <button
            onClick={handleCancel}
            disabled={leaveQueue.isPending}
            className="bg-elevated hover:bg-zinc-700 text-zinc-300 text-sm px-6 py-2 rounded transition-colors"
          >
            {leaveQueue.isPending ? 'Cancelling…' : 'Cancel'}
          </button>
        </div>
      )}
    </div>
  )
}
