package dev.danielbytes.rps.api.game

import dev.danielbytes.rps.{ model => Domain }

/**
 * API game models
 */
object GameApiModel {

  /**
   * A move taken in the game
   *
   * @param from    The move starting point
   * @param to      The move endpoint point
   * @param version The version number of the game (for optimistic concurrency)
   */
  case class GameMove(
    from: Domain.Point,
    to: Domain.Point,
    version: Int)

  /**
   * Summary of a combat action
   *
   * @param attackerTokenType The token type of the attacker
   * @param defenderTokenType The token type of the defender
   * @param winnerTokenType The optional token type of the winner
   */
  case class CombatSummary(
    attackerTokenType: String,
    defenderTokenType: String,
    winnerTokenType: Option[String])

  /**
   * Summary of a game move action
   *
   * @param playerId The id of the player that moved
   * @param from The point the player moved from
   * @param to The point the player moved to
   * @param combatSummary The optional combat outcome summary
   */
  case class GameMoveSummary(
    playerId: String,
    from: Domain.Point,
    to: Domain.Point,
    combatSummary: Option[CombatSummary])

  /**
   * A player token at a position
   *
   * @param position The position the token exists at
   * @param tokenType The type of token
   * @param playerOwned True if the token is owned by the current player
   */
  case class Token(
    position: Domain.Point,
    tokenType: String,
    playerOwned: Boolean)

  /**
   * Overview of the entire game, from the point of view of the current player
   *
   * @param id The game id
   * @param playerId The current player's id
   * @param playerName The current player's name
   * @param otherPlayerName The other player's name
   * @param isGameOver True if the game is over
   * @param winnerName Optional name of the winner (empty unless isGameOver is true)
   */
  case class GameOverview(
    id: String,
    playerId: String,
    playerName: String,
    otherPlayerName: String,
    isGameOver: Boolean,
    winnerName: Option[String])

  object GameOverview {

    /**
     * Creates an API GameOverview from a domain GameWithStatus
     * @param userId The current player's user id
     * @param model The domain Game model
     * @return The API GameOverview model
     */
    def apply(userId: Domain.UserId, model: Domain.GameWithStatus): GameOverview = {
      val player = model.game.player(userId).getOrElse(throw Domain.IncorrectPlayerException())
      val other = model.game.notPlayer(userId)

      val (gameOver: Boolean, winnerName: Option[String]) = model.status match {
        case Domain.GameInProgress => (false, None)
        case s: Domain.GameOverStatus => (true, s.winnerId.flatMap(id => model.game.player(id)).map(_.user.name.value))
      }

      GameOverview(
        model.game.id.value,
        player.id.value,
        player.user.name.value,
        other.user.name.value,
        gameOver,
        winnerName)
    }
  }

  /**
   * A list of all games for the current player
   *
   * @param games The game overviews for the current player
   */
  case class GameOverviews(
    games: List[GameOverview])

  /**
   * A game, from the point of view of the current player
   *
   * @param gameId The id of the game
   * @param playerId The current player's id
   * @param playerName The current player's name
   * @param otherPlayerName The other player's name
   * @param isPlayerTurn True if it is the current player's turn to move
   * @param isGameOver True if the game is over
   * @param winnerName Optional name of the winner (empty unless isGameOver is true)
   * @param board The game board
   * @param tokens The set of tokens on the game board.
   *               Only the current player's token types are available.
   * @param recentMoves A list of recent moves by both players.
   *                    Useful for a frontend to show animations and move summary.
   * @param version The version number of the game (for optimistic concurrency)
   */
  case class Game(
    gameId: String,
    playerId: String,
    playerName: String,
    otherPlayerName: String,
    isPlayerTurn: Boolean,
    isGameOver: Boolean,
    winnerName: Option[String],
    board: Domain.Geometry,
    tokens: List[Token],
    recentMoves: List[GameMoveSummary],
    version: Int)

  object GameApiModel {

    /**
     * Creates a new API Game model from a domain GameWithStatus model
     * @param game The domain Fame model
     * @param userId The current player's user id
     * @return The API Fame model
     */
    def apply(game: Domain.GameWithStatus, userId: Domain.UserId): Game = {
      val board = for {
        x <- 0 to game.game.board.geometry.columns
        y <- 0 to game.game.board.geometry.rows
        token = game.game.board.tokens.get(Domain.Point(x, y))
        playerOwned = token.exists(_.owner == userId)
        tokenType <- token.map { t => if (playerOwned) t.tokenType.name else Domain.Token.other }
      } yield Token(Domain.Point(x, y), tokenType, playerOwned)

      val (isGameOver: Boolean, winnerId: Option[Domain.UserId]) = game.status match {
        case Domain.GameInProgress => (false, None)
        case s: Domain.GameOverStatus => (true, s.winnerId)
      }

      val player = game.game.player(userId).getOrElse(throw Domain.IncorrectPlayerException())
      val otherPlayer = game.game.notPlayer(userId)
      val maybeWinningPlayer =
        winnerId.map(id => game.game.player(id).getOrElse(throw Domain.IncorrectPlayerException()))
      val isPlayerTurn = userId == game.game.currentPlayer.id

      val recentMoves = game.moves.map(move =>
        GameMoveSummary(
          move.playerId.value,
          move.from,
          move.to,
          move.combatResult.map(result =>
            CombatSummary(
              attackerTokenType = result.attacker.tokenType.name,
              defenderTokenType = result.defender.tokenType.name,
              winnerTokenType = result.winner.map(_.tokenType.name)))))

      Game(
        gameId = game.game.id.value,
        playerId = player.id.value,
        playerName = player.name.value,
        otherPlayerName = otherPlayer.name.value,
        isPlayerTurn = isPlayerTurn,
        isGameOver = isGameOver,
        winnerName = maybeWinningPlayer.map(_.name.value),
        board = game.game.board.geometry,
        tokens = board.toList,
        recentMoves = recentMoves,
        version = game.game.version.value)
    }
  }

}
