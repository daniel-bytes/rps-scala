package com.danielbytes.rps.rules

import com.danielbytes.rps
import com.danielbytes.rps.GameTestData
import com.danielbytes.rps.helpers.Helpers
import com.danielbytes.rps.model._
import org.scalatest._

class GameRulesSpec
    extends WordSpec
    with Matchers
    with GameRules
    with GameTestData
    with Helpers {
  implicit val combatRules: CombatRules = new CombatRules {}
  implicit val moveRules: MoveRules = new MoveRules {}

  "GameRules" should {
    "handle gameTurn rules" should {
      "allow a token move" in {
        gameTurn(game, pid1, Point(0, 1), Point(1, 1)) should ===(
          Right(game.copy(
            currentPlayerId = pid2,
            board = game.board.copy(
              tokens = Map( /*
                      ----------------
                  y2  | S2 |    | F2 |
                      ----------------
                  y1  |    | R1 |    |
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
          ))
        )
      }

      "allow a token attack" in {
        gameTurn(game, pid1, Point(0, 1), Point(0, 2)) should ===(
          Right(game.copy(
            currentPlayerId = pid2,
            board = game.board.copy(
              tokens = Map( /*
                     ----------------
                 y2  | R1 |    | F2 |
                     ----------------
                 y1  |    |    |    |
                     ----------------
                 y0  | F1 |    |    |
                     ----------------
                       x0   x1   x2    */
                Point(0, 0) -> Token(pid1, Flag),
                Point(0, 2) -> Token(pid1, Rock),
                Point(2, 2) -> Token(pid2, Flag)
              )
            )
          ))
        )
      }

      "disallow invalid player" in {
        gameTurn(game, pid2, Point(0, 1), Point(0, 2)) should ===(
          Left(WrongPlayerTurnError)
        )
      }

      "disallow moving too far" in {
        gameTurn(game, pid1, Point(0, 1), Point(2, 1)) should ===(
          Left(MoveIsTooFarError)
        )
      }

      "disallow moving an empty point" in {
        gameTurn(game, pid1, Point(1, 1), Point(2, 1)) should ===(
          Left(NotATokenError)
        )
      }

      "disallow moving the opponents token" in {
        gameTurn(game, pid1, Point(0, 2), Point(1, 2)) should ===(
          Left(OtherPlayersTokenError)
        )
      }

      "disallow moving an immovable token" in {
        gameTurn(game, pid1, Point(0, 0), Point(1, 0)) should ===(
          Left(NotAMovableTokenError)
        )
      }

      "disallow attacking your own token" in {
        gameTurn(game, pid1, Point(0, 1), Point(0, 0)) should ===(
          Left(CannotAttackYourOwnTokenError)
        )
      }
    }

    "handle gameStatus rules" should {
      "with NoTokens result" in {
        gameStatus(game.copy(
          board = game.board.copy(
            tokens = Map()
          )
        )) should ===(Left(NoTokens))
      }

      "with NoFlags result" in {
        gameStatus(game.copy(
          board = game.board.copy(
            tokens = Map( /*
                      ----------------
                  y2  | S2 |    |    |
                      ----------------
                  y1  |    | R1 |    |
                      ----------------
                  y0  |    |    |    |
                      ----------------
                        x0   x1   x2   */
              Point(1, 1) -> Token(pid1, Rock),
              Point(0, 2) -> Token(pid2, Scissor)
            )
          )
        )) should ===(Left(NoFlags))
      }

      "with GameOverFlagCaptured result" in {
        gameStatus(game.copy(
          board = game.board.copy(
            tokens = Map( /*
                      ----------------
                  y2  | S2 |    | F2 |
                      ----------------
                  y1  |    | R1 |    |
                      ----------------
                  y0  |    |    |    |
                      ----------------
                        x0   x1   x2   */
              Point(1, 1) -> Token(pid1, Rock),
              Point(0, 2) -> Token(pid2, Scissor),
              Point(2, 2) -> Token(pid2, Flag)
            )
          )
        )) should ===(Right(GameOverFlagCaptured(pid2)))
      }

      "with GameOverStalemate result" in {
        gameStatus(game.copy(
          board = game.board.copy(
            tokens = Map( /*
                      ----------------
                  y2  | B2 |    | F2 |
                      ----------------
                  y1  |    | B1 |    |
                      ----------------
                  y0  | F1 |    |    |
                      ----------------
                        x0   x1   x2   */
              Point(0, 0) -> Token(pid1, Flag),
              Point(1, 1) -> Token(pid1, Bomb),
              Point(0, 2) -> Token(pid2, Bomb),
              Point(2, 2) -> Token(pid2, Flag)
            )
          )
        )) should ===(Right(GameOverStalemate))
      }

      "with GameOverNoMoreTokens result" in {
        gameStatus(game.copy(
          board = game.board.copy(
            tokens = Map( /*
                      ----------------
                  y2  | S2 |    | F2 |
                      ----------------
                  y1  |    |    |    |
                      ----------------
                  y0  | F1 |    |    |
                      ----------------
                        x0   x1   x2   */
              Point(0, 0) -> Token(pid1, Flag),
              Point(0, 2) -> Token(pid2, Scissor),
              Point(2, 2) -> Token(pid2, Flag)
            )
          )
        )) should ===(Right(GameOverNoMoreTokens(pid2)))
      }

      "with GameInProgress result" in {
        gameStatus(game.copy(
          board = game.board.copy(
            tokens = Map( /*
                      ----------------
                  y2  | S2 |    | F2 |
                      ----------------
                  y1  |    | R1 |    |
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
        )) should ===(Right(GameInProgress))
      }
    }
  }
}
