export interface Point {
  x: number
  y: number
}

export interface Geometry {
  rows: number
  columns: number
}

export interface Move {
  from: Point
  to: Point
}

export enum TokenType {
  rock = "rock",
  paper = "paper",
  scissor = "scissor",
  bomb = "bomb",
  flag = "flag",
  other = "other"
}

export interface Token {
  position: Point
  tokenType: TokenType
  playerOwned: boolean
}

export interface CombatSummary {
  attackerTokenType: TokenType
  defenderTokenType: TokenType
  winnerTokenType: TokenType | null
}

export interface RecentMove {
  playerId: string
  from: Point
  to: Point
  combatSummary: CombatSummary | null
}

export interface Game {
  gameId: string
  playerId: string
  playerName: string
  otherPlayerName: string
  isPlayerTurn: boolean
  isGameOver: boolean
  winnerName: string | null
  board: Geometry
  tokens: Token[]
  recentMoves: RecentMove[]
}

export interface GamesOverview {
  games: GameOverview[]
}

export interface GameOverview {
  id: string
  playerId: string
  playerName: string
  otherPlayerName: string
  isGameOver: boolean
  winnerName: string | null
}
