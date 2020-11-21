package dev.danielbytes.rps.services

import akka.actor.ActorSystem
import dev.danielbytes.rps.rules._
import dev.danielbytes.rps.services.auth._
import dev.danielbytes.rps.services.repositories.RedisGameRepository
import dev.danielbytes.rps.config.ApplicationConfig
import dev.danielbytes.rps.services.repositories.GameRepository
import redis.RedisClient

import scala.concurrent.ExecutionContext

/**
 * Services DI container, creates default implementations of
 * all service types given Akka actor system, configuration and rules engines.
 */
class Services(
  system: ActorSystem,
  config: ApplicationConfig,
  rules: Rules)(implicit ec: ExecutionContext) {

  /**
   * GameRepository instance, for performing CRUD on underlying game storage
   */
  lazy val gameRepository: GameRepository = new RedisGameRepository(
    RedisClient(
      host = config.redis.host,
      port = config.redis.port,
      password = config.redis.password)(system))

  /**
   * GameService instance, for executing application business logic
   */
  lazy val gameService: GameService = new GameService.Impl(
    gameRepository,
    rules.gameRules,
    rules.aiRules,
    rules.boardRules)

  /**
   * Google authenticator
   */
  lazy val googleAuthenticationService = new GoogleAuthenticationService.Impl()

  /**
   * Anonymous authenticator
   */
  lazy val anonymousAuthenticationService = new AnonymousAuthenticationService.Impl()
}
