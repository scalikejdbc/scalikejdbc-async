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
import com.github.mauricio.async.db.mysql.MySQLConnection
import com.github.mauricio.async.db.pool.ConnectionPool

/**
 * Connection impl
 */
private[scalikejdbc] trait MauricioConnectionBaseImpl extends AsyncConnection {

  private[scalikejdbc] val underlying: Connection
  private[scalikejdbc] val defaultTimeout = 10.seconds

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
      new AsyncQueryResult(
        rowsAffected = Option(queryResult.rowsAffected),
        statusMessage = Option(queryResult.statusMessage),
        rows = queryResult.rows.map(rows => new internal.MauricioAsyncResultSet(rows))) {

        lazy val generatedKey = extractGeneratedKey(queryResult)
      }
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
      new AsyncQueryResult(
        rowsAffected = Option(queryResult.rowsAffected),
        statusMessage = Option(queryResult.statusMessage),
        rows = queryResult.rows.map(rows => new internal.MauricioAsyncResultSet(rows))) {

        lazy val generatedKey = extractGeneratedKey(queryResult)
      }
    }
  }

  protected def extractGeneratedKey(queryResult: QueryResult)(
    implicit cxt: ExecutionContext = ExecutionContext.Implicits.global): Option[Long] = {
    if (!this.isInstanceOf[NonSharedAsyncConnection]) {
      throw new IllegalStateException("This async connection must be a non-shared connenction.")
    }
    this match {
      case conn: AsyncMySQLConnection =>
        Await.result(underlying.sendQuery("SELECT LAST_INSERT_ID()").map { result =>
          result.rows.headOption.flatMap { rows =>
            rows.headOption.map { row => row(0).asInstanceOf[Long] }
          }
        }, 10.seconds)
      case _ =>
        queryResult.rows.headOption.flatMap { rows =>
          rows.headOption.flatMap(row => Option(row(0)).flatMap { value =>
            try Some(value.toString.toLong)
            catch { case e: Exception => None }
          })
        }
    }
  }

  /**
   * Close or release this connection.
   */
  override def close(): Unit = underlying.disconnect

}
