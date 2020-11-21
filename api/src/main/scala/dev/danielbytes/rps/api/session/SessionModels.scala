package dev.danielbytes.rps.api.session

import dev.danielbytes.rps.{ model => Domain }

/**
 * API session models
 */
object SessionApiModel {

  /**
   * An active game session for a player
   * @param sessionId The id of the player session
   * @param userId The id of the player
   * @param userName The name of the player
   * @param source The type of session (
   */
  case class Session(
    sessionId: String,
    userId: String,
    userName: String,
    source: String) {

    /**
     * Converts the API Session to a domain Session
     */
    def toSession(): Domain.Session =
      Domain.Session(
        Domain.SessionId(this.sessionId),
        Domain.UserId(this.userId),
        Domain.UserName(this.userName),
        Domain.UserSource.withName(this.source))
  }

  object Session {

    /**
     * Converts a domain Session to an API Session
     */
    def apply(session: Domain.Session): Session =
      Session(
        session.sessionId.toString,
        session.userId.toString,
        session.userName.toString,
        session.source.toString)
  }

}