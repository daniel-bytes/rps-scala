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

/**
 * Session manager services
 */
object ApplicationSessionManager {

  /**
   * Serializer data serializer
   */
  val serializer =
    new MultiValueSessionSerializer[Session](s => s.toMap, m => Session(m))

  /**
   * Session secret key
   */
  val secret = ApplicationConfig.instance.auth.session.key

  /**
   * Session serialization encoder
   */
  val sessionEncoder = new BasicSessionEncoder[Session]()(serializer)

  /**
   * Session configuration
   */
  val sessionConfig: SessionConfig = SessionConfig.default(secret)

  /**
   * Implicit SessionManager class instance
   */
  implicit val sessionManager: SessionManager[Session] =
    new SessionManager[Session](sessionConfig)(sessionEncoder)

  /**
   * The type of session in use
   */
  val sessionType: SessionContinuity[Session] = oneOff

  /**
   * The transport used for sessions
   */
  val sessionTransport: SetSessionTransport = usingHeaders
}

/**
 * Directives used to help session and application routes manage sessions
 */
trait ApplicationSessionDirectives {
  import ApplicationSessionManager._

  def system: ActorSystem[Nothing]
  implicit def ec: ExecutionContext

  /**
   * Gets an AuthenticationService backed by Google OAuth
   */
  def googleAuthenticationService: GoogleAuthenticationService

  /**
   * Gets an AuthenticationService backed by simple anonymous user tokens
   */
  def anonymousAuthenticationService: AnonymousAuthenticationService

  /**
   * Creates a new Session for the supplied user
   */
  def createSession(user: User): Directive1[Session] =
    setSession(sessionType, sessionTransport, Session(user)) & provide(Session(user))

  /**
   * Ensures the current request has a valid session
   */
  val requireSession: Directive1[Session] =
    requiredSession(sessionType, sessionTransport)

  /**
   * Refreshes the session in the current request
   */
  val refreshSession: Directive1[Session] =
    touchRequiredSession(sessionType, sessionTransport)

  /**
   * Terminates the current session
   */
  val terminateSession: Directive0 =
    invalidateSession(sessionType, sessionTransport)

  /**
   * Authenticates a token, providing a User on success
   */
  def authenticate(token: TokenRequest): Directive1[User] = {
    onSuccess(authenticateToken(token).apiResult(Some(system.log))) flatMap provide
  }

  // Authenticates a Google or anonymous token
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
