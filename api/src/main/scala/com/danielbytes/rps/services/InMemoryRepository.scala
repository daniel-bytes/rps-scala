package com.danielbytes.rps.services

import scala.collection.mutable
import scala.concurrent.{ ExecutionContext, Future }

class InMemoryRepository[TID <: AnyVal, TModel](
    initialData: (TID, TModel)*
)(
    implicit
    val ec: ExecutionContext
) extends Repository[TID, TModel] {
  private val data: mutable.Map[TID, TModel] = mutable.Map(initialData: _*)

  def get(id: TID): Future[Option[TModel]] = Future {
    data.get(id)
  }

  def set(id: TID, model: TModel): Future[Unit] = Future {
    data.put(id, model)
  }

  def remove(id: TID): Future[Unit] = Future {
    data.remove(id)
  }

  def clear(): Future[Unit] = Future {
    data.clear()
  }
}
