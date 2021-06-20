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

import scalikejdbc._, async._, internal._
import com.github.jasync.sql.db.Configuration
import com.github.jasync.sql.db.postgresql.PostgreSQLConnection
import com.github.jasync.sql.db.postgresql.pool.PostgreSQLConnectionFactory

/**
 * PostgreSQL Connection Pool
 *
 * @param url jdbc url
 * @param user username
 * @param password password
 * @param settings extra settings
 */
private[scalikejdbc] class PostgreSQLConnectionPoolImpl(
  url: String,
  user: String,
  password: String,
  override val settings: AsyncConnectionPoolSettings =
    AsyncConnectionPoolSettings()
) extends AsyncConnectionPoolCommonImpl[PostgreSQLConnection](
    url,
    user,
    password,
    (c: Configuration) => new PostgreSQLConnectionFactory(c),
    settings
  ) {

  override def borrow(): AsyncConnection = new PoolableAsyncConnection(pool)
    with PostgreSQLConnectionImpl

}
