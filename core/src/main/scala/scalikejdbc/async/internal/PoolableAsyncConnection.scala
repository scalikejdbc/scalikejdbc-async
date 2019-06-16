/*
 * Copyright 2013 Kazuhiro Sera
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package scalikejdbc.async.internal

import java.util.concurrent.TimeUnit

import com.github.jasync.sql.db.pool.ConnectionPool
import scalikejdbc.async.{ AsyncConnection, NonSharedAsyncConnection }
import com.github.jasync.sql.db.{ ConcreteConnection, Connection }

import scala.concurrent._
import scalikejdbc.async.ShortenedNames._

/**
 * AsyncConnection implementation which is based on jasync's Connection
 *
 * @param pool connection pool
 * @tparam T Connection sub type
 */
private[scalikejdbc] abstract class PoolableAsyncConnection[T <: ConcreteConnection](val pool: ConnectionPool[T])
  extends AsyncConnectionCommonImpl {

  override def toNonSharedConnection()(implicit cxt: EC = ECGlobal): Future[NonSharedAsyncConnection] =
    Future.failed(new UnsupportedOperationException)

  // TODO make configurable timeout or avoid Future#get
  private[scalikejdbc] lazy val underlying = pool.take().get(5, TimeUnit.SECONDS)

  /**
   * Close or release this connection.
   */
  override def close(): Unit = {
    pool.giveBack(underlying)
  }

}
