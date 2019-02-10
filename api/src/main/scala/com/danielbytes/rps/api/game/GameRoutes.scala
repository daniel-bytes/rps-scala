package com.danielbytes.rps.api.game

import akka.actor.ActorSystem
import akka.event.Logging
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.directives.MethodDirectives.get
import akka.http.scaladsl.server.directives.RouteDirectives.complete
import akka.http.scaladsl.server.directives.PathDirectives.path
import com.danielbytes.rps.api.ApplicationSessionDirectives._
import com.danielbytes.rps.engine.GameRules
import com.danielbytes.rps.services.ApplicationServiceSyntax._
import com.danielbytes.rps.model.GameId
import com.danielbytes.rps.services.GameService
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import io.circe.generic.auto._

trait GameRoutes extends GameService {
  implicit def system: ActorSystem
  implicit def rules: GameRules

  lazy val gameRoutes: Route = pathPrefix("games") {
    requireSession { _ =>
      refreshSession { session =>
        path(Segment / "moves") { id =>
          post {
            entity(as[GameMoveApiModel]) { req =>
              complete(
                processTurn(GameId(id), session.playerId, req.from, req.to)
                  .apiResult
                  .map(r => GameApiModel(r.game, session.playerId, r.status))
              )
            }
          }
        } ~
          path(Segment) { id =>
            pathEndOrSingleSlash {
              get {
                complete(
                  getGame(GameId(id), session.playerId)
                    .apiResult
                    .map(r => GameApiModel(r.game, session.playerId, r.status))
                )
              } ~
                //post {},
                delete {
                  complete(
                    deleteGame(GameId(id), session.playerId)
                  )
                }
            }
          }
      }
    }
  }
}
