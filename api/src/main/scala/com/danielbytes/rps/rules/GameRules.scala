package com.danielbytes.rps.rules

import com.danielbytes.rps.model._

/**
 * Trait that defines the overall rules engine for the game.
 * This includes handling a player move, and determining the game status.
 */
trait GameRules {
  implicit def combatRules: CombatRules
  implicit def moveRules: MoveRules

  /**
   * A player takes a turn in the game
   * @param game The game state
   * @param userId The player taking a turn
   * @param from The point where the player token is moving from
   * @param to The point where the player token is moving to
   * @return The updated game state, or an error condition
   */
  def gameTurn(
    game: Game,
    userId: UserId,
    from: Point,
    to: Point
  ): Either[RuleViolationError, Game] = {
    for {
      moveResult <- moveRules.moveToken(game, userId, from, to)
      gameResult <- handleMove(game, from, to, moveResult)
      result = gameResult.copy(currentPlayerId = game.otherPlayer.id)
    } yield result
  }

  /**
   * Calculates the game status from the game state
   * @param game The game state
   * @return The game status, or a game state error
   */
  def gameStatus(
    game: Game
  ): Either[GameStateError, GameStatus] = {
    val state = game.playerList.map(
      getPlayerState(game, _)
    )

    val hasFlag = state.filter(_.hasFlag)
    val canMove = state.filter(_.canMove)

    if (game.board.tokens.isEmpty)
      Left(NoTokens)
    else if (hasFlag.isEmpty)
      Left(NoFlags)
    else if (hasFlag.size == 1)
      Right(GameOverFlagCaptured(hasFlag.head.userId))
    else if (canMove.isEmpty)
      Right(GameOverStalemate)
    else if (canMove.size == 1)
      Right(GameOverNoMoreTokens(canMove.head.userId))
    else
      Right(GameInProgress)
  }

  /**
   * Process a move as part of a player's game turn
   * @param game The current game state
   * @param from The point the attacker is coming from
   * @param to The point the defender is on
   * @param moveResult The result of the move
   * @return The updated game state
   */
  private def handleMove(
    game: Game,
    from: Point,
    to: Point,
    moveResult: MoveResult
  ): Either[RuleViolationError, Game] = {
    moveResult match {
      case TakePositionMove(f, t, _) =>
        movePoint(game, f, t)
      case AttackMove(f, t, _, attacker, defender) =>
        handleCombat(game, f, t, attacker, defender)
    }
  }

  /**
   * Process combat as part of a player's game turn
   * @param game The current game state
   * @param from The point the attacker is coming from
   * @param to The point the defender is on
   * @param attacker The attacker token
   * @param defender The defender token
   * @return The updated game state
   */
  private def handleCombat(
    game: Game,
    from: Point,
    to: Point,
    attacker: Token,
    defender: Token
  ): Either[RuleViolationError, Game] = {
    combatRules.attackPlayer(attacker, defender) flatMap {
      case EveryoneLosesCombat =>
        removePoints(game, from :: to :: Nil)
      case DefenderWinsCombat(_) =>
        removePoints(game, from :: Nil)
      case AttackerWinsCombat(_) =>
        movePoint(game, from, to)
    }
  }

  /**
   * Moves a point on the game board
   * @param game The current game state
   * @param from The point to move from
   * @param to The point to move to
   * @return The updated game state
   */
  private def movePoint(
    game: Game,
    from: Point,
    to: Point
  ): Either[RuleViolationError, Game] = {
    val tokens = game.board.tokens
    val token = tokens(from)

    Right(game.copy(
      board = game.board.copy(
        tokens = (tokens - from - to) + (to -> token)
      )
    ))
  }

  /**
   * Removes points from a Game
   * @param game The current game state
   * @param points The points to remove from the game board
   * @return The updated game state
   */
  private def removePoints(
    game: Game,
    points: Iterable[Point]
  ): Either[RuleViolationError, Game] = {
    val tokens = game.board.tokens

    Right(game.copy(
      board = game.board.copy(
        tokens = tokens -- points
      )
    ))
  }

  /**
   * Function for returning a State struct for a player
   * @param game The current game state
   * @param player The player
   * @return The player's game state
   */
  private def getPlayerState(
    game: Game,
    player: Player
  ): PlayerState = {
    val tokens = game.board.playerTokens(player.id)

    PlayerState(
      player.id,
      hasFlag = tokens.map(_.tokenType).collectFirst { case flag: Flag.type => flag }.isDefined,
      canMove = tokens.exists(_.movable)
    )
  }

  /**
   * Simple internal struct for managing a player's game state
   * @param userId The player's Id
   * @param hasFlag True if the player has a flag
   * @param canMove True if the player can move
   */
  private case class PlayerState(
    userId: UserId,
    hasFlag: Boolean,
    canMove: Boolean
  )
}

class GameRulesEngine()(
  implicit
  val moveRules: MoveRules,
  val combatRules: CombatRules
) extends GameRules {}
