package com.danielbytes.rps.api

import akka.http.scaladsl.server._
import Directives._
import akka.actor.ActorSystem
import akka.event.Logging
import akka.http.scaladsl.model.{ HttpResponse, StatusCodes }
import com.danielbytes.rps.model._
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import io.circe.generic.auto._

// Error models
case class ErrorResponse(code: String)

object ApplicationErrorHandler {
  implicit def handleRejections(implicit system: ActorSystem): RejectionHandler =
    RejectionHandler.newBuilder()
      .handle {
        case AuthorizationFailedRejection =>
          complete(
            StatusCodes.Unauthorized,
            ErrorResponse("unauthorized")
          )
        case e: Exception => {
          println(e.toString)
          complete(
            StatusCodes.InternalServerError,
            ErrorResponse("internal-server-error")
          )
        }
      }.result()

  implicit def handleExceptions: ExceptionHandler = ExceptionHandler {
    case e: ApplicationErrorException => {
      complete(
        e.error.status,
        ErrorResponse(dasherize(e.error))
      )
    }
  }

  // Convert SomeFooError to some-foo
  private def dasherize[T](t: T): String =
    "[A-Z\\d]".r
      .replaceAllIn(
        t.getClass.getSimpleName,
        m => "-" + m.group(0).toLowerCase()
      )
      .replace("$", "")
      .stripPrefix("-")
      .stripSuffix("-error")
}

