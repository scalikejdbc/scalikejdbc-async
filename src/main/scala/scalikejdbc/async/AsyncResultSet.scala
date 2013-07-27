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

import java.util.Calendar
import org.joda.time.{ LocalTime, LocalDateTime, DateTime }
import scala.concurrent.duration.FiniteDuration

/**
 * WrappedResultSet for Asynchronous DB Session
 */
trait AsyncResultSet extends WrappedResultSet {

  def next(): Boolean

  def tail(): AsyncResultSet

  override def ensureCursor(): Unit = {}

  override def array(columnIndex: Int): java.sql.Array = throw new UnsupportedOperationException
  override def array(columnLabel: String): java.sql.Array = throw new UnsupportedOperationException

  override def asciiStream(columnIndex: Int): java.io.InputStream = throw new UnsupportedOperationException
  override def asciiStream(columnLabel: String): java.io.InputStream = throw new UnsupportedOperationException

  override def bigDecimal(columnIndex: Int): java.math.BigDecimal
  override def bigDecimal(columnLabel: String): java.math.BigDecimal

  override def binaryStream(columnIndex: Int): java.io.InputStream = throw new UnsupportedOperationException
  override def binaryStream(columnLabel: String): java.io.InputStream = throw new UnsupportedOperationException

  override def blob(columnIndex: Int): java.sql.Blob = throw new UnsupportedOperationException
  override def blob(columnLabel: String): java.sql.Blob = throw new UnsupportedOperationException

  override def bytes(columnIndex: Int): Array[Byte]
  override def bytes(columnLabel: String): Array[Byte]

  override def characterStream(columnIndex: Int): java.io.Reader = throw new UnsupportedOperationException
  override def characterStream(columnLabel: String): java.io.Reader = throw new UnsupportedOperationException

  override def clob(columnIndex: Int): java.sql.Clob = throw new UnsupportedOperationException
  override def clob(columnLabel: String): java.sql.Clob = throw new UnsupportedOperationException

  override def concurrency: Int = throw new UnsupportedOperationException
  override def cursorName: String = throw new UnsupportedOperationException

  override def date(columnIndex: Int): java.sql.Date
  override def date(columnLabel: String): java.sql.Date

  override def date(columnIndex: Int, cal: Calendar): java.sql.Date = throw new UnsupportedOperationException
  override def date(columnLabel: String, cal: Calendar): java.sql.Date = throw new UnsupportedOperationException

  override def fetchDirection: Int = throw new UnsupportedOperationException
  override def fetchSize: Int = throw new UnsupportedOperationException
  override def holdability: Int = throw new UnsupportedOperationException
  override def metaData: java.sql.ResultSetMetaData = throw new UnsupportedOperationException

  override def nCharacterStream(columnIndex: Int): java.io.Reader = throw new UnsupportedOperationException
  override def nCharacterStream(columnLabel: String): java.io.Reader = throw new UnsupportedOperationException

  override def nClob(columnIndex: Int): java.sql.NClob = throw new UnsupportedOperationException
  override def nClob(columnLabel: String): java.sql.NClob = throw new UnsupportedOperationException

  override def nString(columnIndex: Int): String = throw new UnsupportedOperationException
  override def nString(columnLabel: String): String = throw new UnsupportedOperationException

  override def any(columnIndex: Int): Any
  override def any(columnLabel: String): Any

  override def any(columnIndex: Int, map: Map[String, Class[_]]): Any = throw new UnsupportedOperationException
  override def any(columnLabel: String, map: Map[String, Class[_]]): Any = throw new UnsupportedOperationException

  override def ref(columnIndex: Int): java.sql.Ref = throw new UnsupportedOperationException
  override def ref(columnLabel: String): java.sql.Ref = throw new UnsupportedOperationException

  override def row: Int = throw new UnsupportedOperationException
  override def rowId(columnIndex: Int): java.sql.RowId = throw new UnsupportedOperationException
  override def rowId(columnLabel: String): java.sql.RowId = throw new UnsupportedOperationException

  override def sqlXml(columnIndex: Int): java.sql.SQLXML = throw new UnsupportedOperationException
  override def sqlXml(columnLabel: String): java.sql.SQLXML = throw new UnsupportedOperationException

  override def statement: java.sql.Statement = throw new UnsupportedOperationException

  override def string(columnIndex: Int): String
  override def string(columnLabel: String): String

  override def time(columnIndex: Int): java.sql.Time
  override def time(columnLabel: String): java.sql.Time

  override def time(columnIndex: Int, cal: Calendar): java.sql.Time = throw new UnsupportedOperationException
  override def time(columnLabel: String, cal: Calendar): java.sql.Time = throw new UnsupportedOperationException

  override def timestamp(columnIndex: Int): java.sql.Timestamp
  override def timestamp(columnLabel: String): java.sql.Timestamp

  override def timestamp(columnIndex: Int, cal: Calendar): java.sql.Timestamp = throw new UnsupportedOperationException
  override def timestamp(columnLabel: String, cal: Calendar): java.sql.Timestamp = throw new UnsupportedOperationException

  override def url(columnIndex: Int): java.net.URL
  override def url(columnLabel: String): java.net.URL

  override def warnings: java.sql.SQLWarning = throw new UnsupportedOperationException

  override def toMap(): Map[String, Any] = throw new UnsupportedOperationException
  override def toSymbolMap(): Map[Symbol, Any] = throw new UnsupportedOperationException

}

