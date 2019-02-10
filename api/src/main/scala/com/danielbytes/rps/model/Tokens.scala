package com.danielbytes.rps.model

sealed trait TokenType {
  def name: String
}

case object Rock extends TokenType {
  def name: String = "rock"
}

case object Paper extends TokenType {
  def name: String = "paper"
}

case object Scissor extends TokenType {
  def name: String = "scissor"
}

case object Bomb extends TokenType {
  def name: String = "bomb"
}

case object Flag extends TokenType {
  def name: String = "flag"
}

case class Token(
    owner: PlayerId,
    tokenType: TokenType
) {
  def movable: Boolean = tokenType match {
    case Bomb | Flag => false
    case _ => true
  }
}

object Token {
  final val types = Rock :: Paper :: Scissor :: Bomb :: Flag :: Nil
}