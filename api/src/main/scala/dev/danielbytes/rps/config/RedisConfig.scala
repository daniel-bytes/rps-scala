package dev.danielbytes.rps.config

import akka.http.scaladsl.model.Uri
import com.typesafe.config.Config

/**
 * Redis client configuration
 * @param url The URL to the Redis instance
 */
case class RedisConfig(
  url: String) {
  /**
   * The Redis URL, as a Uri object
   */
  private lazy val uri: Uri = Uri(url)

  /**
   * The Redis URL host
   */
  lazy val host: String = uri.authority.host.address()

  /**
   * The Redis URL port
   */
  lazy val port: Int = uri.authority.port

  /**
   * The Redis URL username
   */
  lazy val user: Option[String] =
    Some(uri.authority.userinfo).map(_.split(':').head).filter(_.nonEmpty)

  /**
   * The Redis URL password
   */
  lazy val password: Option[String] = Some(uri.authority.userinfo).flatMap(_.split(':') match {
    case Array(_, password) => Some(password)
    case _ => None
  })
}

object RedisConfig {

  /**
   * Creates a new RedisConfig from a Typesafe Config
   */
  def apply(config: Config): RedisConfig =
    RedisConfig(
      url = config.getString("url"))
}
