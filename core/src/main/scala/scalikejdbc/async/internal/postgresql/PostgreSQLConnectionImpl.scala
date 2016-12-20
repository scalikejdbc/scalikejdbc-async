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
package scalikejdbc.async.internal.postgresql

import com.github.mauricio.async.db._
import scala.concurrent._
import scala.util.Try
import scalikejdbc.async._, ShortenedNames._, internal._

/**
 * PostgreSQL Connection Implementation
 */
trait PostgreSQLConnectionImpl extends AsyncConnectionCommonImpl {

  override def toNonSharedConnection()(implicit cxt: EC = ECGlobal): Future[NonSharedAsyncConnection] = {

    if (this.isInstanceOf[PoolableAsyncConnection[_]]) {
      val pool = this.asInstanceOf[PoolableAsyncConnection[Connection]].pool
      pool.take.map(conn => new NonSharedAsyncConnectionImpl(conn, Some(pool)) with PostgreSQLConnectionImpl)
    } else {
      Future.successful(new NonSharedAsyncConnectionImpl(underlying) with PostgreSQLConnectionImpl)
    }
  }

  protected def extractGeneratedKey(queryResult: QueryResult)(implicit cxt: EC = ECGlobal): Future[Option[Long]] = {
    ensureNonShared()
    Future.successful(for {
      rows <- queryResult.rows.headOption
      row <- rows.headOption
      value <- Option(row(0))
      key <- Try(value.toString.toLong).toOption
    } yield key)
  }

}
