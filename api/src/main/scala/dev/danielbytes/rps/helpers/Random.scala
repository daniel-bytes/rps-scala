package dev.danielbytes.rps.helpers

import java.util.UUID
import scala.util.Random

/**
 * Random helpers.
 * Simpler wrappers around Random static function,
 * useful for testing non-deterministic behavior.
 */
trait RandomHelper {
  /**
   * Creates a new UUID
   */
  def nextUUID(): String = UUID.randomUUID().toString.replace("-", "")

  /**
   * Gets the next random integer
   */
  def nextInt(): Int = Random.nextInt()

  /**
   * Gets the next random double
   */
  def nextDouble(): Double = Random.nextDouble()

  /**
   * Returns a new sequence with the order of elements
   * randomly shuffled.
   */
  def shuffle[T](seq: Seq[T]): Seq[T] = Random.shuffle(seq)
}
