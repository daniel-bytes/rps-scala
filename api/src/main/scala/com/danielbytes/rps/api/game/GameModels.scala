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

case class GameOverviewApiModel(
  id: String,
  playerId: String,
  playerName: String,
  otherPlayerName: String,
  isGameOver: Boolean,
  winnerName: Option[String]
)

object GameOverviewApiModel {
  def apply(userId: UserId, model: GameWithStatus): GameOverviewApiModel = {
    val player = model.game.player(userId).getOrElse(throw IncorrectPlayerException())
    val other = model.game.notPlayer(userId)

    val (gameOver: Boolean, winnerName: Option[String]) = model.status match {
      case GameInProgress => (false, None)
      case s: GameOverStatus => (true, s.winnerId.flatMap(id => model.game.player(id)).map(_.user.name.value))
    }

    GameOverviewApiModel(
      model.game.id.value,
      player.id.value,
      player.user.name.value,
      other.user.name.value,
      gameOver,
      winnerName
    )
  }
}

case class GameOverviewsApiModel(
  games: List[GameOverviewApiModel]
)

case class GameApiModel(
  gameId: String,
  playerId: String,
  playerName: String,
  otherPlayerName: String,
  isPlayerTurn: Boolean,
  isGameOver: Boolean,
  winnerName: Option[String],
  board: Geometry,
  tokens: List[TokenApiModel]
)

object GameApiModel {
  def apply(
    game: Game,
    userId: UserId,
    gameStatus: GameStatus
  ): GameApiModel = {
    val board = for {
      x <- 0 to game.board.geometry.columns
      y <- 0 to game.board.geometry.rows
      token = game.board.tokens.get(Point(x, y))
      playerOwned = token.exists(_.owner == userId)
      tokenType <- token.map { t => if (playerOwned) t.tokenType.name else "other" }
    } yield TokenApiModel(Point(x, y), tokenType, playerOwned)

    val (isGameOver: Boolean, winnerId: Option[UserId]) = gameStatus match {
      case GameInProgress => (false, None)
      case s: GameOverStatus => (true, s.winnerId)
    }

    val player = game.player(userId).getOrElse(throw IncorrectPlayerException())
    val otherPlayer = game.notPlayer(userId)
    val maybeWinningPlayer = winnerId.map(id => game.player(id).getOrElse(throw IncorrectPlayerException()))
    val isPlayerTurn = /*if (game.playerList.exists(_.isAI)) true else*/ userId == game.currentPlayer.id

    GameApiModel(
      gameId = game.id.value,
      playerId = player.id.value,
      playerName = player.name.value,
      otherPlayerName = otherPlayer.name.value,
      isPlayerTurn = isPlayerTurn,
      isGameOver = isGameOver,
      winnerName = maybeWinningPlayer.map(_.name.value),
      board = game.board.geometry,
      tokens = board.toList
    )
  }
}

