package dev.danielbytes.rps.services.auth

import dev.danielbytes.rps.model.{ User, UserId, UserName }

import scala.concurrent.{ ExecutionContext, Future }

case class AnonymousTokenRequest(id: String, name: String) extends TokenRequest

/**
 * Anonymous user authenticator
 */
trait AnonymousAuthenticationService extends AuthenticationService {
  type Request = AnonymousTokenRequest
}

object AnonymousAuthenticationService {

  class Impl()(implicit ec: ExecutionContext) extends AnonymousAuthenticationService {

    def authenticate(request: Request): Response = {
      Future.successful(Right(User.anonymous(UserId(request.id), UserName(request.name))))
    }
  }
}
