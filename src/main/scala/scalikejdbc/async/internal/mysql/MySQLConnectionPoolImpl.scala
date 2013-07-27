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
package scalikejdbc.async.internal.mysql

import scalikejdbc._, async._, internal._

import com.github.mauricio.async.db.pool.ConnectionPool
import com.github.mauricio.async.db.pool.PoolConfiguration
import com.github.mauricio.async.db.mysql.MySQLConnection
import com.github.mauricio.async.db.mysql.pool.MySQLConnectionFactory

/**
 * MySQL Connection Pool
 *
 * @param url jdbc url
 * @param user username
 * @param password password
 * @param settings extra settings
 */
private[scalikejdbc] class MySQLConnectionPoolImpl(
  override val url: String,
  override val user: String,
  password: String,
  override val settings: ConnectionPoolSettings = ConnectionPoolSettings())
    extends AsyncConnectionPool(url, user, password, settings)
    with LogSupport {

  private[this] val factory = new MySQLConnectionFactory(config)
  private[this] val pool = new ConnectionPool[MySQLConnection](factory, PoolConfiguration.Default)

  override def borrow(): AsyncConnection = new PoolableAsyncConnection(pool) with MySQLConnectionImpl

  override def close(): Unit = pool.disconnect

  override def giveBack(conn: NonSharedAsyncConnection): Unit = conn match {
    case conn: NonSharedAsyncConnectionImpl => pool.giveBack(conn.underlying.asInstanceOf[MySQLConnection])
    case _ => log.debug("You don't need to give back this connection to the pool.")
  }

}
