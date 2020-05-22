package com.danielbytes.rps.services

import com.danielbytes.rps.helpers.Helpers
import com.danielbytes.rps.{ GameTestData, TestUtils }
import com.danielbytes.rps.model._
import com.danielbytes.rps.rules.Rules
import com.danielbytes.rps.services.repositories.InMemoryGameRepository
import org.scalatest._

import scala.concurrent.ExecutionContext

class GameServiceSpec
    extends WordSpec
    with Matchers
    with GameTestData
    with TestUtils
    with Helpers
    with Rules {
  implicit val ec: ExecutionContext = ExecutionContext.global

  def gameService(
    games: List[Game] = Nil
  ): GameService = {
    val gameRepo = new InMemoryGameRepository()
    games.foreach(g => gameRepo.set(g.id, g))

    new GameServiceImpl(gameRepo, gameRules, aiRules, boardRules, random)(ec)
  }

  "GameService" should {
    "handle getPlayerGames" should {
      "return a players games" in {
        wait(gameService(List(game, completedGame)).getPlayerGames(pid1, includeCompleted = false)) should ===(
          Right(
            List(
              GameWithStatus(
                game,
                Nil,
                GameInProgress
              )
            )
          )
        )
      }

      "return completed games" in {
        wait(gameService(List(game, completedGame)).getPlayerGames(pid1, includeCompleted = true)) should ===(
          Right(
            List(
              GameWithStatus(
                game,
                Nil,
                GameInProgress
              ),
              GameWithStatus(
                completedGame,
                Nil,
                GameOverFlagCaptured(pid1)
              )
            )
          )
        )
      }

      "return nothing for an unknown player" in {
        wait(gameService(List(game, completedGame)).getPlayerGames(UserId("baduser"), includeCompleted = false)) should ===(
          Right(
            List()
          )
        )
      }
    }

    "handle processTurn" should {
      "allow a token move" in {
        wait(gameService(List(game)).processTurn(gid, pid1, Point(0, 1), Point(1, 1))) should ===(
          Right(
            GameWithStatus(
              game.copy(
                currentPlayerId = pid2,
                board = game.board.copy(
                  tokens = Map( /*
                          ----------------
                      y2  | S2 |    | F2 |
                          ----------------
                      y1  |  --> R1 |    |
                          ----------------
                      y0  | F1 |    |    |
                          ----------------
                            x0   x1   x2   */
                    Point(0, 0) -> Token(pid1, Flag),
                    Point(1, 1) -> Token(pid1, Rock),
                    Point(0, 2) -> Token(pid2, Scissor),
                    Point(2, 2) -> Token(pid2, Flag)
                  )
                )
              ),
              List(MoveSummary(pid1, Point(0, 1), Point(1, 1), None)),
              GameInProgress
            )
          )
        )
      }

      "allow a token move followed by an AI move" in {
        wait(gameService(List(gameWithAI)).processTurn(gid, pid1, Point(0, 1), Point(1, 1))) should ===(
          Right(
            GameWithStatus(
              gameWithAI.copy(
                board = game.board.copy(
                  tokens = Map( /*
                          ----------------
                      y2  |    |    | F2 |
                          ----------------
                      y1  | S2 | R1 |    |
                          ----------------
                      y0  | F1 |    |    |
                          ----------------
                            x0   x1   x2   */
                    Point(0, 0) -> Token(pid1, Flag),
                    Point(1, 1) -> Token(pid1, Rock),
                    Point(0, 1) -> Token(pid2, Scissor),
                    Point(2, 2) -> Token(pid2, Flag)
                  )
                ),
                currentPlayerId = pid1
              ),
              List(
                MoveSummary(pid1, Point(0, 1), Point(1, 1), None),
                MoveSummary(pid2, Point(0, 2), Point(0, 1), None)
              ),
              GameInProgress
            )
          )
        )
      }

      "AI move winning an attack correctly sets currentPlayerId to human player" in {
        val newGame = gameWithAI.copy(
          board = gameWithAI.board.copy(
            tokens = Map( /*
                        ----------------
                    y2  | S2 |    | F2 |
                        ----------------
                    y1  | P1 | S1 |    |
                        ----------------
                    y0  | F1 |    |    |
                        ----------------
                          x0   x1   x2   */

              Point(0, 0) -> Token(pid1, Flag),
              Point(0, 1) -> Token(pid1, Paper),
              Point(1, 1) -> Token(pid1, Scissor),
              Point(0, 2) -> Token(pid2, Scissor),
              Point(2, 2) -> Token(pid2, Flag)
            )
          )
        )
        wait(gameService(List(newGame)).processTurn(gid, pid1, Point(1, 1), Point(2, 1))) should ===(
          Right(
            GameWithStatus(
              newGame.copy(
                board = game.board.copy(
                  tokens = Map( /*
                        ---------------
                    y2  | |  |   | F2 |
                        --v------------
                    y1  | S2 | --> S1 |
                        ---------------
                    y0  | F1 |   |    |
                        ---------------
                          x0   x1   x2   */
                    Point(0, 0) -> Token(pid1, Flag),
                    Point(2, 1) -> Token(pid1, Scissor),
                    Point(0, 1) -> Token(pid2, Scissor),
                    Point(2, 2) -> Token(pid2, Flag)
                  )
                ),
                currentPlayerId = pid1
              ),
              List(
                MoveSummary(pid1, Point(1, 1), Point(2, 1), None),
                MoveSummary(
                  pid2,
                  Point(0, 2),
                  Point(0, 1),
                  Some(AttackerWinsCombat(Token(pid2, Scissor), Token(pid1, Paper)))
                )
              ),
              GameInProgress
            )
          )
        )
      }

      "fail when game does not exist" in {
        wait(gameService().processTurn(gid, pid1, Point(0, 1), Point(1, 1))) should ===(
          Left(GameNotFoundError)
        )
      }
    }

    "handle getGame" should {
      "return a game" in {
        wait(gameService(List(game)).getGame(gid, pid1)) should ===(
          Right(
            GameWithStatus(
              game,
              Nil,
              GameInProgress
            )
          )
        )
      }

      "fail when game does not exist" in {
        wait(gameService().getGame(gid, pid1)) should ===(
          Left(GameNotFoundError)
        )
      }
    }

    "handle deleteGame" should {
      "delete a game" in {
        wait(gameService(List(game)).deleteGame(gid, pid1)) should ===(
          Right(())
        )
      }

      "fail when game does not exist" in {
        wait(gameService().deleteGame(gid, pid1)) should ===(
          Left(GameNotFoundError)
        )
      }
    }
  }
}
