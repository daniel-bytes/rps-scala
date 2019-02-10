package com.danielbytes.rps.services

import com.danielbytes.rps.engine._
import com.danielbytes.rps.model._
import cats.data.EitherT
import cats.implicits._
import scala.concurrent.{ ExecutionContext, Future }

/**
 * Result after a player's successful turn
 * @param game The game state result
 * @param status The game status
 */
case class PlayerTurnResult(
  game: Game,
  status: GameStatus
)

/**
 * Service for managing high level game mechanics
 */
trait GameService {
  type Result[T] = Future[Either[ApplicationError, T]]
  private type IntermediateResult[T] = EitherT[Future, ApplicationError, T]

  implicit def ec: ExecutionContext
  implicit def repository: Repository[GameId, Game]
  implicit def rules: GameRules
  implicit def aiRules: PlayerAIRules

  /**
   * Fetches a game by Id
   * @param gameId The game Id
   * @param playerId The current player Id
   * @return The game
   */
  def getGame(
    gameId: GameId,
    playerId: PlayerId
  ): Result[PlayerTurnResult] = {
    (for {
      fetchResult <- fetchGameFromRepository(gameId)
      validateResult <- validateGameOwner(fetchResult, playerId)
      gameStatus <- getGameStatus(validateResult)
    } yield PlayerTurnResult(validateResult, gameStatus)).value
  }

  /**
   * Deletes a game by Id
   * @param gameId The game Id
   * @param playerId The current player Id
   */
  def deleteGame(
    gameId: GameId,
    playerId: PlayerId
  ): Result[Unit] = {
    (for {
      game <- fetchGameFromRepository(gameId)
      _ <- validateGameOwner(game, playerId)
      _ <- deleteGameFromRepository(gameId)
    } yield ()).value
  }

  /**
   * Process a game turn for a player
   * @param gameId The current game Id
   * @param playerId The current player Id
   * @param from The point the player is moving from
   * @param to The point the player is moving to
   * @return The updated game
   */
  def processTurn(
    gameId: GameId,
    playerId: PlayerId,
    from: Point,
    to: Point
  ): Result[PlayerTurnResult] = {
    (for {
      fetchResult <- fetchGameFromRepository(gameId)
      processResult1 <- processTurn(fetchResult, playerId, from, to)
      gameStatus1 <- getGameStatus(processResult1)
      processResult2 <- processAITurn(processResult1, gameStatus1)
      gameStatus2 <- getGameStatus(processResult2)
      saveResult <- saveGameToRepository(gameId, processResult2)
    } yield PlayerTurnResult(saveResult, gameStatus2)).value
  }

  /**
   * Fetches the game state from the repository
   * @param gameId The id of the game
   * @return The game, or an error
   */
  private def fetchGameFromRepository(
    gameId: GameId
  ): IntermediateResult[Game] = EitherT[Future, ApplicationError, Game] {
    repository
      .get(gameId)
      .map(
        _.map(Right(_))
          .getOrElse(Left(GameNotFoundError))
      )
  }

  /**
   * Stores the game state to the repository
   * @param gameId The id of the game
   * @param game The game state
   * @return The saved game, or an error
   */
  private def saveGameToRepository(
    gameId: GameId,
    game: Game
  ): IntermediateResult[Game] = EitherT[Future, ApplicationError, Game] {
    repository
      .set(gameId, game)
      .map(_ => Right(game))
  }

  /**
   * Deletes a game in the repository
   * @param gameId The id of the game
   */
  private def deleteGameFromRepository(
    gameId: GameId
  ): IntermediateResult[Unit] = EitherT[Future, ApplicationError, Unit] {
    repository
      .remove(gameId)
      .map(_ => Right(()))
  }

  /**
   * Asserts the requested player can access the game
   * @param game The game state
   * @param playerId The player Id
   * @return The game, or an error
   */
  private def validateGameOwner(
    game: Game,
    playerId: PlayerId
  ): IntermediateResult[Game] = EitherT[Future, ApplicationError, Game] {
    Future {
      if (game.playerList.map(_.id).contains(playerId)) {
        Right(game)
      } else {
        Left(GameNotFoundError)
      }
    }
  }

  /**
   * Process a game turn for a player
   * @param game The current game state
   * @param playerId The current player Id
   * @param from The point the player is moving from
   * @param to The point the player is moving to
   * @return The updated game, or an error
   */
  private def processTurn(
    game: Game,
    playerId: PlayerId,
    from: Point,
    to: Point
  ): IntermediateResult[Game] = EitherT[Future, ApplicationError, Game] {
    Future {
      rules.gameTurn(game, playerId, from, to)
    }
  }

  /**
   * Process an AI turn if the current player is AI
   * @param game The current game state
   * @param status The game status
   * @return The original or updated game status
   */
  private def processAITurn(
    game: Game,
    status: GameStatus
  ): IntermediateResult[Game] = EitherT[Future, ApplicationError, Game] {
    Future {
      status match {
        case GameInProgress if game.currentPlayer.isAI => {
          aiRules
            .computeMove(game, game.currentPlayerId)
            .flatMap(r => rules.gameTurn(game, game.currentPlayerId, r.from, r.to))
        }
        case GameInProgress => Right(game)
        case _: GameOverStatus => Right(game)
      }
    }
  }

  /**
   * Calculates the game status from the game state
   * @param game The current game state
   * @return The game status
   */
  private def getGameStatus(
    game: Game
  ): IntermediateResult[GameStatus] = EitherT[Future, ApplicationError, GameStatus] {
    Future {
      rules.gameStatus(game)
    }
  }
}

class GameServiceImpl()(
    implicit
    val ec: ExecutionContext,
    val repository: Repository[GameId, Game]
) extends GameService {
  implicit def rules: GameRules = GameRulesEngine
  implicit def aiRules: PlayerAIRules = PlayerAIRulesEngine
}