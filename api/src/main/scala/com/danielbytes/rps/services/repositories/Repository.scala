package com.danielbytes.rps.services.repositories

import scala.collection.mutable
import scala.concurrent.{ ExecutionContext, Future }

trait Repository[TID <: AnyVal, TModel] {
  def get(id: TID): Future[Option[TModel]]
  def set(id: TID, model: TModel): Future[Unit]
  def remove(id: TID): Future[Unit]
}

class InMemoryRepository[TID <: AnyVal, TModel](
    initialData: (TID, TModel)*
)(
    implicit
    val ec: ExecutionContext
) extends Repository[TID, TModel] {
  private val data: mutable.Map[TID, TModel] = mutable.Map(initialData: _*)

  def get(id: TID): Future[Option[TModel]] = Future.successful {
    data.get(id)
  }

  def set(id: TID, model: TModel): Future[Unit] = Future.successful {
    data.put(id, model)
  }

  def remove(id: TID): Future[Unit] = Future.successful {
    data.remove(id)
  }

  def clear(): Future[Unit] = Future.successful {
    data.clear()
  }

  def query(): Future[Iterable[(TID, TModel)]] = Future.successful {
    this.data.toList
  }
}