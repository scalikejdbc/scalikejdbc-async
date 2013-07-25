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
  def toNonSharedConnection()(
    implicit cxt: ExecutionContext = ExecutionContext.Implicits.global): Future[NonSharedAsyncConnection]

  /**
   * Send a query.
   *
   * @param statement statement
   * @param cxt execution context
   * @return future
   */
  def sendQuery(statement: String)(
    implicit cxt: ExecutionContext = ExecutionContext.Implicits.global): Future[AsyncQueryResult]

  /**
   * Send a prepared statement.
   *
   * @param statement statement
   * @param parameters parameters
   * @param cxt execution context
   * @return future
   */
  def sendPreparedStatement(statement: String, parameters: Any*)(
    implicit cxt: ExecutionContext = ExecutionContext.Implicits.global): Future[AsyncQueryResult]

  /**
   * Close or release this connection.
   */
  def close(): Unit

}

