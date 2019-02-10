package com.danielbytes.rps.api

import akka.http.scaladsl.server.{ Directive0, Directive1 }
import com.danielbytes.rps.model.PlayerId
import com.softwaremill.session.SessionDirectives._
import com.softwaremill.session.SessionOptions._
import com.softwaremill.session._

import scala.util.Try

case class SessionData(
  playerId: PlayerId
)

object ApplicationSessionManager {
  val serializer = new MultiValueSessionSerializer[SessionData](
    s => Map("player_id" -> s.playerId.value),
    m => Try(SessionData(PlayerId(m("player_id"))))
  )
  // TODO: Get from config
  val secret = "some_very_long_secret_and_random_string_some_very_long_secret_and_random_string"
  val sessionEncoder = new BasicSessionEncoder[SessionData]()(serializer)
  val sessionConfig: SessionConfig = SessionConfig.default(secret)

  implicit val sessionManager: SessionManager[SessionData] =
    new SessionManager[SessionData](sessionConfig)(sessionEncoder)

  val sessionType: SessionContinuity[SessionData] = oneOff
  val sessionTransport: SetSessionTransport = usingHeaders
}

object ApplicationSessionDirectives {
  import ApplicationSessionManager._

  def createSession(data: SessionData): Directive0 = setSession(sessionType, sessionTransport, data)
  val requireSession: Directive1[SessionData] = requiredSession(sessionType, sessionTransport)
  val refreshSession: Directive1[SessionData] = touchRequiredSession(sessionType, sessionTransport)
  val terminateSession: Directive0 = invalidateSession(sessionType, sessionTransport)
}