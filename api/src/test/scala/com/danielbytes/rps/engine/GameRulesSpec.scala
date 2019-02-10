package com.danielbytes.rps.engine

import com.danielbytes.rps
import com.danielbytes.rps.model._
import org.scalatest._

class GameRulesSpec
    extends WordSpec
    with Matchers
    with GameRules
    with rps.GameTestData {
  implicit val combatRules: CombatRules = new CombatRules {}
  implicit val moveRules: MoveRules = new MoveRules {}

  "GameRules" should {
    "handle gameTurn rules" should {
      "allow a token move" in {
        gameTurn(game, p1, Point(0, 1), Point(1, 1)) should ===(
          Right(game.copy(
            currentPlayerId = p2,
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
                Point(0, 0) -> Token(p1, Flag),
                Point(1, 1) -> Token(p1, Rock),
                Point(0, 2) -> Token(p2, Scissor),
                Point(2, 2) -> Token(p2, Flag)
              )
            )
          ))
        )
      }

      "allow a token attack" in {
        gameTurn(game, p1, Point(0, 1), Point(0, 2)) should ===(
          Right(game.copy(
            currentPlayerId = p2,
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
                Point(0, 0) -> Token(p1, Flag),
                Point(0, 2) -> Token(p1, Rock),
                Point(2, 2) -> Token(p2, Flag)
              )
            )
          ))
        )
      }

      "disallow invalid player" in {
        gameTurn(game, p2, Point(0, 1), Point(0, 2)) should ===(
          Left(WrongPlayerTurnError)
        )
      }

      "disallow moving too far" in {
        gameTurn(game, p1, Point(0, 1), Point(2, 1)) should ===(
          Left(MoveIsTooFarError)
        )
      }

      "disallow moving an empty point" in {
        gameTurn(game, p1, Point(1, 1), Point(2, 1)) should ===(
          Left(NotATokenError)
        )
      }

      "disallow moving the opponents token" in {
        gameTurn(game, p1, Point(0, 2), Point(1, 2)) should ===(
          Left(OtherPlayersTokenError)
        )
      }

      "disallow moving an immovable token" in {
        gameTurn(game, p1, Point(0, 0), Point(1, 0)) should ===(
          Left(NotAMovableTokenError)
        )
      }

      "disallow attacking your own token" in {
        gameTurn(game, p1, Point(0, 1), Point(0, 0)) should ===(
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
              Point(1, 1) -> Token(p1, Rock),
              Point(0, 2) -> Token(p2, Scissor)
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
              Point(1, 1) -> Token(p1, Rock),
              Point(0, 2) -> Token(p2, Scissor),
              Point(2, 2) -> Token(p2, Flag)
            )
          )
        )) should ===(Right(GameOverFlagCaptured(p2)))
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
              Point(0, 0) -> Token(p1, Flag),
              Point(1, 1) -> Token(p1, Bomb),
              Point(0, 2) -> Token(p2, Bomb),
              Point(2, 2) -> Token(p2, Flag)
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
              Point(0, 0) -> Token(p1, Flag),
              Point(0, 2) -> Token(p2, Scissor),
              Point(2, 2) -> Token(p2, Flag)
            )
          )
        )) should ===(Right(GameOverNoMoreTokens(p2)))
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
              Point(0, 0) -> Token(p1, Flag),
              Point(1, 1) -> Token(p1, Rock),
              Point(0, 2) -> Token(p2, Scissor),
              Point(2, 2) -> Token(p2, Flag)
            )
          )
        )) should ===(Right(GameInProgress))
      }
    }
  }
}
