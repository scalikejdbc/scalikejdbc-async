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
package scalikejdbc.async.internal.mysql

import com.github.jasync.sql.db._
import scala.concurrent._
import scalikejdbc.async._, ShortenedNames._, internal._
import scala.compat.java8.FutureConverters._
import scala.collection.JavaConverters._

/**
 * MySQL Connection Implementation
 */
trait MySQLConnectionImpl extends AsyncConnectionCommonImpl {

  override def toNonSharedConnection()(implicit cxt: EC = ECGlobal): Future[NonSharedAsyncConnection] = {
    this match {
      case c: PoolableAsyncConnection[ConcreteConnection @unchecked] =>
        val pool = c.pool
        pool.take.toScala.map(conn => new NonSharedAsyncConnectionImpl(conn, Some(pool)) with MySQLConnectionImpl)
      case _ =>
        Future.successful(new NonSharedAsyncConnectionImpl(underlying) with MySQLConnectionImpl)
    }
  }

  override protected def extractGeneratedKey(queryResult: QueryResult)(implicit cxt: EC = ECGlobal): Future[Option[Long]] = {
    ensureNonShared()
    underlying.sendQuery("SELECT LAST_INSERT_ID()").toScala.map { result =>
      result.getRows.asScala.headOption.map { row => row.get(0).asInstanceOf[Long] }
    }
  }

}
