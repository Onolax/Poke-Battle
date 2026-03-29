import { useState, useEffect, useRef, useCallback } from 'react'
import { useQuery } from '@tanstack/react-query'
import { Client } from '@stomp/stompjs'
import SockJS from 'sockjs-client'
import { api } from '@/lib/axios'
import { useAuthStore } from '@/stores/authStore'
import type { BattleState, BattlePokemon, StompMessage, TurnResult } from '@/types'

export type BattlePhase = 'LOADING' | 'WAITING' | 'ACTIVE' | 'ENDED'

export interface BattleEndResult {
  winnerId: string
  endReason: string
}

export function useBattle(battleId: string) {
  const { token, userId } = useAuthStore()

  const [myTeam, setMyTeam] = useState<BattlePokemon[]>([])
  const [opponentTeam, setOpponentTeam] = useState<BattlePokemon[]>([])
  const [battleState, setBattleState] = useState<BattleState | null>(null)
  const [log, setLog] = useState<string[]>([])
  const [phase, setPhase] = useState<BattlePhase>('LOADING')
  const [actionPending, setActionPending] = useState(false)
  const [endResult, setEndResult] = useState<BattleEndResult | null>(null)
  const [connected, setConnected] = useState(false)

  const stompRef = useRef<Client | null>(null)

  // Fetch initial battle state via REST
  const stateQuery = useQuery<BattleState>({
    queryKey: ['battle', battleId],
    queryFn: () => api.get<BattleState>(`/api/battle/${battleId}/state`).then((r) => r.data),
  })

  // Fetch initial teams via REST — available as soon as the battle is initialised
  const teamsQuery = useQuery<{ teamP1: BattlePokemon[]; teamP2: BattlePokemon[] }>({
    queryKey: ['battle', battleId, 'teams'],
    queryFn: () =>
      api.get(`/api/battle/${battleId}/teams`).then((r) => r.data),
    enabled: !!stateQuery.data,
  })

  useEffect(() => {
    if (!stateQuery.data || !teamsQuery.data) return
    const state = stateQuery.data
    setBattleState(state)
    const iAmP1 = state.player1Id === userId
    setMyTeam(iAmP1 ? teamsQuery.data.teamP1 : teamsQuery.data.teamP2)
    setOpponentTeam(iAmP1 ? teamsQuery.data.teamP2 : teamsQuery.data.teamP1)
    setPhase('ACTIVE')
  }, [stateQuery.data, teamsQuery.data, userId])

  // Process a TurnResult message (also called for the final turn inside BATTLE_END)
  const applyTurnResult = useCallback(
    (result: TurnResult, currentState: BattleState | null) => {
      const iAmP1 = currentState?.player1Id === userId
      setMyTeam(iAmP1 ? result.teamP1 : result.teamP2)
      setOpponentTeam(iAmP1 ? result.teamP2 : result.teamP1)
      setBattleState((prev) =>
        prev
          ? {
              ...prev,
              turnNumber: result.turnNumber,
              activeSlot1: result.activeSlot1,
              activeSlot2: result.activeSlot2,
            }
          : prev
      )
      result.actions.forEach((a) =>
        setLog((prev) => [...prev, a.message])
      )
      setActionPending(false)
      setPhase('ACTIVE')
    },
    [userId]
  )

  // STOMP connection — starts once we have the initial battle state
  useEffect(() => {
    if (!stateQuery.data || !token) return

    const client = new Client({
      webSocketFactory: () => new SockJS(`/ws/battle?token=${token}`),
      reconnectDelay: 5000,
      onConnect: () => {
        setConnected(true)
        client.subscribe(`/topic/battle/${battleId}`, (frame) => {
          const msg: StompMessage = JSON.parse(frame.body)

          if (msg.type === 'TURN_RESULT') {
            applyTurnResult(msg, stateQuery.data!)
          } else if (msg.type === 'BATTLE_END') {
            if (msg.turnResult) applyTurnResult(msg.turnResult, stateQuery.data!)
            const winnerName =
              msg.winnerId === stateQuery.data!.player1Id
                ? stateQuery.data!.player1Username
                : stateQuery.data!.player2Username
            const suffix = msg.winnerId === userId ? 'You won!' : `${winnerName} wins.`
            setLog((prev) => [...prev, `Battle over — ${suffix}`])
            setEndResult({ winnerId: msg.winnerId, endReason: msg.endReason })
            setPhase('ENDED')
          }
        })
      },
      onDisconnect: () => setConnected(false),
      onStompError: (frame) => {
        console.error('STOMP error', frame)
        setConnected(false)
      },
    })

    stompRef.current = client
    client.activate()

    return () => {
      client.deactivate()
      stompRef.current = null
    }
  }, [battleId, token, stateQuery.data, applyTurnResult, userId])

  function sendMove(moveSlug: string) {
    if (!stompRef.current?.connected || actionPending) return
    setActionPending(true)
    stompRef.current.publish({
      destination: `/app/battle/${battleId}/move`,
      body: JSON.stringify({ moveSlug }),
    })
  }

  function sendSwitch(slot: number) {
    if (!stompRef.current?.connected || actionPending) return
    setActionPending(true)
    stompRef.current.publish({
      destination: `/app/battle/${battleId}/switch`,
      body: JSON.stringify({ slot: String(slot) }),
    })
  }

  function sendForfeit() {
    if (!stompRef.current?.connected) return
    stompRef.current.publish({
      destination: `/app/battle/${battleId}/forfeit`,
      body: '{}',
    })
  }

  const iAmP1 = battleState?.player1Id === userId
  const myActiveSlot = iAmP1 ? battleState?.activeSlot1 : battleState?.activeSlot2
  const opponentActiveSlot = iAmP1 ? battleState?.activeSlot2 : battleState?.activeSlot1
  const myUsername = iAmP1 ? battleState?.player1Username : battleState?.player2Username
  const opponentUsername = iAmP1 ? battleState?.player2Username : battleState?.player1Username

  return {
    battleState,
    myTeam,
    opponentTeam,
    myActiveSlot: myActiveSlot ?? 0,
    opponentActiveSlot: opponentActiveSlot ?? 0,
    myUsername,
    opponentUsername,
    log,
    phase,
    actionPending,
    endResult,
    connected,
    isLoading: stateQuery.isLoading || teamsQuery.isLoading,
    sendMove,
    sendSwitch,
    sendForfeit,
  }
}
