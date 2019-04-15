package com.danielbytes.rps.config

import com.typesafe.config.Config

case class RedisConfig(
  host: String,
  port: Int
)

object RedisConfig {
  def apply(config: Config): RedisConfig = RedisConfig(
    host = config.getString("host"),
    port = config.getInt("port")
  )
}
