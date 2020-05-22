package com.danielbytes.rps.rules

import com.danielbytes.rps.helpers.{ DateTimeHelper, RandomHelper }

trait Rules {
  def random: RandomHelper
  def dateTime: DateTimeHelper

  lazy val combatRules: CombatRules = new CombatRulesEngine()
  lazy val moveRules: MoveRules = new MoveRulesEngine()
  lazy val aiRules: PlayerAIRules = new PlayerAIRulesEngine(moveRules)
  lazy val boardRules: BoardRules = new BoardRulesEngine(dateTime, random)
  lazy val gameRules: GameRules = new GameRulesEngine(moveRules, combatRules)
}
