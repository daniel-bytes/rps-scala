package com.danielbytes.rps.services

import akka.actor.ActorSystem
import com.danielbytes.rps.rules._
import com.danielbytes.rps.services.auth._
import com.danielbytes.rps.helpers._
import com.danielbytes.rps.services.repositories.GameRepository

import scala.concurrent.ExecutionContext

trait Services {
  implicit def ec: ExecutionContext
  implicit def system: ActorSystem

  def dateTime: DateTimeHelper
  def random: RandomHelper
  def gameRepository: GameRepository
  def gameRules: GameRules
  def boardRules: BoardRules
  def aiRules: PlayerAIRules

  lazy val gameService: GameService = new GameServiceImpl(
    gameRepository,
    gameRules,
    aiRules,
    boardRules,
    random
  )

  lazy val authService: AuthenticationService = new AuthenticationServiceImpl(dateTime)
}
