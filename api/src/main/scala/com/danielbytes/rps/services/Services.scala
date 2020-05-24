package com.danielbytes.rps.services

import akka.actor.ActorSystem
import com.danielbytes.rps.ApplicationServer.{ config, dateTime, gameRepository }
import com.danielbytes.rps.config.ApplicationConfig
import com.danielbytes.rps.rules._
import com.danielbytes.rps.services.auth._
import com.danielbytes.rps.helpers._
import com.danielbytes.rps.services.repositories.{
  GameRepository,
  RedisGameRepository
}
import redis.RedisClient

import scala.concurrent.ExecutionContext

trait Services {
  implicit def ec: ExecutionContext
  implicit def system: ActorSystem

  def config: ApplicationConfig
  def dateTime: DateTimeHelper
  def random: RandomHelper
  def gameRules: GameRules
  def boardRules: BoardRules
  def aiRules: PlayerAIRules

  lazy val gameRepository: GameRepository = new RedisGameRepository(
    RedisClient(
      host = config.redis.host,
      port = config.redis.port,
      password = config.redis.password
    )
  )

  lazy val gameService: GameService =
    new GameServiceImpl(gameRepository, gameRules, aiRules, boardRules, random)

  lazy val googleAuthenticationService = new GoogleAuthenticationServiceImpl(
    dateTime
  )
  lazy val anonymousAuthenticationService =
    new AnonymousAuthenticationServiceImpl()
}
