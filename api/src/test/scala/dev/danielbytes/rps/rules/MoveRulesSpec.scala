package dev.danielbytes.rps.rules

import dev.danielbytes.rps.GameTestData
import dev.danielbytes.rps.model._
import org.scalactic.TypeCheckedTripleEquals
import org.scalatest.Inside._
import org.scalatest.wordspec.AnyWordSpec

class MoveRulesSpec extends AnyWordSpec with TypeCheckedTripleEquals with GameTestData {
  val subject = new MoveRules.Impl()

  "MoveRules" should {
    "handle moveToken rule" should {
      "allow a token move" in {
        val from = Point(0, 1)
        val to = Point(1, 1)
        /*
             ----------------
         y2  | S2 |    | F2 |
             ----------------
         y1  |    | R1 |    |
             ----------------
         y0  | F1 |    |    |
             ----------------
               x0   x1   x2    */
        inside(subject.moveToken(game, uid1, from, to)) {
          case Right(value) =>
            assert(value === TakePositionMove(from, to, MoveRight, MoveDistance(1)))
        }
      }

      "allow a token attack" in {
        val from = Point(0, 1)
        val to = Point(0, 2)
        /*
               ----------------------
           y2  | R1 -> S2 |    | F2 |
               ----------------------
           y1  |          |    |    |
               ----------------------
           y0  |    F1    |    |    |
               ----------------------
                    x0      x1    x2    */
        inside(subject.moveToken(game, uid1, from, to)) {
          case Right(value) =>
            assert(
              value === AttackMove(from, to, MoveForward, Token(uid1, Rock), Token(uid2, Scissor), MoveDistance(2)))
        }
      }

      "disallow wrong player taking a turn" in {
        inside(subject.moveToken(game, uid2, Point(0, 1), Point(0, 2))) {
          case Left(WrongPlayerTurnError) =>
        }
      }

      "disallow moving too far" in {
        inside(subject.moveToken(game, uid1, Point(0, 1), Point(2, 1))) {
          case Left(MoveIsTooFarError) =>
        }
      }

      "disallow moving an empty point" in {
        inside(subject.moveToken(game, uid1, Point(1, 1), Point(2, 1))) {
          case Left(NotATokenError) =>
        }
      }

      "disallow moving the opponents token" in {
        inside(subject.moveToken(game, uid1, Point(0, 2), Point(1, 2))) {
          case Left(OtherPlayersTokenError) =>
        }
      }

      "disallow moving an immovable token" in {
        inside(subject.moveToken(game, uid1, Point(0, 0), Point(1, 0))) {
          case Left(NotAMovableTokenError) =>
        }
      }

      "disallow attacking your own token" in {
        inside(subject.moveToken(game, uid1, Point(0, 1), Point(0, 0))) {
          case Left(CannotAttackYourOwnTokenError) =>
        }
      }
    }
  }
}
