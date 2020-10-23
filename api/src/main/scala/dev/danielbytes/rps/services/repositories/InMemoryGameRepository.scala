package dev.danielbytes.rps.services.repositories

import dev.danielbytes.rps.model.{ Game, GameId, UserId }

import scala.concurrent.{ ExecutionContext, Future }

/**
 * In-memory test GameRepository
 */
class InMemoryGameRepository()(implicit ec: ExecutionContext)
  extends InMemoryRepository[GameId, Game]
  with GameRepository {

  override def listPlayerGames(userId: UserId): Future[List[Game]] =
    query().map(
      _.filter {
        case (_, game) => game.playerList.map(_.user.id).contains(userId)
      }.map {
        case (_, game) => game
      }.toList)

  override def get(id: GameId): Future[Option[Game]] =
    super.get(id)

  override def set(
    id: GameId,
    model: Game): Future[Unit] = super.set(id, model)

  override def remove(id: GameId): Future[Unit] = super.remove(id)
}
