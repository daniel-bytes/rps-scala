package dev.danielbytes.rps.api.session

import akka.actor.typed.ActorSystem
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.directives.RouteDirectives.complete
import dev.danielbytes.rps.api.Encoders
import dev.danielbytes.rps.services.auth.{
  AnonymousAuthenticationService,
  AnonymousTokenRequest,
  GoogleAuthenticationService,
  GoogleTokenRequest
}
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import io.circe.generic.auto._

import scala.concurrent.{ ExecutionContext, Future }

/**
 * Session routes, used to serve the session management APIs.
 */
class SessionRoutes(
  val system: ActorSystem[Nothing],
  val googleAuthenticationService: GoogleAuthenticationService,
  val anonymousAuthenticationService: AnonymousAuthenticationService)(implicit val ec: ExecutionContext)
  extends ApplicationSessionDirectives
  with Encoders {
  import SessionApiModel._

  def routes: Route =
    pathPrefix("session") {
      path("google") {
        post {
          entity(as[GoogleTokenRequest]) { token =>
            authenticate(token) { user =>
              createSession(user) { session =>
                complete(Future.successful(Session(session)))
              }
            }
          }
        }
      } ~
        path("anonymous") {
          post {
            entity(as[AnonymousTokenRequest]) { token =>
              authenticate(token) { user =>
                createSession(user) { session =>
                  complete(Future.successful(Session(session)))
                }
              }
            }
          }
        } ~
        pathEndOrSingleSlash {
          concat(
            get {
              requireSession { session =>
                complete(Future.successful(Session(session)))
              }
            },
            delete {
              terminateSession {
                complete(Future.successful(()))
              }
            })
        }
    }
}
