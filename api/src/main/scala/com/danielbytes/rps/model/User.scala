package com.danielbytes.rps.model

import com.danielbytes.rps.helpers.DateTimeHelper

/**
 * The authentication source for a user
 */
object UserSource extends Enumeration {
  type UserSource = Value
  val Native, Google = Value
}

/**
 * An authenticated user's Identifier
 */
case class UserId(value: String) extends AnyVal

/**
 * An authenticated user's name
 */
case class UserName(value: String) extends AnyVal

/**
 * An authenticated user
 */
case class User(
  id: UserId,
  name: UserName,
  source: UserSource.Value,
  isAI: Boolean = false
)

object User {
  final val aiId: UserId = UserId("ai")
  final val aiName: UserName = UserName("AI")
  final val unknownName: UserName = UserName("unknown")

  /**
   * The builtin AI user
   */
  def ai(): User = User(
    aiId,
    aiName,
    UserSource.Native,
    isAI = true
  )

  def native(
    userId: UserId,
    userName: UserName
  ): User = User(
    userId,
    userName,
    UserSource.Native
  )

  def google(
    userId: UserId,
    userName: UserName
  ): User = User(
    userId,
    userName,
    UserSource.Google
  )

  def apply(session: Session)(implicit dateTime: DateTimeHelper): User =
    session.source match {
      case UserSource.Google => User.google(session.userId, session.userName)
      case UserSource.Native => User.native(session.userId, session.userName)
    }
}
