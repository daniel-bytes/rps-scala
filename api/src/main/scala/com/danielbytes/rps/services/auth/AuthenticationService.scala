package com.danielbytes.rps.services.auth

import com.danielbytes.rps.model.{ AuthenticationError, User }
import com.danielbytes.rps.helpers.DateTimeHelper

import scala.concurrent.{ ExecutionContext, Future }

trait TokenRequest {
  def id: String
  def name: String
}

trait AuthenticationService {
  type Response = Future[Either[AuthenticationError, User]]

  def authenticate(token: TokenRequest): Response
}

class AuthenticationServiceImpl(
    val dateTime: DateTimeHelper
)(
    implicit
    val ec: ExecutionContext
) extends AuthenticationService {
  private val google: GoogleAuthenticationService = new GoogleAuthenticationServiceImpl(dateTime)

  def authenticate(token: TokenRequest): Response = {
    token match {
      case gt: GoogleTokenRequest =>
        google.authenticate(gt)
    }
  }
}