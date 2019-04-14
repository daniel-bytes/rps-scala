package com.danielbytes.rps.api.session

import akka.actor.ActorSystem
import akka.event.Logging
import akka.http.scaladsl.server._
import akka.http.scaladsl.server.Directives._
import com.danielbytes.rps.config.ApplicationConfig
import com.danielbytes.rps.model._
import com.danielbytes.rps.services.ApplicationServiceSyntax._
import com.danielbytes.rps.services.GameService
import com.danielbytes.rps.services.auth._
import com.softwaremill.session.SessionDirectives._
import com.softwaremill.session.SessionOptions._
import com.softwaremill.session._

import scala.concurrent.ExecutionContext

object ApplicationSessionManager {
  val config = ApplicationConfig.instance

  val serializer = new MultiValueSessionSerializer[Session](
    s => s.toMap,
    m => Session(m)
  )

  val secret = config.auth.session.key
  val sessionEncoder = new BasicSessionEncoder[Session]()(serializer)
  val sessionConfig: SessionConfig = SessionConfig.default(secret)

  implicit val sessionManager: SessionManager[Session] =
    new SessionManager[Session](sessionConfig)(sessionEncoder)

  val sessionType: SessionContinuity[Session] = oneOff
  val sessionTransport: SetSessionTransport = usingHeaders
}

trait ApplicationSessionDirectives {
  import ApplicationSessionManager._

  implicit def authService: AuthenticationService
  implicit def ec: ExecutionContext
  implicit def system: ActorSystem
  private lazy val logger = Logging.getLogger(system, this)

  def createSession(user: User): Directive1[Session] =
    setSession(sessionType, sessionTransport, Session(user)) & provide(Session(user))

  val requireSession: Directive1[Session] =
    requiredSession(sessionType, sessionTransport)

  val refreshSession: Directive1[Session] =
    touchRequiredSession(sessionType, sessionTransport)

  val terminateSession: Directive0 =
    invalidateSession(sessionType, sessionTransport)

  def authenticate(
    request: TokenRequest
  ): Directive1[User] = {
    onSuccess(
      authService.authenticate(request).apiResult(Some(logger))
    ) flatMap provide
  }
}