package dev.danielbytes.rps.config

import com.typesafe.config.Config

/**
 * Google OAuth config
 * @param clientId The Google API client id
 * @param clientSecret The Google API client secret
 * @param redirectUri The Google OAuth redirect URI
 */
case class GoogleConfig(
  clientId: String,
  clientSecret: String,
  redirectUri: String)

object GoogleConfig {

  /**
   * Creates a new GoogleConfig from a Typesafe Config
   */
  def apply(config: Config): GoogleConfig =
    GoogleConfig(
      clientId = config.getString("client-id"),
      clientSecret = config.getString("client-secret"),
      redirectUri = config.getString("redirect-uri"))
}
