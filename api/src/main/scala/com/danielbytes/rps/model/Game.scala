package com.danielbytes.rps.model

sealed trait GameStatus

case object GameInProgress extends GameStatus

sealed trait GameOverStatus extends GameStatus {
  def winnerId: Option[PlayerId]
}

case object GameOverStalemate extends GameOverStatus {
  def winnerId: Option[PlayerId] = None
}

case class GameOverFlagCaptured(winner: PlayerId) extends GameOverStatus {
  def winnerId: Option[PlayerId] = Some(winner)
}

case class GameOverNoMoreTokens(winner: PlayerId) extends GameOverStatus {
  def winnerId: Option[PlayerId] = Some(winner)
}

case class Board(
    geometry: Geometry,
    tokens: Map[Point, Token]
) {
  def playerTokens(playerId: PlayerId): List[Token] = {
    tokens.values.filter(_.owner == playerId).toList
  }
}

case class GameId(value: String) extends AnyVal

case class Game(
    id: GameId,
    player1: Player,
    player2: Player,
    currentPlayerId: PlayerId,
    board: Board
) {
  val playerList = List(player1, player2)

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

  def withCurrentPlayer(playerId: PlayerId): Game = {
    this.copy(currentPlayerId = playerId)
  }
}
