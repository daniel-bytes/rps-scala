package com.danielbytes.rps

import com.danielbytes.rps.helpers.DateTimeHelper
import com.danielbytes.rps.model._

trait GameTestData {
  implicit def dateTime: DateTimeHelper

  val gid = GameId("Game1")
  val gid2 = GameId("Game2")
  val pid1 = UserId("p1")
  val p1 = Player.native(pid1, UserName("Player 1"), StartPositionBottom)
  val pid2 = UserId("p2")
  val p2 = Player.native(pid2, UserName("Player 2"), StartPositionTop)

  val game = Game(
    gid,
    p1,
    p2,
    pid1,
    Board(
      Geometry(3, 3),
      Map( /*
            ----------------
        y2  | S2 |    | F2 |
            ----------------
        y1  | R1 |    |    |
            ----------------
        y0  | F1 |    |    |
            ----------------
              x0   x1   x2   */

        Point(0, 0) -> Token(pid1, Flag),
        Point(0, 1) -> Token(pid1, Rock),
        Point(0, 2) -> Token(pid2, Scissor),
        Point(2, 2) -> Token(pid2, Flag)
      )
    )
  )

  val gameWithAI = game.copy(
    player2 = game.player2.copy(
      user = game.player2.user.copy(
        isAI = true
      )
    )
  )

  val completedGame = game.copy(
    id = gid2,
    board = game.board.copy(
      tokens = Map(
        Point(0, 0) -> Token(pid1, Flag)
      )
    )
  )
}
