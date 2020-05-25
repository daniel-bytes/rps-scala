import * as models from '../models/GameModels'

export default class GameEngine {
  static isValidPoint(game: models.Game, p: models.Point): boolean {
    return p.x >= 0 &&
      p.x < game.board.columns &&
      p.y >= 0 &&
      p.y <= game.board.rows
  }

  static isValidMove(game: models.Game, move: models.Move): boolean {
    const fromToken = GameEngine.getToken(game, move.from)

    if (!fromToken || !fromToken.playerOwned) {
      return false
    }
    
    if (!GameEngine.isValidPoint(game, move.from) || !GameEngine.isValidPoint(game, move.to)) {
      return false
    }

    const toToken = GameEngine.getToken(game, move.to)

    return toToken === undefined || !toToken.playerOwned
  }

  static canMove(game: models.Game): boolean {
    return game.isPlayerTurn && !game.isGameOver
  }

  static getTargetPoints(game: models.Game, p: models.Point): models.Point[] {
    const points = [
      { x: p.x + 0, y: p.y - 1 },
      { x: p.x + 0, y: p.y + 1 },
      { x: p.x - 1, y: p.y + 0 },
      { x: p.x + 1, y: p.y + 0 }
    ]

    return points.reduce((results, point) => {
      if (this.isValidMove(game, { from: p, to: point })) {
        results.push(point)
      }

      return results
    }, Array<models.Point>())
  }

  static canMoveToken(game: models.Game, t: models.Token): boolean {
    return GameEngine.isMovableTokenType(t) && GameEngine.getTargetPoints(game, t.position).length > 0
  }

  static getToken(game: models.Game, p: models.Point): models.Token | undefined {
    return game.tokens.find(t => t.position.x === p.x && t.position.y === p.y)
  }

  static isMovableTokenType(t: models.Token): boolean {
    return t.tokenType === models.TokenType.rock ||
      t.tokenType === models.TokenType.paper ||
      t.tokenType === models.TokenType.scissor
  }

  static pointsEqual(lhs: models.Point, rhs: models.Point): boolean {
    return lhs.x === rhs.x && lhs.y === rhs.y
  }
}
