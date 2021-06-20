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
package scalikejdbc.async

import scalikejdbc._

/**
 * Asynchronous Connection Pool
 */
abstract class AsyncConnectionPool(
  val settings: AsyncConnectionPoolSettings = AsyncConnectionPoolSettings()
) {

  /**
   * Borrows a connection from pool.
   * @return connection
   */
  def borrow(): AsyncConnection

  /**
   * Close this pool.
   */
  def close(): Unit

  /**
   * Gives back the connection.
   * @param conn connection
   */
  def giveBack(conn: NonSharedAsyncConnection): Unit

}

/**
 * Asynchronous Connection Pool Companion Object
 */
object AsyncConnectionPool extends LogSupport {

  type ConcurrentMap[A, B] = scala.collection.concurrent.TrieMap[A, B]
  type CPSettings = AsyncConnectionPoolSettings
  type CPFactory = AsyncConnectionPoolFactory

  val DEFAULT_NAME: Symbol = Symbol("default")

  private[this] val pools = new ConcurrentMap[Any, AsyncConnectionPool]()

  def isInitialized(name: Any = DEFAULT_NAME): Boolean = pools.contains(name)

  def get(name: Any = DEFAULT_NAME): AsyncConnectionPool = {
    pools.getOrElse(
      name, {
        val message =
          ErrorMessage.CONNECTION_POOL_IS_NOT_YET_INITIALIZED + "(name:" + name + ")"
        throw new IllegalStateException(message)
      }
    )
  }

  def apply(name: Any = DEFAULT_NAME): AsyncConnectionPool = get(name)

  def add(
    name: Any,
    url: String,
    user: String,
    password: String,
    settings: CPSettings = AsyncConnectionPoolSettings()
  )(implicit factory: CPFactory = AsyncConnectionPoolFactory): Unit = {
    val newPool: AsyncConnectionPool =
      factory.apply(url, user, password, settings)
    log.debug(
      s"Registered connection pool (url: ${url}, user: ${user}, settings: ${settings}"
    )
    val replaced = pools.put(name, newPool)
    replaced.foreach(_.close())
  }

  def singleton(
    url: String,
    user: String,
    password: String,
    settings: CPSettings = AsyncConnectionPoolSettings()
  )(implicit factory: CPFactory = AsyncConnectionPoolFactory): Unit = {
    add(DEFAULT_NAME, url, user, password, settings)(factory)
  }

  def borrow(name: Any = DEFAULT_NAME): AsyncConnection = {
    val pool = get(name)
    log.debug(s"Borrowed a new connection from pool $name")
    pool.borrow()
  }

  def giveBack(
    connection: NonSharedAsyncConnection,
    name: Any = DEFAULT_NAME
  ): Unit = {
    val pool = get(name)
    log.debug(s"Gave back previously borrowed connection from pool $name")
    pool.giveBack(connection)
  }

  def close(name: Any = DEFAULT_NAME): Unit =
    pools.remove(name).foreach(_.close())

  def closeAll(): Unit = pools.keys.foreach(name => close(name))

}
