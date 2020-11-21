package dev.danielbytes.rps.helpers

import java.time.format.DateTimeFormatter
import java.time.{ Clock, LocalDateTime }

import scala.util.Try

/**
 * DateTime helpers.
 * Simpler wrappers around LocalDateTime static function,
 * useful for testing non-deterministic behavior.
 */
trait DateTimeHelper {
  /**
   * Minimum LocalDateTime value
   */
  def min(): LocalDateTime = LocalDateTime.MIN

  /**
   * Maximum LocalDateTime value
   */
  def max(): LocalDateTime = LocalDateTime.MAX

  /**
   * Current UTC LocalDateTime
   */
  def now(): LocalDateTime = LocalDateTime.now(Clock.systemUTC())

  /**
   * Parses a string representation of a LocalDateTime
   * @param s The LocalDateTime as a string
   * @param format The LocalDateTimeFormatter
   * @return The LocalDateTime, or an error
   */
  def parse(s: String, format: Option[DateTimeFormatter] = None): Try[LocalDateTime] =
    Try {
      LocalDateTime.parse(s, format.getOrElse(DateTimeFormatter.RFC_1123_DATE_TIME))
    }
}
