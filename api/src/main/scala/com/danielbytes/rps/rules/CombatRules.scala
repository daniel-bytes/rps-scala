package com.danielbytes.rps.rules

import com.danielbytes.rps.model._

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
  def attackPlayer(
    attacker: Token,
    defender: Token
  ): Either[RuleViolationError, CombatResult] = {
    (attacker.tokenType, defender.tokenType) match {
      case (Bomb | Flag, _) => Left(TokenNotCapableOfAttackError)

      case (_, Bomb) => Right(EveryoneLosesCombat)
      case (_, Flag) => Right(AttackerWinsCombat(attacker))

      case (Rock, Rock) => Right(EveryoneLosesCombat)
      case (Rock, Paper) => Right(DefenderWinsCombat(defender))
      case (Rock, Scissor) => Right(AttackerWinsCombat(attacker))

      case (Paper, Rock) => Right(AttackerWinsCombat(attacker))
      case (Paper, Paper) => Right(EveryoneLosesCombat)
      case (Paper, Scissor) => Right(DefenderWinsCombat(defender))

      case (Scissor, Rock) => Right(DefenderWinsCombat(defender))
      case (Scissor, Paper) => Right(AttackerWinsCombat(attacker))
      case (Scissor, Scissor) => Right(EveryoneLosesCombat)
    }
  }
}

class CombatRulesEngine()
  extends CombatRules {}
