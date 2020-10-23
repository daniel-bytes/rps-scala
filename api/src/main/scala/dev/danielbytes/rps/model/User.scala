package dev.danielbytes.rps.model

/**
 * The authentication source for a user
 */
object UserSource extends Enumeration {
  type UserSource = Value
  val BuiltIn, Anonymous, Google = Value
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
  isAI: Boolean = false)

object User {
  final val aiId: UserId = UserId("ai")
  final val aiName: UserName = UserName("Player 2")
  final val unknownName: UserName = UserName("unknown")

  /**
   * The builtin AI user
   */
  def ai(): User = User(aiId, aiName, UserSource.BuiltIn, isAI = true)

  def anonymous(userId: UserId, userName: UserName): User =
    User(userId, userName, UserSource.Anonymous)

  def google(userId: UserId, userName: UserName): User =
    User(userId, userName, UserSource.Google)

  def apply(session: Session): User =
    session.source match {
      case UserSource.Google =>
        User.google(session.userId, session.userName)
      case UserSource.Anonymous =>
        User.anonymous(session.userId, session.userName)
    }
}
