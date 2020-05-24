package com.danielbytes.rps.services.repositories

import scala.collection.mutable
import scala.concurrent.{ ExecutionContext, Future }

/**
 * Generic read-only repository, providing an interface for simple key/value lookups
 * @tparam TID The type of the Id / key
 * @tparam TModel The type of the model
 */
trait ReadOnlyRepository[TID <: AnyVal, TModel] {
  /**
   * Get a model by key, or return None
   */
  def get(id: TID): Future[Option[TModel]]
}

/**
 * Generic repository, providing an interface for simple key/value storage
 * @tparam TID The type of the Id / key
 * @tparam TModel The type of the model
 */
trait Repository[TID <: AnyVal, TModel] extends ReadOnlyRepository[TID, TModel] {
  /**
   * Store a model in the repository
   */
  def set(id: TID, model: TModel): Future[Unit]

  /**
   * Remove a model from the repository
   */
  def remove(id: TID): Future[Unit]
}

/**
 * Simple in-memory generic implementation of a Repository, useful for testing
 * @param initialData The intial data to populate the repository
 * @param ec The execution context
 * @tparam TID The type of the Id / key
 * @tparam TModel The type of the model
 */
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