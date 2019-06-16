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

import com.github.jasync.sql.db.Configuration
import scalikejdbc.JDBCUrl
import scalikejdbc.async.AsyncConnectionSettings

/**
 * Configuration attribute
 */
private[scalikejdbc] trait JasyncConfiguration {

  val defaultConfiguration = new Configuration("")

  private[scalikejdbc] def configuration(
    url: String,
    user: String,
    password: String,
    connectionSettings: AsyncConnectionSettings) = {
    val jdbcUrl = JDBCUrl(url)
    new Configuration(
      user,
      jdbcUrl.host,
      jdbcUrl.port,
      password,
      jdbcUrl.database,
      connectionSettings.ssl.getOrElse(defaultConfiguration.getSsl),
      connectionSettings.charset.getOrElse(defaultConfiguration.getCharset),
      connectionSettings.maximumMessageSize.getOrElse(defaultConfiguration.getMaximumMessageSize),
      connectionSettings.allocator.getOrElse(defaultConfiguration.getAllocator),
      connectionSettings.connectTimeout.map(_.toMillis.toInt).getOrElse(defaultConfiguration.getConnectionTimeout),
      connectionSettings.queryTimeout.map(x => java.time.Duration.ofMillis(x.toMillis)).getOrElse(defaultConfiguration.getQueryTimeout),
      defaultConfiguration.getApplicationName,
      defaultConfiguration.getInterceptors,
      defaultConfiguration.getEventLoopGroup,
      defaultConfiguration.getExecutionContext)
  }

}
