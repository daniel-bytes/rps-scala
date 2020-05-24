package com.danielbytes.rps.services.repositories

import com.danielbytes.rps.model.{ GameId, UserId }
import io.circe.parser.decode
import redis.RedisClient
import io.circe.generic.auto._
import io.circe.parser._
import io.circe.syntax._

import scala.concurrent.{ ExecutionContext, Future }

/**
 * Redis repository shared trait
 */
trait RedisRepository {
  implicit def ec: ExecutionContext
  implicit def client: RedisClient

  import SerializationModels._

  /**
   * Redis queries (fetches)
   */
  object Queries {
    def getGame(id: GameId): Future[Option[GameModel]] = {
      client
        .get(Keys.game(id))
        .map(_.flatMap(json => decode[SerializationModels.GameModel](json.utf8String).toOption))
    }

    def getGames(ids: Seq[GameId]): Future[Seq[Option[GameModel]]] = {
      Future.sequence(ids.map(Queries.getGame))
    }

    def getPlayerGameIds(id: UserId): Future[Set[GameId]] = {
      client
        .smembers(Keys.playerGames(id))
        .map(_.map(x => GameId(x.utf8String)).toSet)
    }
  }

  /**
   * Redis commands (writes / deletes)
   */
  object Commands {
    final val ttl = 60 * 60 * 24 // 1 day

    def setGame(id: GameId, model: GameModel): Future[Boolean] = {
      for {
        setResult <- client.set(Keys.game(id), model.asJson.toString())
        expireResult <- if (setResult) client.expire(Keys.game(id), ttl)
        else Future.successful(false)
      } yield setResult && expireResult
    }

    def setPlayerGameId(userId: UserId, gameId: GameId): Future[Boolean] = {
      for {
        saddResult <- client.sadd(Keys.playerGames(userId), gameId.value).map(_ > 0)
        expireResult <- if (saddResult) client.expire(Keys.playerGames(userId), ttl)
        else Future.successful(false)
      } yield saddResult && expireResult
    }

    def setPlayersGameId(userIds: Seq[UserId], gameId: GameId): Future[Seq[Boolean]] = {
      Future.sequence(
        userIds.map(userId => Commands.setPlayerGameId(userId, gameId))
      )
    }

    def removePlayerGameId(userId: UserId, gameId: GameId): Future[Boolean] = {
      client
        .srem(Keys.playerGames(userId), gameId.value)
        .map(_ > 0)
    }

    def removePlayersGameId(userIds: Seq[UserId], gameId: GameId): Future[Seq[Boolean]] = {
      Future.sequence(
        userIds.map(userId => Commands.removePlayerGameId(userId, gameId))
      )
    }

    def removeGame(id: GameId): Future[Boolean] = {
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
