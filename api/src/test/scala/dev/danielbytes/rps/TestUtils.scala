package dev.danielbytes.rps

import scala.concurrent.duration._
import scala.concurrent.{ Await, Future }

trait TestUtils {

  def wait[T](future: Future[T], duration: Duration = 1.minute): T = Await.result(future, duration)
}
