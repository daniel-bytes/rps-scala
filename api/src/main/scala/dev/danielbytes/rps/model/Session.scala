package dev.danielbytes.rps.model

import scala.util.Try
import java.util.UUID

case class SessionId(value: String) extends AnyVal

object SessionId {

  def newId(): SessionId =
    SessionId(
      UUID.randomUUID().toString.replace("-", ""))
}

case class Session(
  sessionId: SessionId,
  userId: UserId,
  userName: UserName,
  source: UserSource.Value) {
  import Session.Keys

  def toMap: Map[String, String] =
    Map(
      Keys.SessionId -> sessionId.value,
      Keys.UserId -> userId.value,
      Keys.UserName -> userName.value,
      Keys.Source -> source.toString)
}

object Session {

  object Keys {
    final val SessionId = "session_id"
    final val UserId = "user_id"
    final val UserName = "user_name"
    final val Source = "source"
  }

  def apply(map: Map[String, String]): Try[Session] =
    Try {
      Session(
        SessionId(map(Keys.SessionId)),
        UserId(map(Keys.UserId)),
        UserName(map(Keys.UserName)),
        UserSource.withName(map(Keys.Source)))
    }

  def apply(user: User): Session =
    Session(
      SessionId.newId(),
      user.id,
      user.name,
      user.source)
}
