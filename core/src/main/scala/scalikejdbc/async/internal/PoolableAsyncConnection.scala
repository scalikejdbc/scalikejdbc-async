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

import com.github.mauricio.async.db.pool.ConnectionPool
import scalikejdbc.async.{ NonSharedAsyncConnection, AsyncConnection }
import com.github.mauricio.async.db.Connection
import scala.concurrent._
import scalikejdbc.async.ShortenedNames._

/**
 * AsyncConnection implementation which is based on Mauricio's Connection
 *
 * @param pool connection pool
 * @tparam T Connection sub type
 */
private[scalikejdbc] abstract class PoolableAsyncConnection[T <: com.github.mauricio.async.db.Connection](val pool: ConnectionPool[T])
    extends AsyncConnectionCommonImpl
    with AsyncConnection {

  override def toNonSharedConnection()(implicit cxt: EC = ECGlobal): Future[NonSharedAsyncConnection] = {
    Future.failed(new UnsupportedOperationException)
  }

  private[scalikejdbc] val underlying: Connection = pool

  /**
   * Close or release this connection.
   */
  override def close(): Unit = {
    pool.asInstanceOf[ConnectionPool[Connection]].giveBack(underlying)
  }

}
