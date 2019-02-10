package com.danielbytes.rps.engine

import com.danielbytes.rps
import com.danielbytes.rps.model._
import org.scalatest._

class PlayerAIRulesSpec
    extends WordSpec
    with Matchers
    with PlayerAIRules
    with rps.GameTestData {
  implicit val moveRules: MoveRules = new MoveRules {}

  "PlayerAIRules" should {
    "handle computeMove rules" should {
      "for player 1" should {
        "return the best possible move" in {
          computeMove(game, p1) should ===(
            Right(
              AttackMove(Point(0, 1), Point(0, 2), MoveForward, Token(p1, Rock), Token(p2, Scissor))
            )
          )
        }

        "fail if no movable tokens exist" in {
          val _game = game.copy(
            board = game.board.copy(
              tokens = game.board.tokens - Point(0, 1)
            )
          )
          computeMove(_game, p1) should ===(
            Left(
              NoMovableTokens
            )
          )
        }
      }

      "for player 2" should {
        "return the best possible move" in {
          computeMove(game.withCurrentPlayer(p2), p2) should ===(
            Right(
              AttackMove(Point(0, 2), Point(0, 1), MoveForward, Token(p2, Scissor), Token(p1, Rock))
            )
          )
        }
      }
    }

    "handle computePossibleMoves rules" should {
      "for player 1" should {
        "return all possible moves" in {
          computePossibleMoves(game, p1) should ===(
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
                TakePositionMove(Point(0, 1), Point(1, 1), MoveRight),
                AttackMove(Point(0, 1), Point(0, 2), MoveForward, Token(p1, Rock), Token(p2, Scissor))
              )
            )
          )
        }
      }

      "for player 2" should {
        "return all possible moves" in {
          computePossibleMoves(game.withCurrentPlayer(p2), p2) should ===(
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
                TakePositionMove(Point(0, 2), Point(1, 2), MoveRight),
                AttackMove(Point(0, 2), Point(0, 1), MoveForward, Token(p2, Scissor), Token(p1, Rock))
              )
            )
          )
        }
      }
    }
  }
}

