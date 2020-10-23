package dev.danielbytes.rps.rules

import dev.danielbytes.rps.GameTestData
import dev.danielbytes.rps.model._
import org.scalactic.TypeCheckedTripleEquals
import org.scalatest.Inside._
import org.scalatest.wordspec.AnyWordSpec

class GameRulesSpec extends AnyWordSpec with TypeCheckedTripleEquals with GameTestData {

  val subject = new GameRules.Impl(
    new MoveRules.Impl(),
    new CombatRules.Impl())

  "GameRules" should {
    "handle gameTurn rules" should {
      "allow a token move" in {
        inside(subject.gameTurn(game, uid1, Point(0, 1), Point(1, 1))) {
          case Right(value) =>
            assert(
              value === GameWithMoveSummary(
                game.copy(
                  currentPlayerId = uid2,
                  board = game.board.copy(
                    tokens = Map(
                      /*
                    ----------------
                y2  | S2 |    | F2 |
                    ----------------
                y1  |    | R1 |    |
                    ----------------
                y0  | F1 |    |    |
                    ----------------
                      x0   x1   x2   */
                      Point(0, 0) -> Token(uid1, Flag),
                      Point(1, 1) -> Token(uid1, Rock),
                      Point(0, 2) -> Token(uid2, Scissor),
                      Point(2, 2) -> Token(uid2, Flag)))),
                Some(MoveSummary(uid1, Point(0, 1), Point(1, 1), None))))
        }
      }

      "allow a token attack" in {
        inside(subject.gameTurn(game, uid1, Point(0, 1), Point(0, 2))) {
          case Right(value) =>
            assert(
              value === GameWithMoveSummary(
                game.copy(
                  currentPlayerId = uid2,
                  board = game.board.copy(
                    tokens = Map(
                      /*
                   ----------------
               y2  | R1 |    | F2 |
                   ----------------
               y1  |    |    |    |
                   ----------------
               y0  | F1 |    |    |
                   ----------------
                     x0   x1   x2    */
                      Point(0, 0) -> Token(uid1, Flag),
                      Point(0, 2) -> Token(uid1, Rock),
                      Point(2, 2) -> Token(uid2, Flag)))),
                Some(
                  MoveSummary(
                    uid1,
                    Point(0, 1),
                    Point(0, 2),
                    Some(AttackerWinsCombat(Token(uid1, Rock), Token(uid2, Scissor)))))))
        }
      }

      "disallow invalid player" in {
        inside(subject.gameTurn(game, uid2, Point(0, 1), Point(0, 2))) {
          case Left(WrongPlayerTurnError) =>
        }
      }

      "disallow moving too far" in {
        inside(subject.gameTurn(game, uid1, Point(0, 1), Point(2, 1))) {
          case Left(MoveIsTooFarError) =>
        }
      }

      "disallow moving an empty point" in {
        inside(subject.gameTurn(game, uid1, Point(1, 1), Point(2, 1))) {
          case Left(NotATokenError) =>
        }
      }

      "disallow moving the opponents token" in {
        inside(subject.gameTurn(game, uid1, Point(0, 2), Point(1, 2))) {
          case Left(OtherPlayersTokenError) =>
        }
      }

      "disallow moving an immovable token" in {
        inside(subject.gameTurn(game, uid1, Point(0, 0), Point(1, 0))) {
          case Left(NotAMovableTokenError) =>
        }
      }

      "disallow attacking your own token" in {
        inside(subject.gameTurn(game, uid1, Point(0, 1), Point(0, 0))) {
          case Left(CannotAttackYourOwnTokenError) =>
        }
      }
    }

    "handle gameStatus rules" should {
      "with NoTokens result" in {
        inside(subject.gameStatus(game.copy(board = game.board.copy(tokens = Map())))) {
          case Left(NoTokens) =>
        }
      }

      "with NoFlags result" in {
        val value = subject.gameStatus(
          game.copy(
            board = game.board.copy(
              tokens = Map(
                /*
                    ----------------
                y2  | S2 |    |    |
                    ----------------
                y1  |    | R1 |    |
                    ----------------
                y0  |    |    |    |
                    ----------------
                      x0   x1   x2   */
                Point(1, 1) -> Token(uid1, Rock),
                Point(0, 2) -> Token(uid2, Scissor)))))
        inside(value) {
          case Left(NoFlags) =>
        }
      }

      "with GameOverFlagCaptured result" in {
        val value = subject.gameStatus(
          game.copy(
            board = game.board.copy(
              tokens = Map(
                /*
                    ----------------
                y2  | S2 |    | F2 |
                    ----------------
                y1  |    | R1 |    |
                    ----------------
                y0  |    |    |    |
                    ----------------
                      x0   x1   x2   */
                Point(1, 1) -> Token(uid1, Rock),
                Point(0, 2) -> Token(uid2, Scissor),
                Point(2, 2) -> Token(uid2, Flag)))))
        inside(value) {
          case Right(GameOverFlagCaptured(uid)) => assert(uid === uid2)
        }
      }

      "with GameOverStalemate result" in {
        val value = subject.gameStatus(
          game.copy(
            board = game.board.copy(
              tokens = Map(
                /*
                    ----------------
                y2  | B2 |    | F2 |
                    ----------------
                y1  |    | B1 |    |
                    ----------------
                y0  | F1 |    |    |
                    ----------------
                      x0   x1   x2   */
                Point(0, 0) -> Token(uid1, Flag),
                Point(1, 1) -> Token(uid1, Bomb),
                Point(0, 2) -> Token(uid2, Bomb),
                Point(2, 2) -> Token(uid2, Flag)))))
        inside(value) {
          case Right(GameOverStalemate) =>
        }
      }

      "with GameOverNoMoreTokens result" in {
        val value = subject.gameStatus(
          game.copy(
            board = game.board.copy(
              tokens = Map(
                /*
                    ----------------
                y2  | S2 |    | F2 |
                    ----------------
                y1  |    |    |    |
                    ----------------
                y0  | F1 |    |    |
                    ----------------
                      x0   x1   x2   */
                Point(0, 0) -> Token(uid1, Flag),
                Point(0, 2) -> Token(uid2, Scissor),
                Point(2, 2) -> Token(uid2, Flag)))))
        inside(value) {
          case Right(GameOverNoMoreTokens(uid2)) =>
        }
      }

      "with GameInProgress result" in {
        val value = subject.gameStatus(
          game.copy(
            board = game.board.copy(
              tokens = Map(
                /*
                    ----------------
                y2  | S2 |    | F2 |
                    ----------------
                y1  |    | R1 |    |
                    ----------------
                y0  | F1 |    |    |
                    ----------------
                      x0   x1   x2   */
                Point(0, 0) -> Token(uid1, Flag),
                Point(1, 1) -> Token(uid1, Rock),
                Point(0, 2) -> Token(uid2, Scissor),
                Point(2, 2) -> Token(uid2, Flag)))))
        inside(value) {
          case Right(GameInProgress) =>
        }
      }
    }
  }
}
