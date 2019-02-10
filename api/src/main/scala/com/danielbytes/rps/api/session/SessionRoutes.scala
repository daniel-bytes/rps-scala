package com.danielbytes.rps.api.session

import akka.actor.ActorSystem
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.directives.RouteDirectives.complete
import com.danielbytes.rps.api.ApplicationSessionDirectives._
import com.danielbytes.rps.api.SessionData
import com.danielbytes.rps.model.PlayerId
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import io.circe.generic.auto._

import scala.concurrent.{ ExecutionContext, Future }

trait SessionRoutes {
  implicit def ec: ExecutionContext
  implicit def system: ActorSystem

  lazy val sessionRoutes: Route = pathPrefix("session") {
    pathEndOrSingleSlash {
      concat(
        get {
          requireSession { session =>
            complete(
              Future(session)
            )
          }
        },
        post {
          entity(as[CreateSessionRequest]) { req =>
            val session = SessionData(PlayerId(req.name))

            createSession(session) {
              complete(
                Future(session)
              )
            }
          }
        },
        delete {
          terminateSession {
            complete(
              Future(())
            )
          }
        }
      )
    }
  }
}

