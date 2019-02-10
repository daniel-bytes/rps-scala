package com.danielbytes.rps.api.home

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

trait HomeRoutes {
  implicit def ec: ExecutionContext
  implicit def system: ActorSystem

  lazy val homeRoutes: Route =
    get {
      pathEndOrSingleSlash {
        getFromResource("build/index.html")
      } ~ {
        getFromResourceDirectory("build")
      }
    }
}

