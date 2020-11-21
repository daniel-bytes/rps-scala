package dev.danielbytes.rps.services.auth

import dev.danielbytes.rps.model.{ User, UserId, UserName }

import scala.concurrent.{ ExecutionContext, Future }

/**
 * Request for an anonymous token
 * @param id The player's id
 * @param name The player's name
 */
case class AnonymousTokenRequest(
  id: String,
  name: String) extends TokenRequest

/**
 * Anonymous user authenticator service contract
 */
trait AnonymousAuthenticationService extends AuthenticationService {
  type Request = AnonymousTokenRequest
}

object AnonymousAuthenticationService {

  /**
   * Default implementation of AnonymousAuthenticationService
   */
  class Impl()(implicit ec: ExecutionContext) extends AnonymousAuthenticationService {

    /**
     * Authenticates an anonymous user request
     */
    def authenticate(request: Request): Response = {
      Future.successful(Right(User.anonymous(UserId(request.id), UserName(request.name))))
    }
  }
}
