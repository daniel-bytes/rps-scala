package dev.danielbytes.rps.api.home

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route

/**
 * Home routes, used to serve the frontend assets.
 */
class HomeRoutes() {

  def routes: Route =
    get {
      pathEndOrSingleSlash {
        getFromResource("build/index.html")
      } ~ {
        getFromResourceDirectory("build")
      }
    }
}
