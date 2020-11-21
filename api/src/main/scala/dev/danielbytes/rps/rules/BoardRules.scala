package dev.danielbytes.rps.rules

import java.util.UUID

import dev.danielbytes.rps.model._
import dev.danielbytes.rps.helpers.{ DateTimeHelper, RandomHelper }

/**
 * Trait that defines the Game Board rules engine
 */
trait BoardRules extends DateTimeHelper with RandomHelper {

  /**
   * Generates a new random single player game with default parameters applied
   * @param player The human player
   * @return The generated game
   */
  def generateRandomSinglePlayerGame(player: Player): Game = {
    val player2 = Player.ai(
      if (player.position == StartPositionTop) StartPositionBottom else StartPositionTop)

    Game(
      GameId(UUID.randomUUID().toString),
      player,
      player2,
      player.id,
      this.generateRandomBoard(player, player2),
      GameVersion(1))
  }

  // Generates a new random game board with fine-grained parameters
  private def generateRandomBoard(
    player1: Player,
    player2: Player,
    bombCount: Int = 2,
    rows: Int = 6,
    columns: Int = 6,
    playerRows: Int = 2): Board = {
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
      val tokens = shuffle(
        generateTokensForType(flagCount, Flag) ++
          generateTokensForType(bombCount, Bomb) ++
          generateTokensForType(rockCount, Rock) ++
          generateTokensForType(paperCount, Paper) ++
          generateTokensForType(scissorCount, Scissor))

      if (tokens.indexOf(Flag) > ((tokens.size / 2) - 1)) {
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

    Board(Geometry(rows, columns), generateTokens(player1) ++ generateTokens(player2))
  }
}

object BoardRules {

  /**
   * Default implementation of Game Board rules engine
   */
  class Impl() extends BoardRules
}
