package dev.danielbytes.rps.helpers

import java.time.format.DateTimeFormatter
import java.time.{ Clock, LocalDateTime }

import scala.util.Try

trait DateTimeHelper {
  def min(): LocalDateTime = LocalDateTime.MIN
  def max(): LocalDateTime = LocalDateTime.MAX
  def now(): LocalDateTime = LocalDateTime.now(Clock.systemUTC())

  def parse(s: String, format: Option[DateTimeFormatter] = None): Try[LocalDateTime] =
    Try {
      LocalDateTime.parse(s, format.getOrElse(DateTimeFormatter.RFC_1123_DATE_TIME))
    }
}
