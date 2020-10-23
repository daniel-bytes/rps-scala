package dev.danielbytes.rps.services

import dev.danielbytes.rps.{ GameTestData, TestUtils }
import dev.danielbytes.rps.model._
import dev.danielbytes.rps.rules.Rules
import dev.danielbytes.rps.services.repositories.InMemoryGameRepository
import dev.danielbytes.rps.model.{ Flag, GameNotFoundError, Paper, Rock, Scissor, VersionConflictError }
import org.scalactic.TypeCheckedTripleEquals
import org.scalatest.Inside._
import org.scalatest.wordspec.AnyWordSpec

import scala.concurrent.ExecutionContext

class GameServiceSpec extends AnyWordSpec with TypeCheckedTripleEquals with GameTestData with TestUtils {
  implicit val ec: ExecutionContext = ExecutionContext.global

  def subject(games: List[Game] = Nil): GameService = {
    val rules = new Rules()
    val gameRepo = new InMemoryGameRepository()
    games.foreach(g => gameRepo.set(g.id, g))

    new GameService.Impl(gameRepo, rules.gameRules, rules.aiRules, rules.boardRules)(ec)
  }

  "GameService" should {
    "handle getPlayerGames" should {
      "return a players games" in {
        inside(wait(subject(List(game, completedGame)).getPlayerGames(uid1, includeCompleted = false))) {
          case Right(value) => assert(value === List(GameWithStatus(game, Nil, GameInProgress)))
        }
      }

      "return completed games" in {
        inside(wait(subject(List(game, completedGame)).getPlayerGames(uid1, includeCompleted = true))) {
          case Right(value) =>
            assert(
              value.toSet === Set(
                GameWithStatus(game, Nil, GameInProgress),
                GameWithStatus(completedGame, Nil, GameOverFlagCaptured(uid1))))
        }
      }

      "return nothing for an unknown player" in {
        inside(wait(subject(List(game, completedGame)).getPlayerGames(UserId("baduser"), includeCompleted = false))) {
          case Right(value) => assert(value === Nil)
        }
      }
    }

    "handle processTurn" should {
      "allow a token move" in {
        inside(wait(subject(List(game)).processTurn(gid1, uid1, Point(0, 1), Point(1, 1), version1))) {
          case Right(value) =>
            assert(
              value === GameWithStatus(
                game.copy(
                  currentPlayerId = uid2,
                  board = game.board.copy(
                    tokens = Map(
                      /*
                      ----------------
                  y2  | S2 |    | F2 |
                      ----------------
                  y1  |  --> R1 |    |
                      ----------------
                  y0  | F1 |    |    |
                      ----------------
                        x0   x1   x2   */
                      Point(0, 0) -> Token(uid1, Flag),
                      Point(1, 1) -> Token(uid1, Rock),
                      Point(0, 2) -> Token(uid2, Scissor),
                      Point(2, 2) -> Token(uid2, Flag))),
                  version = version2),
                List(MoveSummary(uid1, Point(0, 1), Point(1, 1), None)),
                GameInProgress))
        }
      }

      "allow a token move followed by an AI move" in {
        inside(wait(subject(List(gameWithAI)).processTurn(gid1, uid1, Point(0, 1), Point(1, 1), version1))) {
          case Right(value) =>
            assert(
              value === GameWithStatus(
                gameWithAI.copy(
                  board = game.board.copy(
                    tokens = Map(
                      /*
                      ----------------
                  y2  |    |    | F2 |
                      ----------------
                  y1  | S2 | R1 |    |
                      ----------------
                  y0  | F1 |    |    |
                      ----------------
                        x0   x1   x2   */
                      Point(0, 0) -> Token(uid1, Flag),
                      Point(1, 1) -> Token(uid1, Rock),
                      Point(0, 1) -> Token(uid2, Scissor),
                      Point(2, 2) -> Token(uid2, Flag))),
                  currentPlayerId = uid1,
                  version = version2),
                List(
                  MoveSummary(uid1, Point(0, 1), Point(1, 1), None),
                  MoveSummary(uid2, Point(0, 2), Point(0, 1), None)),
                GameInProgress))
        }
      }

      "AI move winning an attack correctly sets currentPlayerId to human player" in {
        val newGame = gameWithAI.copy(
          board = gameWithAI.board.copy(
            tokens = Map(
              /*
                        ----------------
                    y2  | S2 |    | F2 |
                        ----------------
                    y1  | P1 | S1 |    |
                        ----------------
                    y0  | F1 |    |    |
                        ----------------
                          x0   x1   x2   */

              Point(0, 0) -> Token(uid1, Flag),
              Point(0, 1) -> Token(uid1, Paper),
              Point(1, 1) -> Token(uid1, Scissor),
              Point(0, 2) -> Token(uid2, Scissor),
              Point(2, 2) -> Token(uid2, Flag))))

        inside(wait(subject(List(newGame)).processTurn(gid1, uid1, Point(1, 1), Point(2, 1), version1))) {
          case Right(value) =>
            assert(
              value === GameWithStatus(
                newGame.copy(
                  board = game.board.copy(
                    tokens = Map(
                      /*
                    ---------------
                y2  | |  |   | F2 |
                    --v------------
                y1  | S2 | --> S1 |
                    ---------------
                y0  | F1 |   |    |
                    ---------------
                      x0   x1   x2   */
                      Point(0, 0) -> Token(uid1, Flag),
                      Point(2, 1) -> Token(uid1, Scissor),
                      Point(0, 1) -> Token(uid2, Scissor),
                      Point(2, 2) -> Token(uid2, Flag))),
                  currentPlayerId = uid1,
                  version = version2),
                List(
                  MoveSummary(uid1, Point(1, 1), Point(2, 1), None),
                  MoveSummary(
                    uid2,
                    Point(0, 2),
                    Point(0, 1),
                    Some(AttackerWinsCombat(Token(uid2, Scissor), Token(uid1, Paper))))),
                GameInProgress))
        }
      }

      "fail when game does not exist" in {
        inside(wait(subject().processTurn(gid1, uid1, Point(0, 1), Point(1, 1), version1))) {
          case Left(GameNotFoundError) =>
        }
      }

      "fail when game version is incorrect" in {
        inside(wait(subject(List(game)).processTurn(gid1, uid1, Point(1, 1), Point(2, 1), version2))) {
          case Left(VersionConflictError) =>
        }
      }
    }

    "handle getGame" should {
      "return a game" in {
        inside(wait(subject(List(game)).getGame(gid1, uid1))) {
          case Right(GameWithStatus(g, Nil, GameInProgress)) => assert(g === game)
        }
      }

      "fail when game does not exist" in {
        inside(wait(subject().getGame(gid1, uid1))) {
          case Left(GameNotFoundError) =>
        }
      }
    }

    "handle deleteGame" should {
      "delete a game" in {
        inside(wait(subject(List(game)).deleteGame(gid1, uid1))) {
          case Right(()) =>
        }
      }

      "fail when game does not exist" in {
        inside(wait(subject().deleteGame(gid1, uid1))) {
          case Left(GameNotFoundError) =>
        }
      }
    }
  }
}
