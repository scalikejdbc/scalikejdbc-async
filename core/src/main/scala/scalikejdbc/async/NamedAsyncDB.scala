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

import scala.concurrent._
import scalikejdbc.async.ShortenedNames._

/**
 * Basic Database Accessor with the name
 */
case class NamedAsyncDB(name: Any = AsyncConnectionPool.DEFAULT_NAME) {

  /**
   * Provides a code block which have a connection from ConnectionPool and passes it to the operation.
   *
   * @param op operation
   * @tparam A return type
   * @return a Future value
   */
  def withPool[A](op: SharedAsyncDBSession => Future[A]): Future[A] = {
    op.apply(sharedSession)
  }

  /**
   * Provides a shared session.
   *
   * @return shared session
   */
  def sharedSession: SharedAsyncDBSession = SharedAsyncDBSession(
    AsyncConnectionPool(name).borrow()
  )

  /**
   * Provides a future world within a transaction.
   *
   * @param op operation
   * @param cxt execution context
   * @tparam A return type
   * @return a future value
   */
  def localTx[A](
    op: TxAsyncDBSession => Future[A]
  )(implicit cxt: EC = ECGlobal): Future[A] = {
    AsyncConnectionPool(name)
      .borrow()
      .toNonSharedConnection()
      .flatMap(conn => AsyncTx.inTransaction[A](TxAsyncDBSession(conn), op))
  }

}
