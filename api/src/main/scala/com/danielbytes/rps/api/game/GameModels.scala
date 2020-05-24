package com.danielbytes.rps.api.game

import com.danielbytes.rps.model._

case class GameMoveApiModel(
  from: Point,
  to: Point,
  version: Int
)

case class CombatSummaryApiModel(
  attackerTokenType: String,
  defenderTokenType: String,
  winnerTokenType: Option[String]
)

case class GameMoveSummaryApiModel(
  playerId: String,
  from: Point,
  to: Point,
  combatSummary: Option[CombatSummaryApiModel]
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
  tokens: List[TokenApiModel],
  recentMoves: List[GameMoveSummaryApiModel],
  version: Int
)

object GameApiModel {

  def apply(
    game: GameWithStatus,
    userId: UserId
  ): GameApiModel = {
    val board = for {
      x <- 0 to game.game.board.geometry.columns
      y <- 0 to game.game.board.geometry.rows
      token = game.game.board.tokens.get(Point(x, y))
      playerOwned = token.exists(_.owner == userId)
      tokenType <- token.map { t => if (playerOwned) t.tokenType.name else Token.other }
    } yield TokenApiModel(Point(x, y), tokenType, playerOwned)

    val (isGameOver: Boolean, winnerId: Option[UserId]) = game.status match {
      case GameInProgress => (false, None)
      case s: GameOverStatus => (true, s.winnerId)
    }

    val player = game.game.player(userId).getOrElse(throw IncorrectPlayerException())
    val otherPlayer = game.game.notPlayer(userId)
    val maybeWinningPlayer = winnerId.map(id => game.game.player(id).getOrElse(throw IncorrectPlayerException()))
    val isPlayerTurn = userId == game.game.currentPlayer.id

    val recentMoves = game.moves.map(move =>
      GameMoveSummaryApiModel(
        move.playerId.value,
        move.from,
        move.to,
        move.combatResult.map(result =>
          CombatSummaryApiModel(
            attackerTokenType = result.attacker.tokenType.name,
            defenderTokenType = result.defender.tokenType.name,
            winnerTokenType = result.winner.map(_.tokenType.name)
          ))
      ))

    GameApiModel(
      gameId = game.game.id.value,
      playerId = player.id.value,
      playerName = player.name.value,
      otherPlayerName = otherPlayer.name.value,
      isPlayerTurn = isPlayerTurn,
      isGameOver = isGameOver,
      winnerName = maybeWinningPlayer.map(_.name.value),
      board = game.game.board.geometry,
      tokens = board.toList,
      recentMoves = recentMoves,
      version = game.game.version.value
    )
  }
}
