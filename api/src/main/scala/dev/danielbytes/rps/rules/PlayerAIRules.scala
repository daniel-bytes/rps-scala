package dev.danielbytes.rps.rules

import dev.danielbytes.rps.model._
import dev.danielbytes.rps.helpers.RandomHelper

/**
 * Trait that defines the computer player's AI rules.
 */
trait PlayerAIRules extends RandomHelper {

  /**
   * Moves rule engine
   */
  def moveRules: MoveRules

  /**
   * A player takes a turn in the game
   *
   * @param game The game state
   * @param userId The player taking a turn
   * @return The computed move to make
   */
  def computeMove(game: Game, userId: UserId): Either[RuleViolationError, PotentialMove] = {
    for {
      moves <- computePossibleMoves(game, userId)
      move <- computeMove(game, userId, moves)
    } yield move
  }

  /**
   * Computes the next AI move from all possible moves
   *
   * @param game The game state
   * @param userId The player taking a turn
   * @param moves All possible moves
   * @return The computed move
   */
  private def computeMove(
    game: Game,
    userId: UserId,
    moves: Set[PotentialMove]): Either[RuleViolationError, PotentialMove] = {
    if (moves.nonEmpty) {
      val sortedMoves = sortMovesAggressive(moves)
      val index = getMoveIndex(sortedMoves.length, randomFactor = .2)

      Right(sortedMoves(index))
    } else {
      Left(NoMovableTokens)
    }
  }

  /**
   * Sorts a set of potential moves, with attack moves having the highest possible ranking
   * and favoring moves closer to the other player's side of the board.
   * @param moves The set of moves
   * @return The sorted array of moves
   */
  private def sortMovesAggressive(moves: Set[PotentialMove]): Array[PotentialMove] =
    moves.toArray.sortBy {
      case m: AttackMove => 100 + m.distance.value
      case TakePositionMove(_, _, MoveForward, distance) => 50 + distance.value
      case m => m.distance.value
    }.reverse

  /**
   * Calculate move index using a random factor.
   * Assumes index 0 is the best possible move according for the chosen algorithm.
   * @param numMoves Number of moves available
   * @param randomFactor Percent chance (0.0 to 1.0) that a move will be random.
   * @return The move index value
   */
  private def getMoveIndex(numMoves: Int, randomFactor: Double): Int =
    nextDouble() match {
      case rnd if (numMoves < 2) || (rnd >= randomFactor) => 0
      case _ => ((numMoves - 1) * nextDouble()).toInt
    }

  /**
   * Computes all possible moves a player can make
   *
   * @param game The game state
   * @param userId The player taking a turn
   * @return All possible moves
   */
  private[rules] def computePossibleMoves(
    game: Game,
    userId: UserId): Either[RuleViolationError, Set[PotentialMove]] = {
    val movableTokens = game.board
      .playerTokens(userId)
      .collect { case t if t.movable => t }
      .toSet

    val moves = game.board.tokens
      .filter {
        case (_, point) => movableTokens.contains(point)
      }
      .foldLeft(Set[(Point, Point)]()) {
        case (result, (point, _)) =>
          computePoints(point, game.board.geometry) ++ result
      }
      .map {
        case (from, to) =>
          moveRules.moveToken(game, userId, from, to)
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
  private def computePoints(point: Point, geometry: Geometry): Set[(Point, Point)] = {
    Set(
      point -> point.copy(x = point.x + 1),
      point -> point.copy(x = point.x - 1),
      point -> point.copy(y = point.y + 1),
      point -> point.copy(y = point.y - 1)).filter(pts => geometry.contains(pts._2))
  }
}

object PlayerAIRules {
  /**
   * Default implementation of Player AI rules engine
   */
  class Impl(val moveRules: MoveRules) extends PlayerAIRules
}
