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
package scalikejdbc.async

import scalikejdbc._

/**
 * Asynchronous Connection Pool Factory
 */
trait AsyncConnectionPoolFactory {

  def apply(url: String, user: String, password: String, settings: ConnectionPoolSettings = ConnectionPoolSettings()): AsyncConnectionPool

}

/**
 * Asynchronous Connection Pool Factory
 */
object AsyncConnectionPoolFactory extends AsyncConnectionPoolFactory {

  override def apply(url: String, user: String, password: String, settings: ConnectionPoolSettings = ConnectionPoolSettings()): AsyncConnectionPool = {
    // TODO heroku
    if (url.startsWith("jdbc:postgresql://")) {
      new internal.AsyncPostgreSQLConnectionPool(url, user, password, settings)
    } else if (url.startsWith("jdbc:mysql://")) {
      new internal.AsyncMySQLConnectionPool(url, user, password, settings)
    } else throw new UnsupportedOperationException("This RDBMS is not supported yet.")
  }

}

