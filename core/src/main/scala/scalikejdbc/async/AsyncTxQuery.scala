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

import scalikejdbc._
import scala.concurrent._
import scalikejdbc.async.ShortenedNames._

/**
 * Asynchronous Transactional Query
 */
class AsyncTxQuery(sqls: Seq[SQL[_, _]]) {

  def future()(implicit session: SharedAsyncDBSession, cxt: EC = ECGlobal): Future[Seq[AsyncQueryResult]] = {
    def op(tx: TxAsyncDBSession) = {
      sqls.foldLeft(Future.successful(Vector.empty[AsyncQueryResult])) { (resultsFuture, sql) =>
        for {
          results <- resultsFuture
          current <- tx.connection.sendPreparedStatement(sql.statement, sql.parameters.toSeq: _*)
        } yield results :+ current
      }
    }
    session.connection.toNonSharedConnection
      .flatMap(conn => AsyncTx.inTransaction(TxAsyncDBSession(conn), op))
  }

}
