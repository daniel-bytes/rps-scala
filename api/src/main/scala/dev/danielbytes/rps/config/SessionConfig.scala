package dev.danielbytes.rps.config

import com.typesafe.config.Config

case class SessionConfig(
  key: String)

object SessionConfig {

  def apply(config: Config): SessionConfig =
    SessionConfig(
      key = config.getString("key"))
}
