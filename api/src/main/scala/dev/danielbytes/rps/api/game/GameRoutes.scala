package dev.danielbytes.rps.api.game

import akka.actor.typed.ActorSystem
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.directives.MethodDirectives.get
import akka.http.scaladsl.server.directives.RouteDirectives.complete
import akka.http.scaladsl.server.directives.PathDirectives.path
import dev.danielbytes.rps.api.session.ApplicationSessionDirectives
import dev.danielbytes.rps.services.ApplicationServiceSyntax._
import dev.danielbytes.rps.model.{ GameId, GameVersion, User }
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import dev.danielbytes.rps.services.GameService
import dev.danielbytes.rps.services.auth.{ AnonymousAuthenticationService, GoogleAuthenticationService }
import io.circe.generic.auto._

import scala.concurrent.ExecutionContext

class GameRoutes(
  val system: ActorSystem[Nothing],
  val gameService: GameService,
  val googleAuthenticationService: GoogleAuthenticationService,
  val anonymousAuthenticationService: AnonymousAuthenticationService)(implicit val ec: ExecutionContext)
  extends ApplicationSessionDirectives {
  val logger = system.log

  def routes: Route =
    pathPrefix("games") {
      requireSession { _ =>
        refreshSession { session =>
          path(Segment / "moves") { id =>
            post {
              entity(as[GameMoveApiModel]) { req =>
                complete(
                  gameService
                    .processTurn(GameId(id), session.userId, req.from, req.to, GameVersion(req.version))
                    .apiResult(Some(logger))
                    .map(r => GameApiModel(r, session.userId)))
              }
            }
          } ~
            path(Segment) { id =>
              get {
                complete(
                  gameService
                    .getGame(GameId(id), session.userId)
                    .apiResult(Some(logger))
                    .map(r => GameApiModel(r, session.userId)))
              } ~
                delete {
                  complete(gameService.deleteGame(GameId(id), session.userId))
                }
            } ~
            pathEndOrSingleSlash {
              get {
                complete(
                  gameService
                    .getPlayerGames(session.userId, includeCompleted = true)
                    .apiResult(Some(logger))
                    .map(r => GameOverviewsApiModel(r.map(g => GameOverviewApiModel(session.userId, g)))))
              } ~
                post {
                  complete(
                    gameService
                      .createGame(User(session))
                      .apiResult(Some(logger))
                      .map(r => GameApiModel(r, session.userId)))
                }
            }
        }
      }
    }
}
