package com.danielbytes.rps.rules

import com.danielbytes.rps.GameTestData
import com.danielbytes.rps.helpers.{ Helpers, RandomHelper }
import com.danielbytes.rps.model._
import org.scalatest._

class PlayerAIRulesSpec extends WordSpec with Matchers with PlayerAIRules with GameTestData with Helpers {
  val moveRules: MoveRules = new MoveRules {}

  override def random: RandomHelper =
    new RandomHelper {
      override def nextDouble(): Double = 0.0
    }

  "PlayerAIRules" should {
    "handle computeMove rules" should {
      "for player 1" should {
        "return the best possible move" in {
          computeMove(game, pid1) should ===(
            Right(
              AttackMove(
                Point(0, 1),
                Point(0, 2),
                MoveForward,
                Token(pid1, Rock),
                Token(pid2, Scissor),
                MoveDistance(2)
              )
            )
          )
        }

        "fail if no movable tokens exist" in {
          val _game = game.copy(
            board = game.board.copy(
              tokens = game.board.tokens - Point(0, 1)
            )
          )
          computeMove(_game, pid1) should ===(
            Left(
              NoMovableTokens
            )
          )
        }
      }

      "for player 2" should {
        "return the best possible move" in {
          computeMove(game.withCurrentPlayer(pid2), pid2) should ===(
            Right(
              AttackMove(
                Point(0, 2),
                Point(0, 1),
                MoveForward,
                Token(pid2, Scissor),
                Token(pid1, Rock),
                MoveDistance(1)
              )
            )
          )
        }
      }
    }

    "handle computePossibleMoves rules" should {
      "for player 1" should {
        "return all possible moves" in {
          computePossibleMoves(game, pid1) should ===(
            Right(
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
                  Token(pid1, Rock),
                  Token(pid2, Scissor),
                  MoveDistance(2)
                )
              )
            )
          )
        }
      }

      "for player 2" should {
        "return all possible moves" in {
          computePossibleMoves(game.withCurrentPlayer(pid2), pid2) should ===(
            Right(
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
                  Token(pid2, Scissor),
                  Token(pid1, Rock),
                  MoveDistance(1)
                )
              )
            )
          )
        }
      }
    }
  }
}
