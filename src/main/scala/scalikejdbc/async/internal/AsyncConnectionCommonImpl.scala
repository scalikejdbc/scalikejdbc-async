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
import scala.concurrent._, duration.DurationInt
import com.github.mauricio.async.db._

/**
 * Basic Implementation of Asynchronous Connection
 */
private[scalikejdbc] trait AsyncConnectionCommonImpl extends AsyncConnection {

  private[scalikejdbc] val underlying: Connection
  private[scalikejdbc] val defaultTimeout = 10.seconds

  override def sendQuery(statement: String)(
    implicit cxt: ExecutionContext = ExecutionContext.Implicits.global): Future[AsyncQueryResult] = {

    underlying.sendQuery(statement).map { queryResult =>
      new AsyncQueryResult(
        rowsAffected = Option(queryResult.rowsAffected),
        statusMessage = Option(queryResult.statusMessage),
        rows = queryResult.rows.map(rows => new internal.AsyncResultSetImpl(rows))) {

        lazy val generatedKey = extractGeneratedKey(queryResult)
      }
    }
  }

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
        rows = queryResult.rows.map(rows => new internal.AsyncResultSetImpl(rows))) {

        lazy val generatedKey = extractGeneratedKey(queryResult)
      }
    }
  }

  override def close(): Unit = underlying.disconnect

  /**
   * Extracts generated key.
   *
   * @param queryResult query result
   * @param cxt  execution context
   * @return optional generated key
   */
  protected def extractGeneratedKey(queryResult: QueryResult)(
    implicit cxt: ExecutionContext = ExecutionContext.Implicits.global): Option[Long]

}
