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
  rock,
  paper,
  scissor,
  bomb,
  flag,
  other
}

export interface Token {
  position: Point
  tokenType: string
  playerOwned: boolean
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
