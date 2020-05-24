package com.danielbytes.rps

import java.time.LocalDateTime

import akka.actor.ActorSystem
import akka.event.Logging
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.directives.DebuggingDirectives
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import com.danielbytes.rps.api.{ ApplicationErrorHandler, Encoders }
import com.danielbytes.rps.api.game.GameRoutes
import com.danielbytes.rps.api.home.HomeRoutes
import com.danielbytes.rps.api.session.SessionRoutes
import com.danielbytes.rps.config.ApplicationConfig
import com.danielbytes.rps.rules.Rules
import com.danielbytes.rps.services._
import com.danielbytes.rps.helpers.Helpers
import com.danielbytes.rps.services.repositories._

import scala.concurrent.{ Await, ExecutionContext }
import scala.concurrent.duration.Duration

object ApplicationServer
    extends App
    with Helpers
    with GameRoutes
    with HomeRoutes
    with SessionRoutes
    with Rules
    with Services
    with Encoders {
  implicit val system: ActorSystem = ActorSystem("GameServer")
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val ec: ExecutionContext = system.dispatcher
  implicit val config: ApplicationConfig = ApplicationConfig.instance

  val gameRepository: GameRepository = new AkkaRedisGameRepository(config.redis)
  //new RedisGameRepository(config.redis)

  import ApplicationErrorHandler._

  lazy val routes: Route =
    gameRoutes ~
      homeRoutes ~
      sessionRoutes

  val clientRouteLogged = DebuggingDirectives.logRequestResult("API", Logging.InfoLevel)(routes)
  Http().bindAndHandle(clientRouteLogged, config.api.interface, config.api.port)

  println(s"Server online at http://${config.api.interface}:${config.api.port}/")

  Await.result(system.whenTerminated, Duration.Inf)
}
