package com.danielbytes.rps.engine

import com.danielbytes.rps
import com.danielbytes.rps.model._
import org.scalatest._

class CombatRulesSpec
    extends WordSpec
    with Matchers
    with CombatRules
    with rps.GameTestData {
  "CombatRules" should {
    "handle attackPlayer rules" should {
      for { attackerToken <- Token.types; defenderToken <- Token.types } {
        s"$attackerToken attacks $defenderToken" in {
          val attacker = Token(p1, attackerToken)
          val defender = Token(p2, defenderToken)
          val results = attackPlayer(attacker, defender)

          (attackerToken, defenderToken) match {
            case (Bomb | Flag, _) => results should ===(Left(TokenNotCapableOfAttackError))
            case (_, Bomb) => results should ===(Right(EveryoneLosesCombat))
            case (_, Flag) => results should ===(Right(AttackerWinsCombat(attacker)))

            case (Rock, Rock) => results should ===(Right(EveryoneLosesCombat))
            case (Rock, Paper) => results should ===(Right(DefenderWinsCombat(defender)))
            case (Rock, Scissor) => results should ===(Right(AttackerWinsCombat(attacker)))

            case (Paper, Rock) => results should ===(Right(AttackerWinsCombat(attacker)))
            case (Paper, Paper) => results should ===(Right(EveryoneLosesCombat))
            case (Paper, Scissor) => results should ===(Right(DefenderWinsCombat(defender)))

            case (Scissor, Rock) => results should ===(Right(DefenderWinsCombat(defender)))
            case (Scissor, Paper) => results should ===(Right(AttackerWinsCombat(attacker)))
            case (Scissor, Scissor) => results should ===(Right(EveryoneLosesCombat))
          }
        }
      }
    }
  }
}
