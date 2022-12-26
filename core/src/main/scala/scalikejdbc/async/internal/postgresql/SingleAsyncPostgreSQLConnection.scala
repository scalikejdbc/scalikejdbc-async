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

import com.github.jasync.sql.db.Configuration
import com.github.jasync.sql.db.postgresql.util.URLParser
import scalikejdbc.async._, internal._

import java.nio.charset.StandardCharsets

/**
 * PostgreSQL Single Connection
 */
private[scalikejdbc] case class SingleAsyncPostgreSQLConnection(
  url: String,
  user: String,
  private val password: String,
  connectionSettings: AsyncConnectionSettings
) extends AsyncConnectionCommonImpl
  with PostgreSQLConnectionImpl
  with JasyncConfiguration {

  override protected def parseUrl(url: String): Configuration =
    URLParser.INSTANCE.parse(url, StandardCharsets.UTF_8)

  private[scalikejdbc] val underlying = {
    new com.github.jasync.sql.db.postgresql.PostgreSQLConnection(
      configuration(url, user, password, connectionSettings)
    )
  }

}
