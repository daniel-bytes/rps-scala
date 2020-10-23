package dev.danielbytes.rps.helpers

import java.util.UUID
import scala.util.Random

trait RandomHelper {
  def nextUUID(): String = UUID.randomUUID().toString.replace("-", "")
  def nextInt(): Int = Random.nextInt()
  def nextDouble(): Double = Random.nextDouble()
  def shuffle[T](seq: Seq[T]): Seq[T] = Random.shuffle(seq)
}
