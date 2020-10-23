package dev.danielbytes.rps.api.session

import akka.actor.typed.ActorSystem
import akka.http.scaladsl.server._
import akka.http.scaladsl.server.Directives._
import dev.danielbytes.rps.model._
import dev.danielbytes.rps.services.ApplicationServiceSyntax._
import dev.danielbytes.rps.services.auth._
import com.softwaremill.session.SessionDirectives._
import com.softwaremill.session.SessionOptions._
import com.softwaremill.session._
import dev.danielbytes.rps.config.ApplicationConfig

import scala.concurrent.{ ExecutionContext, Future }

object ApplicationSessionManager {

  val serializer =
    new MultiValueSessionSerializer[Session](s => s.toMap, m => Session(m))

  val secret = ApplicationConfig.instance.auth.session.key
  val sessionEncoder = new BasicSessionEncoder[Session]()(serializer)
  val sessionConfig: SessionConfig = SessionConfig.default(secret)

  implicit val sessionManager: SessionManager[Session] =
    new SessionManager[Session](sessionConfig)(sessionEncoder)

  val sessionType: SessionContinuity[Session] = oneOff
  val sessionTransport: SetSessionTransport = usingHeaders
}

trait ApplicationSessionDirectives {
  import ApplicationSessionManager._

  def googleAuthenticationService: GoogleAuthenticationService
  def anonymousAuthenticationService: AnonymousAuthenticationService

  def system: ActorSystem[Nothing]
  implicit def ec: ExecutionContext

  def createSession(user: User): Directive1[Session] =
    setSession(sessionType, sessionTransport, Session(user)) & provide(Session(user))

  val requireSession: Directive1[Session] =
    requiredSession(sessionType, sessionTransport)

  val refreshSession: Directive1[Session] =
    touchRequiredSession(sessionType, sessionTransport)

  val terminateSession: Directive0 =
    invalidateSession(sessionType, sessionTransport)

  def authenticate(token: TokenRequest): Directive1[User] = {
    onSuccess(authenticateToken(token).apiResult(Some(system.log))) flatMap provide
  }

  private def authenticateToken(token: TokenRequest): AuthenticationService#Response = {
    token match {
      case gt: GoogleTokenRequest =>
        googleAuthenticationService.authenticate(gt)
      case at: AnonymousTokenRequest =>
        anonymousAuthenticationService.authenticate(at)
      case _ =>
        Future.successful(Left(UnknownTokenType))
    }
  }
}
