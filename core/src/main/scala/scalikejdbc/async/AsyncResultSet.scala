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
import org.joda.time.{ LocalDateTime, DateTime, LocalDate, LocalTime }

/**
 * WrappedResultSet for Asynchronous DB Session
 */
trait AsyncResultSet extends WrappedResultSet {

  def next(): Boolean

  def tail(): AsyncResultSet

  override final def ensureCursor(): Unit = {}

  override final def fetchDirection: Int = throw new UnsupportedOperationException
  override final def fetchSize: Int = throw new UnsupportedOperationException
  override final def holdability: Int = throw new UnsupportedOperationException
  override final def metaData: java.sql.ResultSetMetaData = throw new UnsupportedOperationException
  override final def row: Int = throw new UnsupportedOperationException
  override final def statement: java.sql.Statement = throw new UnsupportedOperationException
  override final def warnings: java.sql.SQLWarning = throw new UnsupportedOperationException

  override def toMap(): Map[String, Any] = throw new UnsupportedOperationException
  override def toSymbolMap(): Map[Symbol, Any] = throw new UnsupportedOperationException

}
