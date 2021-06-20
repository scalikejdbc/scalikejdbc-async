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

import scalikejdbc.JDBCUrl._
import scalikejdbc.async.internal.mysql.MySQLConnectionPoolImpl
import scalikejdbc.async.internal.postgresql.PostgreSQLConnectionPoolImpl

/**
 * Asynchronous Connection Pool Factory
 */
trait AsyncConnectionPoolFactory {

  def apply(
    url: String,
    user: String,
    password: String,
    settings: AsyncConnectionPoolSettings = AsyncConnectionPoolSettings()
  ): AsyncConnectionPool

}

/**
 * Asynchronous Connection Pool Factory
 */
object AsyncConnectionPoolFactory extends AsyncConnectionPoolFactory {

  override def apply(
    url: String,
    user: String,
    password: String,
    settings: AsyncConnectionPoolSettings = AsyncConnectionPoolSettings()
  ): AsyncConnectionPool = {

    url match {
      case _ if url.startsWith("jdbc:postgresql://") =>
        new PostgreSQLConnectionPoolImpl(url, user, password, settings)

      case _ if url.startsWith("jdbc:mysql://") =>
        new MySQLConnectionPoolImpl(url, user, password, settings)

      case HerokuPostgresRegexp(_user, _password, _host, _dbname) =>
        // Heroku PostgreSQL
        val _url = "jdbc:postgresql://%s/%s".format(_host, _dbname)
        new PostgreSQLConnectionPoolImpl(_url, _user, _password, settings)

      case HerokuMySQLRegexp(_user, _password, _host, _dbname) =>
        // Heroku MySQL

        // issue #5 Error: database name is too long
        //val defaultProperties = """?useUnicode=yes&characterEncoding=UTF-8&connectionCollation=utf8_general_ci"""
        val defaultProperties = ""
        val addDefaultPropertiesIfNeeded = MysqlCustomProperties
          .findFirstMatchIn(url)
          .map(_ => "")
          .getOrElse(defaultProperties)
        val _url = "jdbc:mysql://%s/%s".format(
          _host,
          _dbname + addDefaultPropertiesIfNeeded
        )
        new MySQLConnectionPoolImpl(_url, _user, _password, settings)

      case _ =>
        throw new UnsupportedOperationException(
          "This RDBMS is not supported yet."
        )
    }
  }

}
