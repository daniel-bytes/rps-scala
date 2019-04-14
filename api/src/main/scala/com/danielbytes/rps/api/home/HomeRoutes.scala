package com.danielbytes.rps.api.home

import akka.actor.ActorSystem
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route

import scala.concurrent.ExecutionContext

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

