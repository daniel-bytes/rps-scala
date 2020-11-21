package dev.danielbytes.rps.services.auth

import java.io.IOException

import dev.danielbytes.rps.model._
import com.google.api.client.googleapis.auth.oauth2.{ GoogleAuthorizationCodeTokenRequest, GoogleTokenResponse }
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.jackson2.JacksonFactory
import dev.danielbytes.rps.config.ApplicationConfig
import dev.danielbytes.rps.helpers.DateTimeHelper
import dev.danielbytes.rps.model.AuthenticationError

import scala.concurrent.{ ExecutionContext, Future }

/**
 * Google OAuth token request
 * @param id The id of the Google user principal
 * @param name The name of the Google user
 * @param authCode The Google auth code
 */
case class GoogleTokenRequest(
  id: String,
  name: String,
  authCode: String) extends TokenRequest

/**
 * Google OAuth service contract
 */
trait GoogleAuthenticationService extends AuthenticationService {
  type Request = GoogleTokenRequest
}

object GoogleAuthenticationService extends DateTimeHelper {

  /**
   * Default implementation of the Google OAuth service
   */
  class Impl()(implicit ec: ExecutionContext) extends GoogleAuthenticationService {
    private type GoogleAuthnResponse = Future[Either[AuthenticationError, GoogleTokenResponse]]

    /**
     * Authenticates a Google Oauth request
     */
    def authenticate(request: Request): Response =
      authenticateGoogle(createAuthnRequest(request)).map(_.map(_ => createUser(request)))

    private def authenticateGoogle(
      request: GoogleAuthorizationCodeTokenRequest): GoogleAuthnResponse =
      Future.successful {
        try {
          Right(request.execute())
        } catch {
          case ex: IOException =>
            Left(AuthenticationFailedError(ex.getLocalizedMessage))
        }
      }

    private def createAuthnRequest(
      request: GoogleTokenRequest): GoogleAuthorizationCodeTokenRequest = {
      val config = ApplicationConfig.instance.auth.google

      new GoogleAuthorizationCodeTokenRequest(
        new NetHttpTransport(),
        JacksonFactory.getDefaultInstance,
        config.clientId,
        config.clientSecret,
        request.authCode,
        config.redirectUri)
    }

    private def createUser(r: GoogleTokenRequest): User =
      User.google(UserId(r.id), UserName(r.name))
  }
}
