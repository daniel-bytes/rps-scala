package com.danielbytes.rps.services.repositories

import com.danielbytes.rps.model._

import scala.concurrent.Future
import scala.util.Try

/**
 * Repository trait for managing Game data
 */
trait GameRepository extends Repository[GameId, Game] {
  /**
   * Return all Games belonging to a User
   */
  def listPlayerGames(userId: UserId): Future[List[Game]]
}

/**
 * Internal models used for serialization
 */
private[repositories] object SerializationModels {
  case class GameModel(
    gameId: String,
    player1Id: String,
    player1Name: String,
    player1Source: String,
    player1StartPosition: String,
    player1IsAI: Boolean,
    player2Id: String,
    player2Name: String,
    player2Source: String,
    player2StartPosition: String,
    player2IsAI: Boolean,
    currentPlayerId: String,
    rows: Int,
    columns: Int,
    tokens: Map[String, String]
  )

  object GameModel {
    def toGame(m: GameModel): Try[Game] = {
      for {
        s1 <- toStartPosition(m.player1StartPosition)
        s2 <- toStartPosition(m.player2StartPosition)
        tokens <- toTokens(m.tokens)
      } yield Game(
        id = GameId(m.gameId),
        player1 = Player(
          user = User(
            id = UserId(m.player1Id),
            name = UserName(m.player1Name),
            source = UserSource.withName(m.player1Source),
            isAI = m.player1IsAI
          ),
          position = s1
        ),
        player2 = Player(
          user = User(
            id = UserId(m.player2Id),
            name = UserName(m.player2Name),
            source = UserSource.withName(m.player2Source),
            isAI = m.player2IsAI
          ),
          position = s2
        ),
        currentPlayerId = UserId(m.currentPlayerId),
        board = Board(
          geometry = Geometry(m.rows, m.columns),
          tokens = tokens
        )
      )
    }

    def apply(model: Game): GameModel = GameModel(
      gameId = model.id.value,
      player1Id = model.player1.user.id.value,
      player1Name = model.player1.user.name.value,
      player1Source = model.player1.user.source.toString,
      player1StartPosition = toString(model.player1.position),
      player1IsAI = model.player1.isAI,
      player2Id = model.player2.user.id.value,
      player2Name = model.player2.user.name.value,
      player2Source = model.player2.user.source.toString,
      player2StartPosition = toString(model.player2.position),
      player2IsAI = model.player2.isAI,
      currentPlayerId = model.currentPlayerId.value,
      rows = model.board.geometry.rows,
      columns = model.board.geometry.columns,
      tokens = model.board.tokens.map { case (p, t) => (toString(p), toString(t)) }
    )

    private def toString(s: StartPosition) = s match {
      case StartPositionTop => "top"
      case StartPositionBottom => "bottom"
    }

    private def toString(p: Point): String = {
      s"${p.x}:${p.y}"
    }

    private def toString(t: Token): String = {
      s"${t.owner.value}:${t.tokenType.name}"
    }

    private def toStartPosition(s: String): Try[StartPosition] = Try {
      s match {
        case "top" => StartPositionTop
        case "bottom" => StartPositionBottom
        case _ => throw SerializationException(s"Invalid StartPosition [$s]")
      }
    }

    private def toTokenType(s: String): Try[TokenType] = Try {
      Token.types.find(_.name == s).getOrElse(throw SerializationException(s"Invalid TokenType [$s]"))
    }

    private def toPoint(s: String): Try[Point] = Try {
      s.split(":") match {
        case Array(x, y) =>
          Try(Point(x.toInt, y.toInt))
            .getOrElse(throw SerializationException(s"Invalid Point [$s]"))
        case _ => throw SerializationException(s"Invalid Point [$s]")
      }
    }

    private def toToken(s: String): Try[Token] = {
      s.split(":") match {
        case Array(owner, tokenType) =>
          toTokenType(tokenType).map { t =>
            Token(UserId(owner), t)
          }
        case _ => Try { throw SerializationException(s"Invalid Point [$s]") }
      }
    }

    private def toTokens(t: Map[String, String]): Try[Map[Point, Token]] = Try {
      t.foldLeft(Map[Point, Token]()) {
        case (result, (pt, t)) =>
          result + (for {
            point <- toPoint(pt)
            token <- toToken(t)
          } yield (point, token)).get
      }
    }
  }
}