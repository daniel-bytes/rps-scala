package dev.danielbytes.rps.config

import com.typesafe.config.Config

case class ApiConfig(
  interface: String,
  port: Int)

object ApiConfig {

  def apply(config: Config): ApiConfig =
    ApiConfig(
      interface = config.getString("interface"),
      port = config.getInt("port"))
}
