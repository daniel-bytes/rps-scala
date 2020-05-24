package com.danielbytes.rps.services.repositories

import akka.actor.ActorSystem
import com.danielbytes.rps.model._
import redis.RedisClient

import scala.concurrent.{ ExecutionContext, Future }

/**
 * Production GameRepository using Redis as a backend.
 *
 * - Games are stored as string key/value pairs of GameId->Game JSON
 * - Player to Game mappings are stored as a Redis set of PlayerId->Set[GameId]
 * - All entries have a 1 day TTL
 */
class RedisGameRepository(
    val client: RedisClient
)(
    implicit
    val ec: ExecutionContext,
    system: ActorSystem
) extends GameRepository with RedisRepository {
  import SerializationModels._

  override def listPlayerGames(userId: UserId): Future[List[Game]] = {
    for {
      ids <- Queries.getPlayerGameIds(userId)
      games <- Queries.getGames(ids.toSeq)
      result = games.flatMap(mapGame).toList
    } yield result
  }

  override def get(id: GameId): Future[Option[Game]] = {
    Queries
      .getGame(id)
      .map(mapGame)
  }

  override def set(
    id: GameId,
    model: Game
  ): Future[Unit] = {
    for {
      _ <- Commands.setGame(id, GameModel(model))
      humans = model.userList.filter(_.isHuman).map(_.id)
      _ <- Commands.setPlayersGameId(humans, id)
    } yield ()
  }

  override def remove(id: GameId): Future[Unit] = {
    for {
      maybeGame <- Queries.getGame(id)
      _ <- mapGame(maybeGame).map(removeGame).getOrElse(Future.successful(()))
    } yield ()
  }

  private def mapGame(maybeGame: Option[GameModel]): Option[Game] =
    maybeGame.flatMap(m => GameModel.toGame(m).toOption)

  private def removeGame(model: Game): Future[Unit] =
    for {
      _ <- Commands.removeGame(model.id)
      humans = model.userList.filter(_.isHuman).map(_.id)
      _ <- Commands.removePlayersGameId(humans, model.id)
    } yield ()
}
