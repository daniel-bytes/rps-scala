package com.danielbytes.rps.services

import akka.actor.ActorSystem
import com.danielbytes.rps.rules._
import com.danielbytes.rps.services.auth._
import com.danielbytes.rps.helpers._
import com.danielbytes.rps.services.repositories.GameRepository

import scala.concurrent.ExecutionContext

trait Services {
  implicit def ec: ExecutionContext
  implicit def dateTime: DateTimeHelper
  implicit def random: RandomHelper
  implicit def gameRepository: GameRepository
  implicit def gameRules: GameRules
  implicit def boardRules: BoardRules
  implicit def aiRules: PlayerAIRules
  implicit def system: ActorSystem

  implicit lazy val gameService: GameService = new GameServiceImpl()
  implicit lazy val authService: AuthenticationService = new AuthenticationService()
}
