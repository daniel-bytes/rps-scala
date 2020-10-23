package dev.danielbytes.rps.model

/**
 * The direction a token is moved
 */
sealed trait MoveDirection

/**
 * Move towards the enemies' side of the board
 */
case object MoveForward extends MoveDirection

/**
 * Move towards the player's side of the board
 */
case object MoveBackward extends MoveDirection

/**
 * Move board-left
 */
case object MoveLeft extends MoveDirection

/**
 * Move board-right
 */
case object MoveRight extends MoveDirection

/**
 * Distance of move from the player's home side of the board
 */
case class MoveDistance(value: Int) extends AnyVal

/**
 * A potential move a player token can take
 */
sealed trait PotentialMove {
  def from: Point
  def to: Point
  def direction: MoveDirection
  def distance: MoveDistance
}

/**
 * A move resulting in a token taking an empty space
 */
case class TakePositionMove(
  from: Point,
  to: Point,
  direction: MoveDirection,
  distance: MoveDistance) extends PotentialMove

/**
 * A move resulting in a token attacking an enemy token
 */
case class AttackMove(
  from: Point,
  to: Point,
  direction: MoveDirection,
  attacker: Token,
  defender: Token,
  distance: MoveDistance) extends PotentialMove

/**
 * The summary of a player's move after it is resolved, containing potential combat summary
 */
case class MoveSummary(
  playerId: UserId,
  from: Point,
  to: Point,
  combatResult: Option[CombatResult])
