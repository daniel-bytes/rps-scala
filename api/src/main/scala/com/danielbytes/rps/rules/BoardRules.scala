package com.danielbytes.rps.rules

import java.util.UUID

import com.danielbytes.rps.model._
import com.danielbytes.rps.helpers.{ DateTimeHelper, RandomHelper }

trait BoardRules {
  def dateTime: DateTimeHelper
  def random: RandomHelper

  /**
   * Generates a new random single player game
   * @param player The human player
   * @return The generated game
   */
  def generateRandomSinglePlayerGame(
    player: Player
  ): Game = {
    val player2 = Player.ai(
      if (player.position == StartPositionTop) StartPositionBottom else StartPositionTop
    )

    Game(
      GameId(UUID.randomUUID().toString),
      player,
      player2,
      player.id,
      this.generateRandomBoard(player, player2)
    )
  }

  private def generateRandomBoard(
    player1: Player,
    player2: Player,
    bombCount: Int = 2,
    rows: Int = 6,
    columns: Int = 6,
    playerRows: Int = 2
  ): Board = {
    val flagCount: Int = 1

    if (playerRows < 1 || columns < 6 || rows < (playerRows * 2)) {
      throw BoardGeometryException(rows, columns)
    }

    val tokenCount = columns * playerRows
    val movableCount = tokenCount - flagCount - bombCount
    val rockCount = movableCount / 3
    val paperCount = movableCount / 3
    val scissorCount = movableCount - rockCount - paperCount

    if (paperCount != scissorCount || (tokenCount - flagCount - bombCount - rockCount - paperCount - scissorCount) != 0) {
      throw BoardTokenCountException(rows, columns, flagCount, bombCount, rockCount, paperCount, scissorCount)
    }

    def generateTokensForType(count: Int, token: TokenType): List[TokenType] =
      List.fill(count)(token)

    def generateTokenTypes(): Seq[TokenType] = {
      val tokens = random.shuffle(
        generateTokensForType(flagCount, Flag) ++
          generateTokensForType(bombCount, Bomb) ++
          generateTokensForType(rockCount, Rock) ++
          generateTokensForType(paperCount, Paper) ++
          generateTokensForType(scissorCount, Scissor)
      )

      if (tokens.indexOf(Flag) > tokens.size / 2) {
        generateTokenTypes()
      } else {
        tokens
      }
    }

    def generateTokens(player: Player): Map[Point, Token] = {
      generateTokenTypes().zipWithIndex.map {
        case (tokenType, idx) => {
          val point = player.position match {
            case StartPositionBottom =>
              Point(idx % columns, idx / columns)
            case StartPositionTop =>
              Point(idx % columns, rows - 1 - (idx / columns))
          }

          point -> Token(player.id, tokenType)
        }
      }.toMap
    }

    Board(
      Geometry(rows, columns),
      generateTokens(player1) ++ generateTokens(player2)
    )
  }
}

class BoardRulesEngine(
  val dateTime: DateTimeHelper,
  val random: RandomHelper
) extends BoardRules {}
