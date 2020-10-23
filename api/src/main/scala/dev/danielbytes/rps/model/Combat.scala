package dev.danielbytes.rps.model

/**
 * The result of a combat move (one token attacking another)
 */
sealed trait CombatResult {
  def attacker: Token
  def defender: Token
  def winner: Option[Token]
}

/**
 * Attacker token wins
 */
case class AttackerWinsCombat(attacker: Token, defender: Token) extends CombatResult {
  def winner: Option[Token] = Some(attacker)
}

/**
 * Defending token wins
 */
case class DefenderWinsCombat(attacker: Token, defender: Token) extends CombatResult {
  def winner: Option[Token] = Some(defender)
}

/**
 * Both tokens lose
 */
case class EveryoneLosesCombat(attacker: Token, defender: Token) extends CombatResult {
  def winner: Option[Token] = None
}
