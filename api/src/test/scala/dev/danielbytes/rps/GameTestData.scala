package dev.danielbytes.rps

import dev.danielbytes.rps.model._

trait GameTestData {
  val gid1 = GameId("Game1")
  val gid2 = GameId("Game2")
  val uid1 = UserId("p1")
  val p1 = Player.player(uid1, UserName("Player 1"), StartPositionBottom)
  val uid2 = UserId("p2")
  val p2 = Player.player(uid2, UserName("Player 2"), StartPositionTop)
  val version1 = GameVersion(1)
  val version2 = GameVersion(2)

  val game = Game(
    gid1,
    p1,
    p2,
    uid1,
    Board(
      Geometry(3, 3),
      Map(
        /*
            ----------------
        y2  | S2 |    | F2 |
            ----------------
        y1  | R1 |    |    |
            ----------------
        y0  | F1 |    |    |
            ----------------
              x0   x1   x2   */

        Point(0, 0) -> Token(uid1, Flag),
        Point(0, 1) -> Token(uid1, Rock),
        Point(0, 2) -> Token(uid2, Scissor),
        Point(2, 2) -> Token(uid2, Flag))),
    version1)

  val gameWithAI = game.copy(player2 = game.player2.copy(user = game.player2.user.copy(isAI = true)))

  val completedGame = game.copy(id = gid2, board = game.board.copy(tokens = Map(Point(0, 0) -> Token(uid1, Flag))))
}
