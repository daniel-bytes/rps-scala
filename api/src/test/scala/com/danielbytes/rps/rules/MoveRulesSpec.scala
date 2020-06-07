package com.danielbytes.rps.rules

import com.danielbytes.rps.GameTestData
import com.danielbytes.rps.helpers.Helpers
import com.danielbytes.rps.model._
import org.scalatest._

class MoveRulesSpec extends WordSpec with Matchers with MoveRules with GameTestData with Helpers {
  "MoveRules" should {
    "handle moveToken rule" should {
      "allow a token move" in {
        val from = Point(0, 1)
        val to = Point(1, 1)
        moveToken(game, pid1, from, to) should ===(
          /*
               ----------------
           y2  | S2 |    | F2 |
               ----------------
           y1  |    | R1 |    |
               ----------------
           y0  | F1 |    |    |
               ----------------
                 x0   x1   x2    */
          Right(TakePositionMove(from, to, MoveRight, MoveDistance(1)))
        )
      }

      "allow a token attack" in {
        val from = Point(0, 1)
        val to = Point(0, 2)
        moveToken(game, pid1, from, to) should ===(
          /*
               ----------------------
           y2  | R1 -> S2 |    | F2 |
               ----------------------
           y1  |          |    |    |
               ----------------------
           y0  |    F1    |    |    |
               ----------------------
                    x0      x1    x2    */
          Right(AttackMove(from, to, MoveForward, Token(pid1, Rock), Token(pid2, Scissor), MoveDistance(2)))
        )
      }

      "disallow wrong player taking a turn" in {
        moveToken(game, pid2, Point(0, 1), Point(0, 2)) should ===(
          Left(WrongPlayerTurnError)
        )
      }

      "disallow moving too far" in {
        moveToken(game, pid1, Point(0, 1), Point(2, 1)) should ===(
          Left(MoveIsTooFarError)
        )
      }

      "disallow moving an empty point" in {
        moveToken(game, pid1, Point(1, 1), Point(2, 1)) should ===(
          Left(NotATokenError)
        )
      }

      "disallow moving the opponents token" in {
        moveToken(game, pid1, Point(0, 2), Point(1, 2)) should ===(
          Left(OtherPlayersTokenError)
        )
      }

      "disallow moving an immovable token" in {
        moveToken(game, pid1, Point(0, 0), Point(1, 0)) should ===(
          Left(NotAMovableTokenError)
        )
      }

      "disallow attacking your own token" in {
        moveToken(game, pid1, Point(0, 1), Point(0, 0)) should ===(
          Left(CannotAttackYourOwnTokenError)
        )
      }
    }
  }
}
