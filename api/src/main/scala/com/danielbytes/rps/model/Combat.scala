package com.danielbytes.rps.model

sealed trait CombatResult
case class AttackerWinsCombat(token: Token) extends CombatResult
case class DefenderWinsCombat(token: Token) extends CombatResult
case object EveryoneLosesCombat extends CombatResult

