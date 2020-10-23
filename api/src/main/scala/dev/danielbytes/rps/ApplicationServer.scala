package dev.danielbytes.rps

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.adapter._
import akka.event.Logging
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import dev.danielbytes.rps.api.{ ApplicationErrorHandler, Encoders }
import dev.danielbytes.rps.api.home.HomeRoutes
import dev.danielbytes.rps.api.session.SessionRoutes
import dev.danielbytes.rps.api.game.GameRoutes
import dev.danielbytes.rps.config.ApplicationConfig
import dev.danielbytes.rps.rules.Rules
import dev.danielbytes.rps.services.Services

import scala.concurrent.duration.Duration
import scala.concurrent.{ Await, ExecutionContext }

object ApplicationServer extends ApplicationErrorHandler with Encoders {

  def main(args: Array[String]): Unit = {
    // Setup Actor system
    // -- We need to start with a legacy system for the Redis client which doesn't support typed actors
    val legacySystem: akka.actor.ActorSystem = akka.actor.ActorSystem("GameServer")
    implicit val system: ActorSystem[Nothing] = legacySystem.toTyped
    implicit val ec: ExecutionContext = system.executionContext

    // Setup Services
    val config: ApplicationConfig = ApplicationConfig.instance
    val rules: Rules = new Rules()
    val services: Services = new Services(legacySystem, config, rules)

    // Setup Routes
    val homeRoutes: HomeRoutes = new HomeRoutes()
    val gameRoutes: GameRoutes = new GameRoutes(
      system,
      services.gameService,
      services.googleAuthenticationService,
      services.anonymousAuthenticationService)
    val sessionRoutes: SessionRoutes = new SessionRoutes(
      system,
      services.googleAuthenticationService,
      services.anonymousAuthenticationService)

    lazy val routes: Route =
      logRequestResult("api", Logging.InfoLevel) {
        handleRejections(rejectionHandler(system.log)) {
          handleExceptions(exceptionHandler(system.log)) {
            homeRoutes.routes ~
              gameRoutes.routes ~
              sessionRoutes.routes
          }
        }
      }

    // Setup server
    Http()(system)
      .newServerAt(config.api.interface, config.api.port)
      .bind(routes)

    println(s"Server online at http://${config.api.interface}:${config.api.port}/")

    Await.result(system.whenTerminated, Duration.Inf)

    println("Server offline, process exiting")
  }
}
