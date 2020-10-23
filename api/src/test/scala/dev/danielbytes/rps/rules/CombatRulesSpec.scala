package dev.danielbytes.rps.rules

import dev.danielbytes.rps.GameTestData
import dev.danielbytes.rps.model._
import org.scalactic.TypeCheckedTripleEquals
import org.scalatest.wordspec.AnyWordSpec

class CombatRulesSpec extends AnyWordSpec with TypeCheckedTripleEquals with GameTestData {
  val subject = new CombatRules.Impl()

  "CombatRules" should {
    "handle attackPlayer rules" should {
      for { attackerToken <- Token.types; defenderToken <- Token.types } {
        s"$attackerToken attacks $defenderToken" in {
          val attacker = Token(uid1, attackerToken)
          val defender = Token(uid2, defenderToken)
          val results = subject.attackPlayer(attacker, defender)

          (attackerToken, defenderToken) match {
            case (Bomb | Flag, _) => assert(results === Left(TokenNotCapableOfAttackError))
            case (_, Bomb) => assert(results === Right(EveryoneLosesCombat(attacker, defender)))
            case (_, Flag) => assert(results === Right(AttackerWinsCombat(attacker, defender)))

            case (Rock, Rock) => assert(results === Right(EveryoneLosesCombat(attacker, defender)))
            case (Rock, Paper) => assert(results === Right(DefenderWinsCombat(attacker, defender)))
            case (Rock, Scissor) => assert(results === Right(AttackerWinsCombat(attacker, defender)))

            case (Paper, Rock) => assert(results === Right(AttackerWinsCombat(attacker, defender)))
            case (Paper, Paper) => assert(results === Right(EveryoneLosesCombat(attacker, defender)))
            case (Paper, Scissor) => assert(results === Right(DefenderWinsCombat(attacker, defender)))

            case (Scissor, Rock) => assert(results === Right(DefenderWinsCombat(attacker, defender)))
            case (Scissor, Paper) => assert(results === Right(AttackerWinsCombat(attacker, defender)))
            case (Scissor, Scissor) => assert(results === Right(EveryoneLosesCombat(attacker, defender)))
          }
        }
      }
    }
  }
}
