package com.danielbytes.rps.model

/**
 * A player's Identifier
 * @param value
 */
case class PlayerId(value: String) extends AnyVal

/**
 * A game player (either human or AI)
 */
case class Player(
  id: PlayerId,
  name: String,
  position: StartPosition,
  isAI: Boolean
)
