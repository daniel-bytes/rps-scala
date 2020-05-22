package com.danielbytes.rps.helpers

trait Helpers {
  def random: RandomHelper = new RandomHelper {}
  def dateTime: DateTimeHelper = new DateTimeHelper {}
}
