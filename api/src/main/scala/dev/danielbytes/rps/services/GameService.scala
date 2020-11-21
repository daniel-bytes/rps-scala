package dev.danielbytes.rps.services

import dev.danielbytes.rps.model._
import cats.data.EitherT
import cats.implicits._
import dev.danielbytes.rps.services.repositories.GameRepository
import dev.danielbytes.rps.model.{ ApplicationError, GameNotFoundError, Point, StartPositionBottom, VersionConflictError }
import dev.danielbytes.rps.rules.{ BoardRules, GameRules, PlayerAIRules }

import scala.concurrent.{ ExecutionContext, Future }

/**
 * Service for managing high level game mechanics
 */
trait GameService {
  type Result[T] = Future[Either[ApplicationError, T]]
  private type IntermediateResult[T] = EitherT[Future, ApplicationError, T]

  implicit def ec: ExecutionContext

  def gameRepository: GameRepository

  def gameRules: GameRules

  def aiRules: PlayerAIRules

  def boardRules: BoardRules

  /**
   * Fetches all games for a player
   *
   * @param userId The player Id
   * @param includeCompleted If true completed games are also returned
   * @return The list of games
   */
  def getPlayerGames(userId: UserId, includeCompleted: Boolean): Result[List[GameWithStatus]] = {
    for {
      playerGames <- gameRepository.listPlayerGames(userId)

      results = playerGames
        .map { game =>
          (game, gameRules.gameStatus(game))
        }
        .collect {
          case (game, Right(status)) if includeCompleted =>
            GameWithStatus(game, Nil, status)
          case (game, Right(GameInProgress)) =>
            GameWithStatus(game, Nil, GameInProgress)
        }
    } yield Right(results)
  }

  /**
   * Fetches a game by Id
   *
   * @param gameId The game Id
   * @param userId The current player Id
   * @return The game
   */
  def getGame(gameId: GameId, userId: UserId): Result[GameWithStatus] = {
    (for {
      fetchResult <- fetchGameFromRepository(gameId)
      validateResult <- validateGameOwner(fetchResult, userId)
      gameStatus <- getGameStatus(validateResult)
    } yield GameWithStatus(validateResult, Nil, gameStatus)).value
  }

  /**
   * Creates a new single player game
   *
   * @param user The player Id
   * @return The game
   */
  def createGame(user: User): Result[GameWithStatus] = {
    val player = Player(user, StartPositionBottom)
    val game = boardRules.generateRandomSinglePlayerGame(player)

    saveGameToRepository(game.id, game)
      .map(g => GameWithStatus(g, Nil, GameInProgress))
      .value
  }

  /**
   * Deletes a game by Id
   *
   * @param gameId The game Id
   * @param userId The current player Id
   */
  def deleteGame(gameId: GameId, userId: UserId): Result[Unit] = {
    (for {
      game <- fetchGameFromRepository(gameId)
      _ <- validateGameOwner(game, userId)
      _ <- deleteGameFromRepository(gameId)
    } yield ()).value
  }

  /**
   * Process a game turn for a player
   *
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
    to: Point,
    version: GameVersion): Result[GameWithStatus] = {
    (for {
      fetchResult <- fetchGameFromRepository(gameId)
      validGame <- validateVersion(fetchResult, version)
      processResult1 <- processTurn(validGame, userId, from, to)
      gameStatus1 <- getGameStatus(processResult1.game)
      processResult2 <- processAITurn(processResult1.game, gameStatus1)
      gameStatus2 <- getGameStatus(processResult2.game)
      saveResult <- saveGameToRepository(gameId, processResult2.game.incrementVersion())
    } yield GameWithStatus(saveResult, List(processResult1.move, processResult2.move).flatten, gameStatus2)).value
  }

  /**
   * Fetches the game state from the repository
   *
   * @param game The game
   * @param version The expected version
   * @return The game, or an error
   */
  private def validateVersion(game: Game, version: GameVersion): IntermediateResult[Game] =
    EitherT[Future, ApplicationError, Game] {
      Future.successful {
        if (game.version == version) Right(game)
        else Left(VersionConflictError)
      }
    }

  /**
   * Fetches the game state from the repository
   *
   * @param gameId The id of the game
   * @return The game, or an error
   */
  private def fetchGameFromRepository(gameId: GameId): IntermediateResult[Game] =
    EitherT[Future, ApplicationError, Game] {
      gameRepository
        .get(gameId)
        .map(
          _.map(Right(_))
            .getOrElse(Left(GameNotFoundError)))
    }

  /**
   * Stores the game state to the repository
   *
   * @param gameId The id of the game
   * @param game The game state
   * @return The saved game, or an error
   */
  private def saveGameToRepository(gameId: GameId, game: Game): IntermediateResult[Game] =
    EitherT[Future, ApplicationError, Game] {
      gameRepository
        .set(gameId, game)
        .map(_ => Right(game))
    }

  /**
   * Deletes a game in the repository
   *
   * @param gameId The id of the game
   */
  private def deleteGameFromRepository(gameId: GameId): IntermediateResult[Unit] =
    EitherT[Future, ApplicationError, Unit] {
      gameRepository
        .remove(gameId)
        .map(_ => Right(()))
    }

  /**
   * Asserts the requested player can access the game
   *
   * @param game The game state
   * @param userId The player Id
   * @return The game, or an error
   */
  private def validateGameOwner(game: Game, userId: UserId): IntermediateResult[Game] =
    EitherT[Future, ApplicationError, Game] {
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
   *
   * @param game The current game state
   * @param userId The current player Id
   * @param from The point the player is moving from
   * @param to The point the player is moving to
   * @return The updated game, or an error
   */
  private def processTurn(game: Game, userId: UserId, from: Point, to: Point): IntermediateResult[GameWithMoveSummary] =
    EitherT[Future, ApplicationError, GameWithMoveSummary] {
      Future.successful {
        gameRules.gameTurn(game, userId, from, to)
      }
    }

  /**
   * Process an AI turn if the current player is AI
   *
   * @param game The current game state
   * @param status The game status
   * @return The original or updated game status
   */
  private def processAITurn(game: Game, status: GameStatus): IntermediateResult[GameWithMoveSummary] =
    EitherT[Future, ApplicationError, GameWithMoveSummary] {
      Future.successful {
        status match {
          case GameInProgress if game.currentPlayer.isAI => {
            aiRules
              .computeMove(game, game.currentPlayerId)
              .flatMap(r => gameRules.gameTurn(game, game.currentPlayerId, r.from, r.to))
          }
          case GameInProgress => Right(GameWithMoveSummary(game, None))
          case _: GameOverStatus => Right(GameWithMoveSummary(game, None))
        }
      }
    }

  /**
   * Calculates the game status from the game state
   *
   * @param game The current game state
   * @return The game status
   */
  private def getGameStatus(game: Game): IntermediateResult[GameStatus] =
    EitherT[Future, ApplicationError, GameStatus] {
      Future.successful {
        gameRules.gameStatus(game)
      }
    }
}

object GameService {

  /**
   * Default implementation of Game service
   */
  class Impl(
    val gameRepository: GameRepository,
    val gameRules: GameRules,
    val aiRules: PlayerAIRules,
    val boardRules: BoardRules)(implicit val ec: ExecutionContext)
    extends GameService {}

}
