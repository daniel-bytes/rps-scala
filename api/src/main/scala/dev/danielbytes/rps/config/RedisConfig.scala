package dev.danielbytes.rps.config

import akka.http.scaladsl.model.Uri
import com.typesafe.config.Config

case class RedisConfig(
  url: String) {
  private lazy val uri: Uri = Uri(url)

  lazy val host: String = uri.authority.host.address()
  lazy val port: Int = uri.authority.port
  lazy val user: Option[String] = Some(uri.authority.userinfo).map(_.split(':').head).filter(_.nonEmpty)

  lazy val password: Option[String] = Some(uri.authority.userinfo).flatMap(_.split(':') match {
    case Array(_, password) => Some(password)
    case _ => None
  })
}

object RedisConfig {

  def apply(config: Config): RedisConfig =
    RedisConfig(
      url = config.getString("url"))
}
