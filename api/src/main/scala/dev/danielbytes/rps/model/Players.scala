package dev.danielbytes.rps.model

import dev.danielbytes.rps.helpers.DateTimeHelper

/**
 * A game player (either human or AI)
 */
case class Player(user: User, position: StartPosition) {
  def id: UserId = user.id
  def name: UserName = user.name
  def isAI: Boolean = user.isAI
  def isHuman: Boolean = !isAI
}

object Player {
  def ai(position: StartPosition): Player = Player(User.ai(), position)

  def player(
    userId: UserId,
    userName: UserName,
    position: StartPosition): Player =
    Player(User.anonymous(userId, userName), position)
}
