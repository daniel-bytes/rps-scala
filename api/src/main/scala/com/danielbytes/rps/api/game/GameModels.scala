package com.danielbytes.rps.api.game

import com.danielbytes.rps.model.{ GameOverStatus, _ }

case class GameMoveApiModel(
  from: Point,
  to: Point
)

case class TokenApiModel(
  position: Point,
  tokenType: String,
  playerOwned: Boolean
)

case class GameApiModel(
  gameId: String,
  playerId: String,
  otherPlayerId: String,
  isPlayerTurn: Boolean,
  isGameOver: Boolean,
  winnerId: Option[String],
  board: Geometry,
  tokens: List[TokenApiModel]
)

object GameApiModel {
  def apply(
    game: Game,
    playerId: PlayerId,
    gameStatus: GameStatus
  ): GameApiModel = {
    val board = for {
      x <- 0 to game.board.geometry.columns
      y <- 0 to game.board.geometry.rows
      token = game.board.tokens.get(Point(x, y))
      playerOwned = token.exists(_.owner == playerId)
      tokenType <- token.map { t => if (playerOwned) t.tokenType.name else "other" }
    } yield TokenApiModel(Point(x, y), tokenType, playerOwned)

    val (gameOver: Boolean, winnerId: Option[String]) = gameStatus match {
      case GameInProgress => (false, None)
      case s: GameOverStatus => (true, s.winnerId.map(_.value))
    }

    GameApiModel(
      game.id.value,
      playerId.value,
      game.playerList.filterNot(_.id == playerId).head.id.value,
      playerId == game.currentPlayer.id,
      gameOver,
      winnerId,
      game.board.geometry,
      board.toList
    )
  }
}

