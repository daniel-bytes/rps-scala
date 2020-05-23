package com.danielbytes.rps.services.repositories

import com.danielbytes.rps.config.RedisConfig
import com.danielbytes.rps.model._
import com.redis.{ RedisClient, RedisClientPool }
import io.circe.generic.auto._
import io.circe.parser._
import io.circe.syntax._

import scala.concurrent.{ ExecutionContext, Future }

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

  override def listPlayerGames(userId: UserId): Future[List[Game]] = {
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

      model.userList.filter(_.isHuman).foreach(p =>
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

    private val ttl = 60 * 60 * 24 // 1 day

    def setGame(client: RedisClient, id: GameId, model: GameModel): Boolean = {
      if (client.set(Keys.game(id), model.asJson))
        client.expire(Keys.game(id), ttl)
      else
        false
    }

    def setPlayerGameId(client: RedisClient, userId: UserId, gameId: GameId): Option[Boolean] = {
      client
        .sadd(Keys.playerGames(userId), gameId.value)
        .map { result =>
          if (result > 0)
            client.expire(Keys.playerGames(userId), ttl)
          else
            false
        }
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
