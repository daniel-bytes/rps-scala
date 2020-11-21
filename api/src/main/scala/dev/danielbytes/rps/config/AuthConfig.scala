package dev.danielbytes.rps.config

import com.typesafe.config.Config

/**
 * Authn/z configuration
 * @param session Auth session configuration
 * @param google Google OAuth configuration
 */
case class AuthConfig(
  session: SessionConfig,
  google: GoogleConfig)

object AuthConfig {

  /**
   * Creates a new AuthConfig from a Typesafe Config
   */
  def apply(config: Config): AuthConfig =
    AuthConfig(
      session = SessionConfig(config.getConfig("session")),
      google = GoogleConfig(config.getConfig("google")))
}
