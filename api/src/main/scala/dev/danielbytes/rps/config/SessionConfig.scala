package dev.danielbytes.rps.config

import com.typesafe.config.Config

/**
 * API session configuration
 * @param key The session token secret key
 */
case class SessionConfig(
  key: String)

object SessionConfig {

  /**
   * Creates a new SessionConfig from a Typesafe Config
   */
  def apply(config: Config): SessionConfig =
    SessionConfig(
      key = config.getString("key"))
}
