package dev.danielbytes.rps.rules

/**
 * Wrapper class that houses instances of all rule engine default implementations
 */
class Rules {
  lazy val combatRules: CombatRules = new CombatRules.Impl()
  lazy val moveRules: MoveRules = new MoveRules.Impl()
  lazy val aiRules: PlayerAIRules = new PlayerAIRules.Impl(moveRules)
  lazy val boardRules: BoardRules = new BoardRules.Impl()
  lazy val gameRules: GameRules = new GameRules.Impl(moveRules, combatRules)
}
