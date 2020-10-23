package dev.danielbytes.rps.rules

import dev.danielbytes.rps.GameTestData
import dev.danielbytes.rps.model._
import org.scalatest.Inside._
import org.scalatest.wordspec.AnyWordSpec

class PlayerAIRulesSpec extends AnyWordSpec with GameTestData {

  val subject = new PlayerAIRules.Impl(
    new MoveRules.Impl()) {
    override def nextDouble(): Double = 0.0
  }

  "PlayerAIRules" should {
    "handle computeMove rules" should {
      "for player 1" should {
        "return the best possible move" in {
          inside(subject.computeMove(game, uid1)) {
            case Right(value) =>
              assert(
                value === AttackMove(
                  Point(0, 1),
                  Point(0, 2),
                  MoveForward,
                  Token(uid1, Rock),
                  Token(uid2, Scissor),
                  MoveDistance(2)))
          }
        }

        "fail if no movable tokens exist" in {
          val _game = game.copy(board = game.board.copy(tokens = game.board.tokens - Point(0, 1)))

          inside(subject.computeMove(_game, uid1)) {
            case Left(NoMovableTokens) =>
          }
        }
      }

      "for player 2" should {
        "return the best possible move" in {
          inside(subject.computeMove(game.withCurrentPlayer(uid2), uid2)) {
            case Right(value) =>
              assert(
                value === AttackMove(
                  Point(0, 2),
                  Point(0, 1),
                  MoveForward,
                  Token(uid2, Scissor),
                  Token(uid1, Rock),
                  MoveDistance(1)))
          }
        }
      }
    }

    "handle computePossibleMoves rules" should {
      "for player 1" should {
        "return all possible moves" in {
          inside(subject.computePossibleMoves(game, uid1)) {
            case Right(value) =>
              assert(
                value ===
                  Set(
                    /*
                        ----------------
                    y2  | ^^ |    | F2 |
                        --||------------
                    y1  | R1 ==>  |    |
                        ----------------
                    y0  | F1 |    |    |
                        ----------------
                          x0   x1   x2   */
                    TakePositionMove(Point(0, 1), Point(1, 1), MoveRight, MoveDistance(1)),
                    AttackMove(
                      Point(0, 1),
                      Point(0, 2),
                      MoveForward,
                      Token(uid1, Rock),
                      Token(uid2, Scissor),
                      MoveDistance(2))))
          }
        }
      }

      "for player 2" should {
        "return all possible moves" in {
          inside(subject.computePossibleMoves(game.withCurrentPlayer(uid2), uid2)) {
            case Right(value) =>
              assert(
                value ===
                  Set(
                    /*
                        ----------------
                    y2  | S2 ==>  | F2 |
                        --||------------
                    y1  | vv |    |    |
                        ----------------
                    y0  | F1 |    |    |
                        ----------------
                          x0   x1   x2   */
                    TakePositionMove(Point(0, 2), Point(1, 2), MoveRight, MoveDistance(0)),
                    AttackMove(
                      Point(0, 2),
                      Point(0, 1),
                      MoveForward,
                      Token(uid2, Scissor),
                      Token(uid1, Rock),
                      MoveDistance(1))))
          }
        }
      }
    }
  }
}
