package dev.danielbytes.rps.api

import akka.http.scaladsl.server._
import Directives._
import akka.actor.typed.ActorSystem
import akka.http.scaladsl.model.StatusCodes
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import dev.danielbytes.rps.model.ApplicationErrorException
import io.circe.generic.auto._
import org.slf4j.Logger

// Error models
case class ErrorResponse(code: String)

trait ApplicationErrorHandler {

  def rejectionHandler(logger: Logger): RejectionHandler =
    RejectionHandler
      .newBuilder()
      .handle {
        case AuthorizationFailedRejection =>
          logger.warn("AuthorizationFailedRejection")
          complete(StatusCodes.Unauthorized, ErrorResponse("unauthorized"))
        case r: AuthenticationFailedRejection =>
          logger.warn(s"AuthenticationFailedRejection: $r")
          complete(StatusCodes.Unauthorized, ErrorResponse("unauthorized"))
        case e: Exception => {
          logger.error("Internal Server Error", e)
          complete(StatusCodes.InternalServerError, ErrorResponse("internal-server-error"))
        }
      }
      .result()

  def exceptionHandler(logger: Logger): ExceptionHandler =
    ExceptionHandler {
      case e: ApplicationErrorException => {
        logger.error("Application Error", e)
        complete(e.error.status, ErrorResponse(dasherize(e.error)))
      }
    }

  // Convert SomeFooError to some-foo
  private def dasherize[T](t: T): String =
    "[A-Z\\d]".r
      .replaceAllIn(t.getClass.getSimpleName, m => "-" + m.group(0).toLowerCase())
      .replace("$", "")
      .stripPrefix("-")
      .stripSuffix("-error")
}
