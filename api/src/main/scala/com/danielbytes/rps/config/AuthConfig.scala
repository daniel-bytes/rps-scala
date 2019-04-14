package com.danielbytes.rps.config

import com.typesafe.config.Config

case class AuthConfig(
  session: SessionConfig,
  google: GoogleConfig
)

object AuthConfig {
  def apply(config: Config): AuthConfig = AuthConfig(
    session = SessionConfig(config.getConfig("session")),
    google = GoogleConfig(config.getConfig("google"))
  )
}
