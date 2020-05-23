package com.danielbytes.rps.model

sealed trait TokenType {
  def name: String
}

case object Rock extends TokenType {
  def name: String = "Rock"
}

case object Paper extends TokenType {
  def name: String = "Paper"
}

case object Scissor extends TokenType {
  def name: String = "Scissor"
}

case object Bomb extends TokenType {
  def name: String = "Bomb"
}

case object Flag extends TokenType {
  def name: String = "Flag"
}

case class Token(
    owner: UserId,
    tokenType: TokenType
) {
  def movable: Boolean = tokenType match {
    case Bomb | Flag => false
    case _ => true
  }
}

object Token {
  final val types: List[TokenType] = Rock :: Paper :: Scissor :: Bomb :: Flag :: Nil
  final val other = "Other"
}