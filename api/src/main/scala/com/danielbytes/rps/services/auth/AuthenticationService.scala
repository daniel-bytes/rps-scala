package com.danielbytes.rps.services.auth

import com.danielbytes.rps.model.{ AuthenticationError, User }
import com.danielbytes.rps.helpers.DateTimeHelper

import scala.concurrent.{ ExecutionContext, Future }

trait TokenRequest {
  def id: String
  def name: String
}

class AuthenticationService()(
    implicit
    val dateTime: DateTimeHelper,
    val ec: ExecutionContext
) {
  type Response = Future[Either[AuthenticationError, User]]

  def authenticate(token: TokenRequest): Response = {
    token match {
      case gt: GoogleTokenRequest =>
        new GoogleAuthenticationService().authenticate(gt)
    }
  }
}