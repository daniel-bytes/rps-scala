package com.danielbytes.rps.engine

import com.danielbytes.rps.model._

/**
 * Trait that defines the token move rules engine.
 */
trait MoveRules {
  /**
   * Move a token from one point to another
   * @param game The game
   * @param playerId The player taking a turn
   * @param from The point where the token is being moved from
   * @param to The point where the token is being moved to
   * @return The result of the move:
   *         Either a player takes a position, attacks the other player
   *         or an error condition is returned
   */
  def moveToken(
    game: Game,
    playerId: PlayerId,
    from: Point,
    to: Point
  ): Either[RuleViolationError, MoveResult] = {
    for {
      _ <- validatePlayer(game, playerId)
      source <- validateSourceToken(game, from)
      dest <- validateDestinationToken(game, from, to)
      result <- validateMove(game, from, source, to, dest)
    } yield result
  }

  /**
   * Validates that the player taking a turn is valid
   * @param game The current game state
   * @param playerId The player taking a turn
   * @return The validation result
   */
  private def validatePlayer(
    game: Game,
    playerId: PlayerId
  ): Either[RuleViolationError, Unit] = {
    if (game.currentPlayer.id == playerId) {
      Right(())
    } else {
      Left(WrongPlayerTurnError)
    }
  }

  /**
   * Validates that the source (from) token is valid
   * @param game The current game state
   * @param from The point to move
   * @return The token to move, or an error
   */
  private def validateSourceToken(
    game: Game,
    from: Point
  ): Either[RuleViolationError, Token] = {
    game.board.tokens.get(from) match {
      case Some(t) if t.owner != game.currentPlayerId => Left(OtherPlayersTokenError)
      case Some(t) if t.movable => Right(t)
      case Some(_) => Left(NotAMovableTokenError)
      case None => Left(NotATokenError)
    }
  }

  /**
   * Validates that the destination (to) token is valid
   * @param game The current game state
   * @param from The point to move from
   * @param to The point to move to
   * @return The token at the destination, or an error
   */
  private def validateDestinationToken(
    game: Game,
    from: Point,
    to: Point
  ): Either[RuleViolationError, Option[Token]] = {
    // Can only move a single point away up, down, left or right
    if (math.abs(from.x - to.x) + math.abs(from.y - to.y) != 1) {
      Left(MoveIsTooFarError)
    } else {
      game.board.tokens.get(to) match {
        case Some(other) if other.owner == game.currentPlayerId => Left(CannotAttackYourOwnTokenError)
        case Some(other) => Right(Some(other))
        case None => Right(None)
      }
    }
  }

  /**
   * Validates the game move
   * @param game The current game state
   * @param from The point to move from
   * @param source The player token that is moving
   * @param to The point to move to
   * @param dest The destination token
   * @return The move result or an error
   */
  private def validateMove(
    game: Game,
    from: Point,
    source: Token,
    to: Point,
    dest: Option[Token]
  ): Either[RuleViolationError, MoveResult] = {
    for {
      direction <- calculateDirection(from, to, game.currentPlayer)
      result <- dest match {
        case Some(dest) => Right(AttackMove(from, to, direction, source, dest))
        case None => Right(TakePositionMove(from, to, direction))
      }
    } yield result
  }

  /**
   * Calculates the direction a token was moved
   * @param game The current game state
   * @param from The point to move from
   * @param player The player moving the token
   * @return The move direction or an error
   */
  private def calculateDirection(
    from: Point,
    to: Point,
    player: Player
  ): Either[RuleViolationError, MoveDirection] = {
    (from.x - to.x, from.y - to.y, player.position) match {
      case (-1, 0, _) => Right(MoveRight)
      case (1, 0, _) => Right(MoveLeft)
      case (0, -1, StartPositionTop) => Right(MoveReverse)
      case (0, -1, StartPositionBottom) => Right(MoveForward)
      case (0, 1, StartPositionTop) => Right(MoveForward)
      case (0, 1, StartPositionBottom) => Right(MoveReverse)
      case _ => Left(MoveIsTooFarError)
    }
  }
}

object MoveRulesEngine extends MoveRules

