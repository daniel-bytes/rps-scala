package com.danielbytes.rps.services.repositories

import akka.actor.ActorSystem
import akka.util.ByteString
import com.danielbytes.rps.config.RedisConfig
import com.danielbytes.rps.model._
import redis.RedisClient
import io.circe.generic.auto._
import io.circe.parser._
import io.circe.syntax._

import scala.concurrent.{ ExecutionContext, Future }

/**
 * Production GameRepository using Redis as a backend
 */
class AkkaRedisGameRepository(
    config: RedisConfig
)(
    implicit
    ec: ExecutionContext,
    system: ActorSystem
) extends GameRepository {
  import SerializationModels._

  private val client = RedisClient(host = config.host, port = config.port, password = config.password)

  override def listPlayerGames(userId: UserId): Future[List[Game]] = {
    for {
      ids <- Queries.getPlayerGameIds(client, userId)
      games <- Future.sequence(ids.map(x => Queries.getGame(client, x)))
      result = games.flatMap(_.flatMap(x => GameModel.toGame(x).toOption)).toList
    } yield result
  }

  override def get(id: GameId): Future[Option[Game]] = {
    Queries
      .getGame(client, id)
      .map(_.flatMap(g => GameModel.toGame(g).toOption))
  }

  override def set(
    id: GameId,
    model: Game
  ): Future[Unit] = {
    for {
      _ <- Commands.setGame(client, id, GameModel(model))
      _ <- Future.sequence(
        model.userList.filter(_.isHuman).map(p => Commands.setPlayerGameId(client, p.id, id))
      )
    } yield ()
  }

  override def remove(id: GameId): Future[Unit] = {
    for {
      maybeGame <- Queries.getGame(client, id)
      maybeModel = maybeGame.flatMap(m => GameModel.toGame(m).toOption)
      _ <- maybeModel.map { model =>
        for {
          _ <- Commands.removeGame(client, id)
          _ <- Future.sequence(
            model.userList.map(user => Commands.removePlayerGameId(client, user.id, id))
          )
        } yield ()
      }.getOrElse(Future.successful(()))
    } yield ()
  }

  /**
   * Redis queries (fetches)
   */
  private object Queries {
    import SerializationModels._

    def getGame(client: RedisClient, id: GameId): Future[Option[GameModel]] = {
      client
        .get(Keys.game(id))
        .map(_.flatMap(json => decode[SerializationModels.GameModel](json.utf8String).toOption))
    }

    def getPlayerGameIds(client: RedisClient, id: UserId): Future[Set[GameId]] = {
      client
        .smembers(Keys.playerGames(id))
        .map(_.map(x => GameId(x.utf8String)).toSet)
    }
  }

  /**
   * Redis commands (writes / deletes)
   */
  private object Commands {
    import SerializationModels._

    private val ttl = 60 * 60 * 24 // 1 day

    def setGame(client: RedisClient, id: GameId, model: GameModel): Future[Boolean] = {
      for {
        setResult <- client.set(Keys.game(id), model.asJson.toString())
        expireResult <- if (setResult) client.expire(Keys.game(id), ttl)
        else Future.successful(false)
      } yield setResult && expireResult
    }

    def setPlayerGameId(client: RedisClient, userId: UserId, gameId: GameId): Future[Boolean] = {
      for {
        saddResult <- client.sadd(Keys.playerGames(userId), gameId.value).map(_ > 0)
        expireResult <- if (saddResult) client.expire(Keys.playerGames(userId), ttl)
        else Future.successful(false)
      } yield saddResult && expireResult
    }

    def removePlayerGameId(client: RedisClient, userId: UserId, gameId: GameId): Future[Boolean] = {
      client
        .srem(Keys.playerGames(userId), gameId.value)
        .map(_ > 0)
    }

    def removeGame(client: RedisClient, id: GameId): Future[Boolean] = {
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
