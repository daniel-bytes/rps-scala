package com.danielbytes.rps.config

import com.typesafe.config.{ Config, ConfigFactory }

case class ApplicationConfig(
  auth: AuthConfig
)

object ApplicationConfig {
  def apply(config: Config): ApplicationConfig = ApplicationConfig(
    auth = AuthConfig(config.getConfig("auth"))
  )

  lazy val instance: ApplicationConfig = ApplicationConfig(
    ConfigFactory.load.getConfig("rps")
  )
}
