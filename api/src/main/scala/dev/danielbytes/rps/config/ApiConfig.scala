package dev.danielbytes.rps.config

import com.typesafe.config.Config

/**
 * API configuration
 * @param interface The network interface to run the API server on
 * @param port The port to run the API server on
 */
case class ApiConfig(
  interface: String,
  port: Int)

object ApiConfig {

  /**
   * Creates a new ApiConfig from a Typesafe Config
   */
  def apply(config: Config): ApiConfig =
    ApiConfig(
      interface = config.getString("interface"),
      port = config.getInt("port"))
}
