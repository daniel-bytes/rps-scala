package com.danielbytes.rps.model

sealed trait GameStatus

case object GameInProgress extends GameStatus

sealed trait GameOverStatus extends GameStatus {
  def winnerId: Option[UserId]
}

case object GameOverStalemate extends GameOverStatus {
  def winnerId: Option[UserId] = None
}

case class GameOverFlagCaptured(winner: UserId) extends GameOverStatus {
  def winnerId: Option[UserId] = Some(winner)
}

case class GameOverNoMoreTokens(winner: UserId) extends GameOverStatus {
  def winnerId: Option[UserId] = Some(winner)
}

case class Board(
    geometry: Geometry,
    tokens: Map[Point, Token]
) {
  def playerTokens(userId: UserId): List[Token] = {
    tokens.values.filter(_.owner == userId).toList
  }
}

case class GameId(value: String) extends AnyVal

case class Game(
    id: GameId,
    player1: Player,
    player2: Player,
    currentPlayerId: UserId,
    board: Board
) {
  val playerList = List(player1, player2)

  // players that are not AI
  lazy val userList = playerList.filterNot(_.isAI)

  if (!playerList.exists(_.id == currentPlayerId)) {
    throw new IllegalStateException(s"Invalid currentPlayerId [$currentPlayerId]")
  }

  def currentPlayer: Player = {
    playerList
      .find(_.id == currentPlayerId)
      .head
  }

  def otherPlayer: Player = {
    playerList
      .find(_.id != currentPlayerId)
      .head
  }

  def player(id: UserId): Option[Player] = {
    playerList
      .find(_.id == id)
  }

  def notPlayer(currentId: UserId): Player = {
    playerList
      .find(_.id != currentId)
      .head
  }

  def withCurrentPlayer(userId: UserId): Game = {
    this.copy(currentPlayerId = userId)
  }
}

case class GameWithMoveSummary(
  game: Game,
  move: Option[MoveSummary]
)

/**
 * A game along with it's current calculated status and recent moves
 * @param game The game state result
 * @param moves The game moves token
 * @param status The game status
 */
case class GameWithStatus(
  game: Game,
  moves: List[MoveSummary],
  status: GameStatus
)