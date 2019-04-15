package com.danielbytes.rps.api.game

import akka.actor.ActorSystem
import akka.event.Logging
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.directives.MethodDirectives.get
import akka.http.scaladsl.server.directives.RouteDirectives.complete
import akka.http.scaladsl.server.directives.PathDirectives.path
import com.danielbytes.rps.api.session.ApplicationSessionDirectives
import com.danielbytes.rps.helpers.Helpers
import com.danielbytes.rps.services.ApplicationServiceSyntax._
import com.danielbytes.rps.model.{ GameId, User }
import com.danielbytes.rps.services.GameService
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import io.circe.generic.auto._

trait GameRoutes
    extends ApplicationSessionDirectives
    with Helpers {
  implicit def system: ActorSystem
  implicit def gameService: GameService
  private lazy val logger = Logging.getLogger(system, this)

  lazy val gameRoutes: Route = pathPrefix("games") {
    requireSession { _ =>
      refreshSession { session =>
        path(Segment / "moves") { id =>
          post {
            entity(as[GameMoveApiModel]) { req =>
              complete(
                gameService.processTurn(GameId(id), session.userId, req.from, req.to)
                  .apiResult(Some(logger))
                  .map(r => GameApiModel(r.game, session.userId, r.status))
              )
            }
          }
        } ~
          path(Segment) { id =>
            get {
              complete(
                gameService.getGame(GameId(id), session.userId)
                  .apiResult()
                  .map(r => GameApiModel(r.game, session.userId, r.status))
              )
            } ~
              delete {
                complete(
                  gameService.deleteGame(GameId(id), session.userId)
                )
              }
          } ~
          pathEndOrSingleSlash {
            get {
              complete(
                gameService.getPlayerGames(session.userId, includeCompleted = true)
                  .apiResult()
                  .map(r => GameOverviewsApiModel(
                    r.map(g => GameOverviewApiModel(session.userId, g))
                  ))
              )
            } ~
              post {
                complete(
                  gameService.createGame(User(session))
                    .apiResult()
                    .map(r => GameApiModel(r.game, session.userId, r.status))
                )
              }
          }
      }
    }
  }
}
