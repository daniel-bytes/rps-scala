package dev.danielbytes.rps.config

import com.typesafe.config.Config

case class GoogleConfig(
  clientId: String,
  clientSecret: String,
  redirectUri: String)

object GoogleConfig {

  def apply(config: Config): GoogleConfig =
    GoogleConfig(
      clientId = config.getString("client-id"),
      clientSecret = config.getString("client-secret"),
      redirectUri = config.getString("redirect-uri"))
}
