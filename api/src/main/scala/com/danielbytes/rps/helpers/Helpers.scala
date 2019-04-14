package com.danielbytes.rps.helpers

trait Helpers {
  implicit lazy val random: RandomHelper = new RandomHelper {}
  implicit lazy val dateTime: DateTimeHelper = new DateTimeHelper {}
}
