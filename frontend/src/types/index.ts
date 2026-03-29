// ── Pokemon ──────────────────────────────────────────────────────────────────

export interface BaseStats {
  hp: number
  atk: number
  def: number
  spa: number
  spd: number
  spe: number
}

export interface Pokemon {
  id: string
  dexNumber: number
  name: string
  types: string[]
  baseStats: BaseStats
  abilities: string[]
  learnset: string[]
  tier: Record<string, string>
  weightKg: number
}

// ── Teams ────────────────────────────────────────────────────────────────────

export interface Team {
  id: string
  name: string
  format: string
  pokemonSlugs: string[]
  validated: boolean
}

export interface ValidationResult {
  valid: boolean
  errors: string[]
}

// ── Battle ───────────────────────────────────────────────────────────────────

export interface BattleMove {
  slug: string
  name: string
  type: string
  category: 'PHYSICAL' | 'SPECIAL' | 'STATUS'
  basePower: number
  accuracy: number
  pp: number
  currentPp: number
  priority: number
}

export interface BattlePokemon {
  slug: string
  name: string
  dexNumber: number
  types: string[]
  maxHp: number
  currentHp: number
  attack: number
  defense: number
  spAtk: number
  spDef: number
  speed: number
  status: string | null
  statusTurns: number
  statBoosts: Record<string, number>
  moves: BattleMove[]
  fainted: boolean
}

export interface BattleState {
  battleId: string
  phase: 'ACTIVE' | 'ENDED'
  turnNumber: number
  player1Id: string
  player1Username: string
  player2Id: string
  player2Username: string
  activeSlot1: number
  activeSlot2: number
  weather: string | null
  winnerId: string | null
  endReason: string | null
}

export interface ActionResult {
  playerId: string
  actionType: string
  moveName?: string
  targetName?: string
  damageDealt?: number
  crit?: boolean
  effectiveness?: string
  statusApplied?: string
  message: string
}

export interface TurnResult {
  type: 'TURN_RESULT'
  turnNumber: number
  actions: ActionResult[]
  teamP1: BattlePokemon[]
  teamP2: BattlePokemon[]
  activeSlot1: number
  activeSlot2: number
}

export interface BattleEndMessage {
  type: 'BATTLE_END'
  winnerId: string
  endReason: string
  turnResult?: TurnResult
}

export type StompMessage = TurnResult | BattleEndMessage

// ── Ratings ──────────────────────────────────────────────────────────────────

export interface Rating {
  userId: string
  username: string
  format: string
  elo: number
  wins: number
  losses: number
}

export interface LeaderboardEntry {
  rank: number
  userId: string
  username: string
  elo: number
  wins: number
  losses: number
}

// ── Lobby ────────────────────────────────────────────────────────────────────

export interface LobbyStatus {
  status: 'WAITING' | 'MATCHED'
  battleId?: string
}
