package com.danielbytes.rps.engine

import com.danielbytes.rps
import com.danielbytes.rps.model._
import org.scalatest._

class MoveRulesSpec
    extends WordSpec
    with Matchers
    with MoveRules
    with rps.GameTestData {
  "MoveRules" should {
    "handle moveToken rule" should {
      "allow a token move" in {
        val from = Point(0, 1)
        val to = Point(1, 1)
        moveToken(game, p1, from, to) should ===( /*
               ----------------
           y2  | S2 |    | F2 |
               ----------------
           y1  |    | R1 |    |
               ----------------
           y0  | F1 |    |    |
               ----------------
                 x0   x1   x2    */
          Right(TakePositionMove(from, to, MoveRight))
        )
      }

      "allow a token attack" in {
        val from = Point(0, 1)
        val to = Point(0, 2)
        moveToken(game, p1, from, to) should ===( /*
               ----------------------
           y2  | R1 -> S2 |    | F2 |
               ----------------------
           y1  |          |    |    |
               ----------------------
           y0  |    F1    |    |    |
               ----------------------
                    x0      x1    x2    */
          Right(AttackMove(from, to, MoveForward, Token(p1, Rock), Token(p2, Scissor)))
        )
      }

      "disallow wrong player taking a turn" in {
        moveToken(game, p2, Point(0, 1), Point(0, 2)) should ===(
          Left(WrongPlayerTurnError)
        )
      }

      "disallow moving too far" in {
        moveToken(game, p1, Point(0, 1), Point(2, 1)) should ===(
          Left(MoveIsTooFarError)
        )
      }

      "disallow moving an empty point" in {
        moveToken(game, p1, Point(1, 1), Point(2, 1)) should ===(
          Left(NotATokenError)
        )
      }

      "disallow moving the opponents token" in {
        moveToken(game, p1, Point(0, 2), Point(1, 2)) should ===(
          Left(OtherPlayersTokenError)
        )
      }

      "disallow moving an immovable token" in {
        moveToken(game, p1, Point(0, 0), Point(1, 0)) should ===(
          Left(NotAMovableTokenError)
        )
      }

      "disallow attacking your own token" in {
        moveToken(game, p1, Point(0, 1), Point(0, 0)) should ===(
          Left(CannotAttackYourOwnTokenError)
        )
      }
    }
  }
}
