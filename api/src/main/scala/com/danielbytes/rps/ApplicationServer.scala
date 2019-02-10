package com.danielbytes.rps

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import com.danielbytes.rps.api.ApplicationErrorHandler
import com.danielbytes.rps.api.game.GameRoutes
import com.danielbytes.rps.api.home.HomeRoutes
import com.danielbytes.rps.api.session.SessionRoutes
import com.danielbytes.rps.engine.{ GameRules, GameRulesEngine, PlayerAIRules, PlayerAIRulesEngine }
import com.danielbytes.rps.model._
import com.danielbytes.rps.services.{ InMemoryRepository, Repository }

import scala.concurrent.{ Await, ExecutionContext }
import scala.concurrent.duration.Duration

object ApplicationServer
    extends App
    with GameRoutes
    with HomeRoutes
    with SessionRoutes {
  implicit val system: ActorSystem = ActorSystem("GameServer")
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val ec: ExecutionContext = system.dispatcher

  implicit val rules: GameRules = GameRulesEngine
  implicit val aiRules: PlayerAIRules = PlayerAIRulesEngine
  implicit val repository: Repository[GameId, Game] = new InMemoryRepository[GameId, Game](
    GameId("game1") -> Game(
      GameId("game1"),
      Player(PlayerId("player1"), "Player 1", StartPositionBottom, isAI = false),
      Player(PlayerId("player2"), "Player 2", StartPositionTop, isAI = true),
      PlayerId("player1"),
      Board(
        Geometry(3, 3),
        Map(
          Point(0, 0) -> Token(PlayerId("player1"), Flag),
          Point(0, 1) -> Token(PlayerId("player1"), Rock),
          Point(0, 2) -> Token(PlayerId("player2"), Scissor),
          Point(2, 2) -> Token(PlayerId("player2"), Flag)
        )
      )
    )
  )

  import ApplicationErrorHandler._
  lazy val routes: Route =
    gameRoutes ~
      sessionRoutes ~
      homeRoutes

  Http().bindAndHandle(routes, "localhost", 8080)

  println(s"Server online at http://localhost:8080/")

  Await.result(system.whenTerminated, Duration.Inf)
}
