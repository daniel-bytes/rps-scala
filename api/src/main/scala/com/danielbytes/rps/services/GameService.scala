package com.danielbytes.rps.services

import com.danielbytes.rps.rules._
import com.danielbytes.rps.model._
import cats.data.EitherT
import cats.implicits._
import com.danielbytes.rps.helpers.RandomHelper
import com.danielbytes.rps.services.repositories.GameRepository

import scala.concurrent.{ ExecutionContext, Future }

/**
 * Service for managing high level game mechanics
 */
trait GameService {
  type Result[T] = Future[Either[ApplicationError, T]]
  private type IntermediateResult[T] = EitherT[Future, ApplicationError, T]

  implicit def ec: ExecutionContext
  implicit def gameRepository: GameRepository
  implicit def gameRules: GameRules
  implicit def aiRules: PlayerAIRules
  implicit def boardRules: BoardRules
  implicit def random: RandomHelper

  /**
   * Fetches all games for a player
   * @param userId The player Id
   * @param includeCompleted If true completed games are also returned
   * @return The list of games
   */
  def getPlayerGames(
    userId: UserId,
    includeCompleted: Boolean
  ): Result[List[GameWithStatus]] = {
    for {
      playerGames <- gameRepository.playerGames(userId)

      results = playerGames
        .map { game =>
          (game, gameRules.gameStatus(game))
        }
        .collect {
          case (game, Right(status)) if includeCompleted =>
            GameWithStatus(game, status)
          case (game, Right(GameInProgress)) =>
            GameWithStatus(game, GameInProgress)
        }
    } yield Right(results)
  }

  /**
   * Fetches a game by Id
   * @param gameId The game Id
   * @param userId The current player Id
   * @return The game
   */
  def getGame(
    gameId: GameId,
    userId: UserId
  ): Result[GameWithStatus] = {
    (for {
      fetchResult <- fetchGameFromRepository(gameId)
      validateResult <- validateGameOwner(fetchResult, userId)
      gameStatus <- getGameStatus(validateResult)
    } yield GameWithStatus(validateResult, gameStatus)).value
  }

  /**
   * Creates a new single player game
   * @param user The player Id
   * @return The game
   */
  def createGame(
    user: User
  ): Result[GameWithStatus] = {
    val player = Player(user, StartPositionBottom)
    val game = boardRules.generateRandomSinglePlayerGame(player)

    saveGameToRepository(game.id, game)
      .map(g => GameWithStatus(g, GameInProgress))
      .value
  }

  /**
   * Saves a game
   * @param game The game to save
   * @return The saved game
   */
  def saveGame(
    game: Game
  ): Result[Game] = {
    saveGameToRepository(game.id, game).value
  }

  /**
   * Deletes a game by Id
   * @param gameId The game Id
   * @param userId The current player Id
   */
  def deleteGame(
    gameId: GameId,
    userId: UserId
  ): Result[Unit] = {
    (for {
      game <- fetchGameFromRepository(gameId)
      _ <- validateGameOwner(game, userId)
      _ <- deleteGameFromRepository(gameId)
    } yield ()).value
  }

  /**
   * Process a game turn for a player
   * @param gameId The current game Id
   * @param userId The current player Id
   * @param from The point the player is moving from
   * @param to The point the player is moving to
   * @return The updated game
   */
  def processTurn(
    gameId: GameId,
    userId: UserId,
    from: Point,
    to: Point
  ): Result[GameWithStatus] = {
    (for {
      fetchResult <- fetchGameFromRepository(gameId)
      processResult1 <- processTurn(fetchResult, userId, from, to)
      gameStatus1 <- getGameStatus(processResult1)
      processResult2 <- processAITurn(processResult1, gameStatus1)
      gameStatus2 <- getGameStatus(processResult2)
      saveResult <- saveGameToRepository(gameId, processResult2)
    } yield GameWithStatus(saveResult, gameStatus2)).value
  }

  /**
   * Fetches the game state from the repository
   * @param gameId The id of the game
   * @return The game, or an error
   */
  private def fetchGameFromRepository(
    gameId: GameId
  ): IntermediateResult[Game] = EitherT[Future, ApplicationError, Game] {
    gameRepository
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
    gameRepository
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
    gameRepository
      .remove(gameId)
      .map(_ => Right(()))
  }

  /**
   * Asserts the requested player can access the game
   * @param game The game state
   * @param userId The player Id
   * @return The game, or an error
   */
  private def validateGameOwner(
    game: Game,
    userId: UserId
  ): IntermediateResult[Game] = EitherT[Future, ApplicationError, Game] {
    Future.successful {
      if (game.playerList.map(_.id).contains(userId)) {
        Right(game)
      } else {
        Left(GameNotFoundError)
      }
    }
  }

  /**
   * Process a game turn for a player
   * @param game The current game state
   * @param userId The current player Id
   * @param from The point the player is moving from
   * @param to The point the player is moving to
   * @return The updated game, or an error
   */
  private def processTurn(
    game: Game,
    userId: UserId,
    from: Point,
    to: Point
  ): IntermediateResult[Game] = EitherT[Future, ApplicationError, Game] {
    Future.successful {
      gameRules.gameTurn(game, userId, from, to)
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
    Future.successful {
      status match {
        case GameInProgress if game.currentPlayer.isAI => {
          aiRules
            .computeMove(game, game.currentPlayerId)
            .flatMap(r => gameRules.gameTurn(game, game.currentPlayerId, r.from, r.to))
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
    Future.successful {
      gameRules.gameStatus(game)
    }
  }
}

class GameServiceImpl()(
  implicit
  val ec: ExecutionContext,
  val gameRepository: GameRepository,
  val gameRules: GameRules,
  val aiRules: PlayerAIRules,
  val boardRules: BoardRules,
  val random: RandomHelper
) extends GameService {}