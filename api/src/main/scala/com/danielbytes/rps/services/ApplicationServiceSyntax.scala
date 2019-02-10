package com.danielbytes.rps.services

import akka.http.javadsl.server.directives.RouteDirectives
import akka.http.scaladsl.server.StandardRoute
import cats.data.EitherT
import cats.implicits._
import com.danielbytes.rps.model.{ ApplicationError, ApplicationErrorException }

import scala.concurrent.{ ExecutionContext, Future }

object ApplicationServiceSyntax {
  implicit class ApiResultSyntax[T](
      f: Future[Either[ApplicationError, T]]
  )(
      implicit
      ec: ExecutionContext
  ) {
    /**
     * Converts a standard service result (error as Left(err))
     * into an API result (error as a failed Future)
     * @return
     */
    def apiResult: Future[T] = f map {
      case Left(err) => throw ApplicationErrorException(err)
      case Right(data) => data
    }
  }
}
