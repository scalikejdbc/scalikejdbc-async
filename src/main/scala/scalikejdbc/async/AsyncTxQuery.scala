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
import scala.util._

/**
 * Asynchronous Transactional Query
 */
class AsyncTxQuery(sqlObjects: Seq[SQL[_, _]]) {

  def future()(
    implicit session: AsyncDBSession,
    cxt: ExecutionContext = ExecutionContext.Implicits.global): Future[Seq[AsyncQueryResult]] = {

    session.connection.toNonSharedConnection.map(conn => AsyncTxDBSession(conn)).flatMap { txSession: AsyncTxDBSession =>
      txSession.begin().flatMap { _ =>
        sqlObjects.foldLeft(Future.successful(Vector.empty[AsyncQueryResult])) { (f, sql) =>
          for {
            results <- f
            current <- txSession.connection.sendPreparedStatement(sql.statement, sql.parameters: _*)
          } yield results :+ current
        }.andThen {
          case Success(_) => txSession.commit()
          case Failure(e) => txSession.rollback()
        }.andThen {
          case _ => txSession.release()
        }
      }
    }
  }

}
