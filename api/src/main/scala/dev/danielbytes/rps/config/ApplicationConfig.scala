package dev.danielbytes.rps.config

import com.typesafe.config.{ Config, ConfigFactory }

/**
 * Application level configuration
 * @param auth Authn/z configuration
 * @param redis Redis configuration
 * @param api API server configuration
 */
case class ApplicationConfig(
  auth: AuthConfig,
  redis: RedisConfig,
  api: ApiConfig)

object ApplicationConfig {

  /**
   * Creates a new ApplicationConfig from a Typesafe Config
   */
  def apply(config: Config): ApplicationConfig =
    ApplicationConfig(
      auth = AuthConfig(config.getConfig("auth")),
      redis = RedisConfig(config.getConfig("redis")),
      api = ApiConfig(config.getConfig("api")))

  /**
   * Default global configuration object instance,
   * build from the application.conf resource.
   */
  lazy val instance: ApplicationConfig = ApplicationConfig(
    ConfigFactory.load.getConfig("rps"))
}
