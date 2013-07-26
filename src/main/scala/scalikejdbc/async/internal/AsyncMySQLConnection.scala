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

import com.github.mauricio.async.db.Connection
import scala.concurrent._
import scalikejdbc.async.{ AsyncConnection, NonSharedAsyncConnection }

trait AsyncMySQLConnection extends AsyncConnection {

  private[scalikejdbc] val underlying: Connection

  override def toNonSharedConnection()(
    implicit cxt: ExecutionContext = ExecutionContext.Implicits.global): Future[NonSharedAsyncConnection] = {

    if (this.isInstanceOf[MauricioPoolableAsyncConnection[_]]) {
      val pool = this.asInstanceOf[MauricioPoolableAsyncConnection[Connection]].pool
      pool.take.map(conn => new MauricioNonSharedAsyncConnection(conn, Some(pool)) with AsyncMySQLConnection)
    } else {
      future(new MauricioSharedAsyncConnection(this.underlying) with AsyncMySQLConnection)
    }
  }

}
