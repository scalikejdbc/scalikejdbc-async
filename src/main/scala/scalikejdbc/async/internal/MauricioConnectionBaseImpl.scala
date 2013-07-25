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

import scalikejdbc.async._
import scala.concurrent._
import scala.concurrent.duration.DurationInt
import com.github.mauricio.async.db.{ QueryResult, Connection }

/**
 * Connection impl
 */
private[scalikejdbc] trait MauricioConnectionBaseImpl { self: AsyncConnection =>

  private[scalikejdbc] val underlying: Connection
  private[scalikejdbc] val defaultTimeout = 10.seconds

  /**
   * Returns a non-shared connection.
   *
   * @param cxt execution context
   * @return  non-shared connection
   */
  override def toNonSharedConnection()(
    implicit cxt: ExecutionContext = ExecutionContext.Implicits.global): Future[NonSharedAsyncConnection] = {

    if (this.isInstanceOf[MauricioPoolableAsyncConnection[_]]) {
      val pool = this.asInstanceOf[MauricioPoolableAsyncConnection[Connection]].pool
      pool.take.map(conn => MauricioNonSharedAsyncConnection(conn, Some(pool)))
    } else {
      future(MauricioSharedAsyncConnection(this.underlying))
    }
  }

  /**
   * Send a query.
   *
   * @param statement statement
   * @param cxt execution context
   * @return future
   */
  override def sendQuery(statement: String)(
    implicit cxt: ExecutionContext = ExecutionContext.Implicits.global): Future[AsyncQueryResult] = {

    underlying.sendQuery(statement).map { queryResult =>
      AsyncQueryResult(
        rowsAffected = Option(queryResult.rowsAffected),
        statusMessage = Option(queryResult.statusMessage),
        rows = queryResult.rows.map(rows => new internal.MauricioAsyncResultSet(rows))
      )
    }
  }

  /**
   * Send a prepared statement.
   *
   * @param statement statement
   * @param parameters parameters
   * @param cxt execution context
   * @return future
   */
  override def sendPreparedStatement(statement: String, parameters: Any*)(
    implicit cxt: ExecutionContext = ExecutionContext.Implicits.global): Future[AsyncQueryResult] = {

    val queryResultFuture: Future[QueryResult] = {
      if (parameters.isEmpty) underlying.sendQuery(statement)
      else underlying.sendPreparedStatement(statement, parameters)
    }
    queryResultFuture.map { queryResult =>
      AsyncQueryResult(
        rowsAffected = Option(queryResult.rowsAffected),
        statusMessage = Option(queryResult.statusMessage),
        rows = queryResult.rows.flatMap { rows =>
          if (rows.isEmpty) None
          else Some(new internal.MauricioAsyncResultSet(rows))
        }
      )
    }
  }

  /**
   * Close or release this connection.
   */
  override def close(): Unit = underlying.disconnect

}
