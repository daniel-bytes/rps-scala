package dev.danielbytes.rps.services

import dev.danielbytes.rps.model.{ ApplicationError, ApplicationErrorException }
import org.slf4j.Logger

import scala.concurrent.{ ExecutionContext, Future }

/**
 * Extension method typeclasses for converting domain service responses
 * to API responses.
 */
object ApplicationServiceSyntax {

  implicit class ApiResultSyntax[T, TErr <: ApplicationError](f: Future[Either[TErr, T]])(implicit ec: ExecutionContext) {

    /**
     * Converts a standard service result (error as Left(err))
     * into an API result (error as a failed Future)
     */
    def apiResult(maybeLogger: Option[Logger] = None): Future[T] =
      f map {
        case Left(err) => {
          maybeLogger.foreach(_.warn(s"API error: $err"))
          throw ApplicationErrorException(err)
        }
        case Right(data) => {
          maybeLogger.foreach(_.info(s"API success: $data"))
          data
        }
      }
  }
}
