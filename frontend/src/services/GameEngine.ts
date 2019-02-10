import * as models from '../models/GameModels'

export default class GameEngine {
  readonly model: models.Game 

  constructor(game: models.Game) {
    this.model = game
  }

  isValidPoint(p: models.Point): boolean {
    return p.x >= 0 &&
      p.x < this.model.board.columns &&
      p.y >= 0 &&
      p.y <= this.model.board.rows
  }

  isValidMove(move: models.Move): boolean {
    const fromToken = this.getToken(move.from)

    if (!fromToken || !fromToken.playerOwned) {
      return false
    }
    
    if (!this.isValidPoint(move.from) || !this.isValidPoint(move.to)) {
      return false
    }

    const toToken = this.getToken(move.to)

    return toToken === undefined || !toToken.playerOwned
  }

  canMove(): boolean {
    return this.model.isPlayerTurn && !this.model.isGameOver
  }

  getTargetPoints(p: models.Point): models.Point[] {
    const points = [
      { x: p.x + 0, y: p.y - 1 },
      { x: p.x + 0, y: p.y + 1 },
      { x: p.x - 1, y: p.y + 0 },
      { x: p.x + 1, y: p.y + 0 }
    ]

    return points.reduce((results, point) => {
      if (this.isValidMove({ from: p, to: point })) {
        results.push(point)
      }

      return results
    }, Array<models.Point>())
  }

  getToken(p: models.Point): models.Token | undefined {
    return this.model.tokens.find(t => t.position.x === p.x && t.position.y === p.y)
  }
}
