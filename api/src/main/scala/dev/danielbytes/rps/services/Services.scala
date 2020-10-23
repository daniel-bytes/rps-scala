package dev.danielbytes.rps.services

import akka.actor.ActorSystem
import dev.danielbytes.rps.rules._
import dev.danielbytes.rps.services.auth._
import dev.danielbytes.rps.services.repositories.RedisGameRepository
import dev.danielbytes.rps.config.ApplicationConfig
import dev.danielbytes.rps.services.repositories.GameRepository
import redis.RedisClient

import scala.concurrent.ExecutionContext

class Services(
  system: ActorSystem,
  config: ApplicationConfig,
  rules: Rules)(implicit ec: ExecutionContext) {

  lazy val gameRepository: GameRepository = new RedisGameRepository(
    RedisClient(
      host = config.redis.host,
      port = config.redis.port,
      password = config.redis.password)(system))

  lazy val gameService: GameService = new GameService.Impl(
    gameRepository,
    rules.gameRules,
    rules.aiRules,
    rules.boardRules)

  lazy val googleAuthenticationService = new GoogleAuthenticationService.Impl()

  lazy val anonymousAuthenticationService = new AnonymousAuthenticationService.Impl()
}
