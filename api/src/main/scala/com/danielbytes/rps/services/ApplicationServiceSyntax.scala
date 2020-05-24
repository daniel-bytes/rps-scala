package com.danielbytes.rps.services

import akka.event.LoggingAdapter
import com.danielbytes.rps.model.{ ApplicationError, ApplicationErrorException }

import scala.concurrent.{ ExecutionContext, Future }

object ApplicationServiceSyntax {

  implicit class ApiResultSyntax[T, TErr <: ApplicationError](
      f: Future[Either[TErr, T]]
  )(implicit ec: ExecutionContext) {

    /**
     * Converts a standard service result (error as Left(err))
     * into an API result (error as a failed Future)
     * @return
     */
    def apiResult(maybeLogger: Option[LoggingAdapter] = None): Future[T] =
      f map {
        case Left(err) => {
          maybeLogger.foreach(_.warning(s"API error: $err"))
          throw ApplicationErrorException(err)
        }
        case Right(data) => {
          maybeLogger.foreach(_.info(s"API success: $data"))
          data
        }
      }
  }
}
