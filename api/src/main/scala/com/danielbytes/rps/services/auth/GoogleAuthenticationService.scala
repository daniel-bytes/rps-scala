package com.danielbytes.rps.services.auth

import java.io.IOException

import com.danielbytes.rps.config.ApplicationConfig
import com.danielbytes.rps.model._
import com.danielbytes.rps.helpers.DateTimeHelper
import com.google.api.client.googleapis.auth.oauth2.{ GoogleAuthorizationCodeTokenRequest, GoogleTokenResponse }
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.jackson2.JacksonFactory

import scala.concurrent.{ ExecutionContext, Future }

case class GoogleTokenRequest(
  id: String,
  name: String,
  authCode: String
) extends TokenRequest

class GoogleAuthenticationService()(
    implicit
    dateTimeHelper: DateTimeHelper,
    ec: ExecutionContext
) {
  type Request = GoogleTokenRequest
  type Response[T] = Future[Either[AuthenticationError, T]]

  def authenticate(request: Request): Response[User] =
    authenticateGoogle(createAuthnRequest(request)).map(
      _.map(_ => createUser(request))
    )

  private def authenticateGoogle(
    request: GoogleAuthorizationCodeTokenRequest
  ): Response[GoogleTokenResponse] =
    Future.successful {
      try {
        Right(request.execute())
      } catch {
        case ex: IOException =>
          Left(AuthenticationFailedError(ex.getLocalizedMessage))
      }
    }

  private def createAuthnRequest(
    request: GoogleTokenRequest
  ): GoogleAuthorizationCodeTokenRequest = {
    val config = ApplicationConfig.instance.auth.google

    new GoogleAuthorizationCodeTokenRequest(
      new NetHttpTransport(),
      JacksonFactory.getDefaultInstance,
      config.clientId,
      config.clientSecret,
      request.authCode,
      config.redirectUri
    )
  }

  private def createUser(r: GoogleTokenRequest): User =
    User.google(
      UserId(r.id),
      UserName(r.name)
    )
}
