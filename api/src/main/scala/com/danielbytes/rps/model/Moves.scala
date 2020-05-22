package com.danielbytes.rps.model

sealed trait MoveDirection
case object MoveForward extends MoveDirection
case object MoveReverse extends MoveDirection
case object MoveLeft extends MoveDirection
case object MoveRight extends MoveDirection

sealed trait MoveResult {
  def from: Point
  def to: Point
  def direction: MoveDirection
}

case class TakePositionMove(
  from: Point,
  to: Point,
  direction: MoveDirection
) extends MoveResult

case class AttackMove(
  from: Point,
  to: Point,
  direction: MoveDirection,
  attacker: Token,
  defender: Token
) extends MoveResult

case class MoveSummary(
  playerId: UserId,
  from: Point,
  to: Point,
  combatResult: Option[CombatResult]
)