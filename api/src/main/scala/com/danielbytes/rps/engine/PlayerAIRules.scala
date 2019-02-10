package com.danielbytes.rps.engine

import com.danielbytes.rps.model._

/**
 * Trait that defines the computer player's AI rules.
 */
trait PlayerAIRules {
  implicit def moveRules: MoveRules

  /**
   * A player takes a turn in the game
   *
   * @param game The game state
   * @param playerId The player taking a turn
   * @return The computed move to make
   */
  def computeMove(
    game: Game,
    playerId: PlayerId
  ): Either[RuleViolationError, MoveResult] = {
    for {
      moves <- computePossibleMoves(game, playerId)
      move <- computeMove(game, playerId, moves)
    } yield move
  }

  /**
   * Computes the next AI move from all possible moves
   *
   * @param game The game state
   * @param playerId The player taking a turn
   * @param moves All possible moves
   * @return The computed move
   */
  private def computeMove(
    game: Game,
    playerId: PlayerId,
    moves: Set[MoveResult]
  ): Either[RuleViolationError, MoveResult] = {
    moves.toList.sortBy {
      case AttackMove(_, _, _, _, _) => -2
      case TakePositionMove(_, _, direction) => direction match {
        case MoveForward => -1
        case _ => 1
      }
    }
      .headOption
      .map(Right(_))
      .getOrElse(Left(NoMovableTokens))
  }

  /**
   * Computes all possible moves a player can make
   *
   * @param game The game state
   * @param playerId The player taking a turn
   * @return All possible moves
   */
  private[engine] def computePossibleMoves(
    game: Game,
    playerId: PlayerId
  ): Either[RuleViolationError, Set[MoveResult]] = {
    val movableTokens = game
      .board
      .playerTokens(playerId)
      .collect { case t if t.movable => t }
      .toSet

    val moves = game
      .board
      .tokens
      .filter {
        case (_, point) => movableTokens.contains(point)
      }
      .foldLeft(Set[(Point, Point)]()) {
        case (result, (point, _)) =>
          computePoints(point, game.board.geometry) ++ result
      }
      .map {
        case (from, to) =>
          moveRules.moveToken(game, playerId, from, to)
      }
      .collect {
        case Right(result) => result
      }

    Right(moves)
  }

  /**
   * Computes all possible moves for a Point based on the current board Geometry
   *
   * @param point The Point to move
   * @param geometry The board Geometry
   * @return The possible Points to move to
   */
  private def computePoints(
    point: Point,
    geometry: Geometry
  ): Set[(Point, Point)] = {
    Set(
      point -> point.copy(x = point.x + 1),
      point -> point.copy(x = point.x - 1),
      point -> point.copy(y = point.y + 1),
      point -> point.copy(y = point.y - 1)
    ).filter(pts => geometry.contains(pts._2))
  }
}

object PlayerAIRulesEngine extends PlayerAIRules {
  implicit def moveRules: MoveRules = MoveRulesEngine
}
