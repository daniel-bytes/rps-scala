package com.danielbytes.rps.api.session

import akka.actor.ActorSystem
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.directives.RouteDirectives.complete
import com.danielbytes.rps.api.Encoders
import com.danielbytes.rps.services.auth.GoogleTokenRequest
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import io.circe.generic.auto._

import scala.concurrent.{ ExecutionContext, Future }

trait SessionRoutes
    extends ApplicationSessionDirectives
    with Encoders {
  implicit def ec: ExecutionContext
  implicit def system: ActorSystem

  lazy val sessionRoutes: Route = pathPrefix("session") {
    path("google") {
      post {
        entity(as[GoogleTokenRequest]) { gsession =>
          authenticate(gsession) { user =>
            createSession(user) { session =>
              complete(
                Future.successful(SessionApiModel(session))
              )
            }
          }
        }
      }
    } ~
      pathEndOrSingleSlash {
        concat(
          get {
            requireSession { session =>
              complete(
                Future.successful(SessionApiModel(session))
              )
            }
          },
          delete {
            terminateSession {
              complete(
                Future.successful(())
              )
            }
          }
        )
      }
  }
}