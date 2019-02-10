package com.danielbytes.rps.services

import com.danielbytes.rps.{ GameTestData, TestUtils }
import com.danielbytes.rps.model._
import org.scalatest._

import scala.concurrent.ExecutionContext

class GameServiceSpec
    extends WordSpec
    with Matchers
    with GameTestData
    with TestUtils {
  implicit val ec: ExecutionContext = ExecutionContext.global

  def gameService(game: Option[Game] = None): GameService = {
    val repository = new InMemoryRepository[GameId, Game]()
    game.foreach(g => repository.set(g.id, g))

    new GameServiceImpl()(ec, repository)
  }

  "GameService" should {
    "handle processTurn" should {
      "allow a token move" in {
        wait(gameService(Some(game)).processTurn(gid, p1, Point(0, 1), Point(1, 1))) should ===(
          Right(
            PlayerTurnResult(
              game.copy(
                currentPlayerId = p2,
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
                    Point(0, 0) -> Token(p1, Flag),
                    Point(1, 1) -> Token(p1, Rock),
                    Point(0, 2) -> Token(p2, Scissor),
                    Point(2, 2) -> Token(p2, Flag)
                  )
                )
              ),
              GameInProgress
            )
          )
        )
      }

      "allow a token move followed by an AI move" in {
        wait(gameService(Some(gameWithAI)).processTurn(gid, p1, Point(0, 1), Point(1, 1))) should ===(
          Right(
            PlayerTurnResult(
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
                    Point(0, 0) -> Token(p1, Flag),
                    Point(1, 1) -> Token(p1, Rock),
                    Point(0, 1) -> Token(p2, Scissor),
                    Point(2, 2) -> Token(p2, Flag)
                  )
                )
              ),
              GameInProgress
            )
          )
        )
      }

      "fail when game does not exist" in {
        wait(gameService().processTurn(gid, p1, Point(0, 1), Point(1, 1))) should ===(
          Left(GameNotFoundError)
        )
      }
    }

    "handle getGame" should {
      "return a game" in {
        wait(gameService(Some(game)).getGame(gid, p1)) should ===(
          Right(
            PlayerTurnResult(
              game,
              GameInProgress
            )
          )
        )
      }

      "fail when game does not exist" in {
        wait(gameService().getGame(gid, p1)) should ===(
          Left(GameNotFoundError)
        )
      }
    }

    "handle deleteGame" should {
      "delete a game" in {
        wait(gameService(Some(game)).deleteGame(gid, p1)) should ===(
          Right(())
        )
      }

      "fail when game does not exist" in {
        wait(gameService().deleteGame(gid, p1)) should ===(
          Left(GameNotFoundError)
        )
      }
    }
  }
}
