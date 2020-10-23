package dev.danielbytes.rps.rules

import dev.danielbytes.rps.model._

/**
 * Trait that defines the combat rules engine.
 */
trait CombatRules {

  /**
   * A player token attacks another token
   * @param attacker The attacker token
   * @param defender The defender token
   * @return The result of the attack:
   *         Either the attacker wins, defender wins,
   *         everyone loses, or an error condition is returned
   */
  def attackPlayer(attacker: Token, defender: Token): Either[RuleViolationError, CombatResult] = {
    (attacker.tokenType, defender.tokenType) match {
      case (Bomb | Flag, _) => Left(TokenNotCapableOfAttackError)

      case (_, Flag) | (Rock, Scissor) | (Paper, Rock) | (Scissor, Paper) =>
        Right(AttackerWinsCombat(attacker, defender))

      case (Rock, Paper) | (Paper, Scissor) | (Scissor, Rock) =>
        Right(DefenderWinsCombat(attacker, defender))

      case (_, Bomb) | (Rock, Rock) | (Paper, Paper) | (Scissor, Scissor) =>
        Right(EveryoneLosesCombat(attacker, defender))
    }
  }
}

object CombatRules {
  class Impl() extends CombatRules
}
