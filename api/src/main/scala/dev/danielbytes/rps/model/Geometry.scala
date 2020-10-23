package dev.danielbytes.rps.model

sealed trait StartPosition
case object StartPositionTop extends StartPosition
case object StartPositionBottom extends StartPosition

case class Point(x: Int, y: Int)

case class Geometry(rows: Int, columns: Int) {

  def contains(point: Point): Boolean = {
    point.x >= 0 &&
      point.x < columns &&
      point.y >= 0 &&
      point.y < rows
  }
}
