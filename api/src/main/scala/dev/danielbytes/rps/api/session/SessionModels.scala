package dev.danielbytes.rps.api.session

import dev.danielbytes.rps.model._

class SessionModels {}

case class SessionApiModel(
  sessionId: String,
  userId: String,
  userName: String,
  source: String) {

  def toSession(): Session =
    Session(
      SessionId(this.sessionId),
      UserId(this.userId),
      UserName(this.userName),
      UserSource.withName(this.source))
}

object SessionApiModel {

  def apply(session: Session): SessionApiModel =
    SessionApiModel(
      session.sessionId.toString,
      session.userId.toString,
      session.userName.toString,
      session.source.toString)
}
