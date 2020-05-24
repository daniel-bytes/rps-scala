package com.danielbytes.rps.services.auth

import com.danielbytes.rps.model.{ User, UserId, UserName }

import scala.concurrent.{ ExecutionContext, Future }

case class AnonymousTokenRequest(id: String, name: String) extends TokenRequest

trait AnonymousAuthenticationService extends AuthenticationService {
  type Request = AnonymousTokenRequest
}

/**
 * Anonymous user authenticator
 */
class AnonymousAuthenticationServiceImpl()(implicit ec: ExecutionContext)
    extends AnonymousAuthenticationService {

  def authenticate(request: Request): Response = {
    Future.successful(
      Right(User.anonymous(UserId(request.id), UserName(request.name)))
    )
  }
}
