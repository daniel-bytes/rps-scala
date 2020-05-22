package com.danielbytes.rps.rules

import com.danielbytes.rps
import com.danielbytes.rps.GameTestData
import com.danielbytes.rps.helpers.Helpers
import com.danielbytes.rps.model._
import org.scalatest._

class CombatRulesSpec
    extends WordSpec
    with Matchers
    with CombatRules
    with GameTestData
    with Helpers {
  "CombatRules" should {
    "handle attackPlayer rules" should {
      for { attackerToken <- Token.types; defenderToken <- Token.types } {
        s"$attackerToken attacks $defenderToken" in {
          val attacker = Token(pid1, attackerToken)
          val defender = Token(pid2, defenderToken)
          val results = attackPlayer(attacker, defender)

          (attackerToken, defenderToken) match {
            case (Bomb | Flag, _) => results should ===(Left(TokenNotCapableOfAttackError))
            case (_, Bomb) => results should ===(Right(EveryoneLosesCombat(attacker, defender)))
            case (_, Flag) => results should ===(Right(AttackerWinsCombat(attacker, defender)))

            case (Rock, Rock) => results should ===(Right(EveryoneLosesCombat(attacker, defender)))
            case (Rock, Paper) => results should ===(Right(DefenderWinsCombat(attacker, defender)))
            case (Rock, Scissor) => results should ===(Right(AttackerWinsCombat(attacker, defender)))

            case (Paper, Rock) => results should ===(Right(AttackerWinsCombat(attacker, defender)))
            case (Paper, Paper) => results should ===(Right(EveryoneLosesCombat(attacker, defender)))
            case (Paper, Scissor) => results should ===(Right(DefenderWinsCombat(attacker, defender)))

            case (Scissor, Rock) => results should ===(Right(DefenderWinsCombat(attacker, defender)))
            case (Scissor, Paper) => results should ===(Right(AttackerWinsCombat(attacker, defender)))
            case (Scissor, Scissor) => results should ===(Right(EveryoneLosesCombat(attacker, defender)))
          }
        }
      }
    }
  }
}
