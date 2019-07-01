/*
 * Copyright 2013 Kazuhiro Sera, Manabu Nakamura
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

import com.github.jasync.sql.db.Connection
import scalikejdbc._
import scalikejdbc.async.ShortenedNames._
import scalikejdbc.async.internal.AsyncConnectionCommonImpl

import scala.concurrent.{ Promise, Future }
import scala.util.{ Failure, Success }
import scala.compat.java8.FutureConverters._

/**
 * Asynchronous Transaction Provider
 */
object AsyncTx {

  /**
   * Provides [[scalikejdbc.async.AsyncTxQuery]] from [[scalikejdbc.SQLInterpolation.SQLBuilder]] objects.
   *
   * @param builders sql builders
   * @param session asynchronous db session
   * @param cxt execution context
   * @return async tx query
   */
  def withSQLBuilders(builders: SQLBuilder[_]*)(implicit session: SharedAsyncDBSession, cxt: EC = ECGlobal): AsyncTxQuery = {
    withSQLs(builders.map(_.toSQL): _*)
  }

  /**
   * Provides [[scalikejdbc.async.AsyncTxQuery]] from [[scalikejdbc.SQL]] objects.
   *
   * @param sqlObjects sql objects
   * @param session asynchronous db session
   * @param cxt execution context
   * @return async tx query
   */
  def withSQLs(sqlObjects: SQL[_, _]*)(implicit session: SharedAsyncDBSession, cxt: EC = ECGlobal): AsyncTxQuery = {
    new AsyncTxQuery(sqlObjects)
  }

  def inTransaction[A](tx: TxAsyncDBSession, op: TxAsyncDBSession => Future[A])(implicit cxt: EC = ECGlobal): Future[A] = {
    val p = Promise[A]()
    val connection = tx.connection.asInstanceOf[AsyncConnectionCommonImpl].underlying
    connection
      .inTransaction((_: Connection) => op.apply(tx).toJava.toCompletableFuture)
      .toScala
      .onComplete {
        case Success(result) =>
          tx.release()
          p.success(result)
        case Failure(e) =>
          tx.release()
          p.failure(e)
      }
    p.future
  }

}

