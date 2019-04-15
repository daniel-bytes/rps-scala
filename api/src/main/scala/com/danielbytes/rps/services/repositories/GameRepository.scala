package com.danielbytes.rps.services.repositories

import com.danielbytes.rps.api.Encoders
import com.danielbytes.rps.config.RedisConfig
import com.danielbytes.rps.model.{ Game, GameId, UserId }
import com.redis.{ RedisClient, RedisClientPool }
import io.circe.generic.auto._
import io.circe.parser._
import io.circe.syntax._

import scala.concurrent.{ ExecutionContext, Future }

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
) extends GameRepository with Encoders {

  private val redis = new RedisClientPool(config.host, config.port)

  override def playerGames(userId: UserId): Future[List[Game]] = {
    redis.withClient { client =>
      Future {
        Queries
          .getPlayerGameIds(client, userId)
          .flatMap { id =>
            Queries.
              client
              .get(Keys.game(GameId(id)))
              .flatMap(json => decode[Game](json).toOption)
          }
          .toList
      }
    }
  }

  override def get(id: GameId): Future[Option[Game]] = {
    redis.withClient { client =>
      Future {
        client
          .get(Keys.game(id))
          .flatMap(json => decode[Game](json).toOption)
      }
    }
  }

  override def set(
    id: GameId,
    model: Game
  ): Future[Unit] = {
    redis.withClient { client =>
      Future {
        client.set(Keys.game(id), model.asJson)

        // Store all game IDs for non-AI users
        model.playerList.filterNot(_.isAI).foreach(p =>
          client.sadd(Keys.playerGames(p.id), id.value))
      }
    }
  }

  override def remove(id: GameId): Future[Unit] = {
    redis.withClient { client =>
      Future {
        client
          .get(Keys.game(id))
          .flatMap(json => decode[Game](json).toOption)
          .foreach { model =>
            client.del(Keys.game(id))

            model.playerList.filterNot(_.isAI).foreach(p =>
              client.srem(Keys.playerGames(p.id), id.value))
          }
      }
    }
  }

  private object Queries {
    def getGame(client: RedisClient, id: GameId): Option[Game] = {
      client
        .get(Keys.game(id))
        .flatMap(json => decode[Game](json).toOption)
    }

    def setGame(client: RedisClient, id: GameId, model: Game): Boolean = {
      client.set(Keys.game(id), model.asJson)
    }

    def getPlayerGameIds(client: RedisClient, id: UserId): Set[String] = {
      client
        .smembers(Keys.playerGames(id))
        .getOrElse(Set())
        .flatten
    }

    def setPlayerGameId(client: RedisClient, userId: UserId, gameId: GameId): Option[Long] = {
      client
        .sadd(Keys.playerGames(userId), gameId.value)
    }

    def removePlayerGameId(client: RedisClient, userId: UserId, gameId: GameId): Option[Long] = {
      client
        .srem(Keys.playerGames(userId), gameId.value)
    }
  }

  private object Keys {
    def game(id: GameId): String = s"game:${id.value}"
    def playerGames(id: UserId): String = s"player-games:${id.value}"
  }
}