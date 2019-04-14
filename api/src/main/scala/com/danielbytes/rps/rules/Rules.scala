package com.danielbytes.rps.rules

import com.danielbytes.rps.helpers.{ DateTimeHelper, RandomHelper }

trait Rules {
  implicit def random: RandomHelper
  implicit def dateTime: DateTimeHelper

  implicit lazy val combatRules: CombatRules = new CombatRulesEngine()
  implicit lazy val moveRules: MoveRules = new MoveRulesEngine()
  implicit lazy val aiRules: PlayerAIRules = new PlayerAIRulesEngine()
  implicit lazy val boardRules: BoardRules = new BoardRulesEngine()
  implicit lazy val gameRules: GameRules = new GameRulesEngine()
}
