package com.danielbytes.rps.config

import com.typesafe.config.{ Config, ConfigFactory }

case class ApplicationConfig(
  auth: AuthConfig,
  redis: RedisConfig,
  api: ApiConfig
)

object ApplicationConfig {
  def apply(config: Config): ApplicationConfig = ApplicationConfig(
    auth = AuthConfig(config.getConfig("auth")),
    redis = RedisConfig(config.getConfig("redis")),
    api = ApiConfig(config.getConfig("api"))
  )

  lazy val instance: ApplicationConfig = ApplicationConfig(
    ConfigFactory.load.getConfig("rps")
  )
}
