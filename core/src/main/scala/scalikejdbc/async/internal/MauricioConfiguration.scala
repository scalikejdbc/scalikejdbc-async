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

import com.github.mauricio.async.db.Configuration
import scalikejdbc.JDBCUrl
import scalikejdbc.async.AsyncConnectionSettings

/**
 * Configuration attribute
 */
private[scalikejdbc] trait MauricioConfiguration {

  val defaultConfiguration = Configuration("")

  private[scalikejdbc] def configuration(
    url: String,
    user: String,
    password: String,
    connectionSettings: AsyncConnectionSettings) = {
    val jdbcUrl = JDBCUrl(url)
    Configuration(
      username = user,
      host = jdbcUrl.host,
      port = jdbcUrl.port,
      password = Option(password).filterNot(_.trim.isEmpty),
      database = Option(jdbcUrl.database).filterNot(_.trim.isEmpty),
      ssl = connectionSettings.ssl.getOrElse(defaultConfiguration.ssl),
      charset = connectionSettings.charset.getOrElse(defaultConfiguration.charset),
      maximumMessageSize = connectionSettings.maximumMessageSize.getOrElse(defaultConfiguration.maximumMessageSize),
      allocator = connectionSettings.allocator.getOrElse(defaultConfiguration.allocator),
      connectTimeout = connectionSettings.connectTimeout.getOrElse(defaultConfiguration.connectTimeout),
      testTimeout = connectionSettings.testTimeout.getOrElse(defaultConfiguration.testTimeout),
      queryTimeout = connectionSettings.queryTimeout)
  }

}
