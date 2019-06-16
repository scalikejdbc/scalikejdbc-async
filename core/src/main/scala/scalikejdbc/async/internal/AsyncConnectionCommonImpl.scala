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
import ShortenedNames._

import scala.concurrent._
import duration.DurationInt
import com.github.jasync.sql.db._

import scala.compat.java8.FutureConverters._
import scala.collection.JavaConverters._

/**
 * Basic Implementation of Asynchronous Connection
 */
private[scalikejdbc] trait AsyncConnectionCommonImpl extends AsyncConnection {

  private[scalikejdbc] def underlying: ConcreteConnection
  private[scalikejdbc] val defaultTimeout = 10.seconds

  override def isActive: Boolean = underlying.isConnected

  override def sendQuery(statement: String)(implicit cxt: EC = ECGlobal): Future[AsyncQueryResult] = {

    underlying.sendQuery(statement).toScala.map { queryResult =>
      new AsyncQueryResult(
        rowsAffected = Option(queryResult.getRowsAffected),
        statusMessage = Option(queryResult.getStatusMessage),
        rows = Some(new internal.AsyncResultSetImpl(queryResult.getRows.asScala.toIndexedSeq))) {

        lazy val generatedKey = extractGeneratedKey(queryResult)
      }
    }
  }

  override def sendPreparedStatement(statement: String, parameters: Any*)(implicit cxt: EC = ECGlobal): Future[AsyncQueryResult] = {

    val queryResultFuture: Future[QueryResult] = {
      if (parameters.isEmpty) underlying.sendQuery(statement)
      else underlying.sendPreparedStatement(statement, parameters.asJava)
    }.toScala
    queryResultFuture.map { queryResult =>
      new AsyncQueryResult(
        rowsAffected = Option(queryResult.getRowsAffected),
        statusMessage = Option(queryResult.getStatusMessage),
        rows = Some(new internal.AsyncResultSetImpl(queryResult.getRows.asScala.toIndexedSeq))) {

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
  protected def extractGeneratedKey(queryResult: QueryResult)(implicit cxt: EC = ECGlobal): Future[Option[Long]]

  protected def ensureNonShared(): Unit = {
    if (!this.isInstanceOf[NonSharedAsyncConnection]) {
      throw new IllegalStateException("This asynchronous connection must be a non-shared connection.")
    }
  }

}
