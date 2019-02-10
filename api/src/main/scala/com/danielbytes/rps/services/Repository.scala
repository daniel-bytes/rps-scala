package com.danielbytes.rps.services

import scala.concurrent.Future

trait Repository[TID <: AnyVal, TModel] {
  def get(id: TID): Future[Option[TModel]]
  def set(id: TID, model: TModel): Future[Unit]
  def remove(id: TID): Future[Unit]
}
