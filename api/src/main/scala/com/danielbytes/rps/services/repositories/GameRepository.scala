package com.danielbytes.rps.services.repositories

import com.danielbytes.rps.api.Encoders
import com.danielbytes.rps.config.RedisConfig
import com.danielbytes.rps.model.{ Point, StartPosition, Token, _ }
import com.redis.{ RedisClient, RedisClientPool }
import io.circe.generic.auto._
import io.circe.parser._
import io.circe.syntax._

import scala.concurrent.{ ExecutionContext, Future }
import scala.util.Try

/**
 * Repository trait for managing Game data
 */
trait GameRepository extends Repository[GameId, Game] {
  def playerGames(userId: UserId): Future[List[Game]]
}

/**
 * In-memory test GameRepository
 */
class InMemoryGameRepository()(implicit ec: ExecutionContext)
    extends InMemoryRepository[GameId, Game]
    with GameRepository {

  override def playerGames(userId: UserId): Future[List[Game]] =
    query().map(
      _.filter {
      case (_, game) => game.playerList.map(_.user.id).contains(userId)
    }.map {
      case (_, game) => game
    }.toList
    )

  override def get(id: GameId): Future[Option[Game]] =
    super.get(id)

  override def set(
    id: GameId,
    model: Game
  ): Future[Unit] = super.set(id, model)

  override def remove(id: GameId): Future[Unit] = super.remove(id)
}

/**
 * Production GameRepository using Redis as a backend
 */
class RedisGameRepository(
    config: RedisConfig
)(
    implicit
    ec: ExecutionContext
) extends GameRepository {
  import SerializationModels._

  private val redis = new RedisClientPool(config.host, config.port)

  override def playerGames(userId: UserId): Future[List[Game]] = {
    withClient { client =>
      Queries.getPlayerGameIds(client, userId)
        .flatMap(Queries.getGame(client, _))
        .map(model => GameModel.toGame(model).get)
        .toList
    }
  }

  override def get(id: GameId): Future[Option[Game]] = {
    withClient { client =>
      Queries
        .getGame(client, id)
        .map(model => GameModel.toGame(model).get)
    }
  }

  override def set(
    id: GameId,
    model: Game
  ): Future[Unit] = {
    withClient { client =>
      Commands.setGame(client, id, GameModel(model))

      model.userList.foreach(p =>
        Commands.setPlayerGameId(client, p.id, id))
    }
  }

  override def remove(id: GameId): Future[Unit] = {
    withClient { client =>
      Queries
        .getGame(client, id)
        .foreach { model =>
          val game = GameModel.toGame(model).get
          Commands.removeGame(client, id)

          game.userList.foreach(p =>
            Commands.removePlayerGameId(client, p.id, id))
        }
    }
  }

  private def withClient[T](body: RedisClient => T): Future[T] = {
    redis.withClient { client =>
      Future {
        body(client)
      }
    }
  }

  /**
   * Redis queries (fetches)
   */
  private object Queries {
    import SerializationModels._

    def getGame(client: RedisClient, id: GameId): Option[GameModel] = {
      client
        .get(Keys.game(id))
        .flatMap(json => decode[SerializationModels.GameModel](json).toOption)
    }

    def getPlayerGameIds(client: RedisClient, id: UserId): Set[GameId] = {
      client
        .smembers(Keys.playerGames(id))
        .getOrElse(Set())
        .flatten
        .map(GameId)
    }
  }

  /**
   * Redis commands (writes / deletes)
   */
  private object Commands {
    import SerializationModels._

    def setGame(client: RedisClient, id: GameId, model: GameModel): Boolean = {
      client.set(Keys.game(id), model.asJson)
    }

    def setPlayerGameId(client: RedisClient, userId: UserId, gameId: GameId): Option[Boolean] = {
      client
        .sadd(Keys.playerGames(userId), gameId.value)
        .map(_ > 0)
    }

    def removePlayerGameId(client: RedisClient, userId: UserId, gameId: GameId): Option[Boolean] = {
      client
        .srem(Keys.playerGames(userId), gameId.value)
        .map(_ > 0)
    }

    def removeGame(client: RedisClient, id: GameId): Option[Boolean] = {
      client.del(Keys.game(id)).map(_ > 0)
    }
  }

  /**
   * Redis string key builders
   */
  private object Keys {
    def game(id: GameId): String = s"game:${id.value}"
    def playerGames(id: UserId): String = s"player-games:${id.value}"
  }
}

/**
 * Serialization models
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

    private def toTokenType(s: String) = Try {
      s match {
        case "rock" => Rock
        case "paper" => Paper
        case "scissor" => Scissor
        case "bomb" => Bomb
        case "flag" => Flag
        case _ => throw SerializationException(s"Invalid TokenType [$s]")
      }
    }

    private def toPoint(s: String) = Try {
      s.split(":") match {
        case Array(x, y) =>
          Try {
            Point(x.toInt, y.toInt)
          }
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