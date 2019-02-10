package com.danielbytes.rps

import com.danielbytes.rps.model._

trait GameTestData {
  val gid = GameId("Game1")
  val p1 = PlayerId("p1")
  val p2 = PlayerId("p2")

  val game = Game(
    gid,
    Player(p1, "Player 1", StartPositionBottom, isAI = false),
    Player(p2, "Player 2", StartPositionTop, isAI = false),
    p1,
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

        Point(0, 0) -> Token(p1, Flag),
        Point(0, 1) -> Token(p1, Rock),
        Point(0, 2) -> Token(p2, Scissor),
        Point(2, 2) -> Token(p2, Flag)
      )
    )
  )

  val gameWithAI = game.copy(
    player2 = game.player2.copy(
      isAI = true
    )
  )
}
