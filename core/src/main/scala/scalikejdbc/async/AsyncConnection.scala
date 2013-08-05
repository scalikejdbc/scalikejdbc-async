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
 * Asynchronous DB connection
 */
trait AsyncConnection {

  /**
   * Returns non-shared connection.
   *
   * @param cxt execution context
   * @return connection
   */
  def toNonSharedConnection()(implicit cxt: EC = ECGlobal): Future[NonSharedAsyncConnection]

  /**
   * Send a query.
   *
   * @param statement statement
   * @param cxt execution context
   * @return future
   */
  def sendQuery(statement: String)(implicit cxt: EC = ECGlobal): Future[AsyncQueryResult]

  /**
   * Send a prepared statement.
   *
   * @param statement statement
   * @param parameters parameters
   * @param cxt execution context
   * @return future
   */
  def sendPreparedStatement(statement: String, parameters: Any*)(implicit cxt: EC = ECGlobal): Future[AsyncQueryResult]

  /**
   * Returns this connection is active.
   *
   * @return active
   */
  def isActive: Boolean = false

  /**
   * Returns this connection is shared.
   *
   * @return shared
   */
  def isShared: Boolean = true

  /**
   * Close or release this connection.
   */
  def close(): Unit

}

