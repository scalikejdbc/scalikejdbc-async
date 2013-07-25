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
import org.joda.time.{ LocalDateTime, DateTime }

/**
 * WrappedResultSet for AsyncDBSession
 */
trait AsyncResultSet { self: WrappedResultSet =>

  def next(): Boolean

  def tail(): WrappedResultSet

  override def ensureCursor(): Unit = {}

  override def array(columnIndex: Int): java.sql.Array = any(columnIndex).asInstanceOf[java.sql.Array]
  override def array(columnLabel: String): java.sql.Array = any(columnLabel).asInstanceOf[java.sql.Array]

  override def asciiStream(columnIndex: Int): java.io.InputStream = any(columnIndex).asInstanceOf[java.io.InputStream]
  override def asciiStream(columnLabel: String): java.io.InputStream = any(columnLabel).asInstanceOf[java.io.InputStream]

  override def bigDecimal(columnIndex: Int): java.math.BigDecimal = any(columnIndex).asInstanceOf[java.math.BigDecimal]
  override def bigDecimal(columnLabel: String): java.math.BigDecimal = any(columnLabel).asInstanceOf[java.math.BigDecimal]

  override def binaryStream(columnIndex: Int): java.io.InputStream = any(columnIndex).asInstanceOf[java.io.InputStream]
  override def binaryStream(columnLabel: String): java.io.InputStream = any(columnLabel).asInstanceOf[java.io.InputStream]

  override def blob(columnIndex: Int): java.sql.Blob = any(columnIndex).asInstanceOf[java.sql.Blob]
  override def blob(columnLabel: String): java.sql.Blob = any(columnLabel).asInstanceOf[java.sql.Blob]

  override def bytes(columnIndex: Int): Array[Byte] = any(columnIndex).asInstanceOf[Array[Byte]]
  override def bytes(columnLabel: String): Array[Byte] = any(columnLabel).asInstanceOf[Array[Byte]]

  override def characterStream(columnIndex: Int): java.io.Reader = any(columnIndex).asInstanceOf[java.io.Reader]
  override def characterStream(columnLabel: String): java.io.Reader = any(columnLabel).asInstanceOf[java.io.Reader]

  override def clob(columnIndex: Int): java.sql.Clob = any(columnIndex).asInstanceOf[java.sql.Clob]
  override def clob(columnLabel: String): java.sql.Clob = any(columnLabel).asInstanceOf[java.sql.Clob]

  override def concurrency: Int = throw new UnsupportedOperationException
  override def cursorName: String = throw new UnsupportedOperationException

  override def date(columnIndex: Int): java.sql.Date = any(columnIndex).asInstanceOf[java.sql.Date]
  override def date(columnLabel: String): java.sql.Date = any(columnLabel).asInstanceOf[java.sql.Date]
  override def date(columnIndex: Int, cal: Calendar): java.sql.Date = throw new UnsupportedOperationException
  override def date(columnLabel: String, cal: Calendar): java.sql.Date = throw new UnsupportedOperationException

  override def fetchDirection: Int = throw new UnsupportedOperationException
  override def fetchSize: Int = throw new UnsupportedOperationException
  override def holdability: Int = throw new UnsupportedOperationException
  override def metaData: java.sql.ResultSetMetaData = throw new UnsupportedOperationException

  override def nCharacterStream(columnIndex: Int): java.io.Reader = any(columnIndex).asInstanceOf[java.io.Reader]
  override def nCharacterStream(columnLabel: String): java.io.Reader = any(columnLabel).asInstanceOf[java.io.Reader]

  override def nClob(columnIndex: Int): java.sql.NClob = any(columnIndex).asInstanceOf[java.sql.NClob]
  override def nClob(columnLabel: String): java.sql.NClob = any(columnLabel).asInstanceOf[java.sql.NClob]

  override def nString(columnIndex: Int): String = any(columnIndex).asInstanceOf[String]
  override def nString(columnLabel: String): String = any(columnLabel).asInstanceOf[String]

  override def any(columnIndex: Int): Any = throw new IllegalStateException("This method should be implementated")
  override def any(columnLabel: String): Any = throw new IllegalStateException("This method should be implementated")
  override def any(columnIndex: Int, map: Map[String, Class[_]]): Any = throw new UnsupportedOperationException
  override def any(columnLabel: String, map: Map[String, Class[_]]): Any = throw new UnsupportedOperationException

  override def ref(columnIndex: Int): java.sql.Ref = any(columnIndex).asInstanceOf[java.sql.Ref]
  override def ref(columnLabel: String): java.sql.Ref = any(columnLabel).asInstanceOf[java.sql.Ref]

  override def row: Int = throw new UnsupportedOperationException
  override def rowId(columnIndex: Int): java.sql.RowId = throw new UnsupportedOperationException
  override def rowId(columnLabel: String): java.sql.RowId = throw new UnsupportedOperationException

  override def sqlXml(columnIndex: Int): java.sql.SQLXML = any(columnIndex).asInstanceOf[java.sql.SQLXML]
  override def sqlXml(columnLabel: String): java.sql.SQLXML = any(columnLabel).asInstanceOf[java.sql.SQLXML]

  override def statement: java.sql.Statement = throw new UnsupportedOperationException

  override def string(columnIndex: Int): String = any(columnIndex).asInstanceOf[String]
  override def string(columnLabel: String): String = any(columnLabel).asInstanceOf[String]

  override def time(columnIndex: Int): java.sql.Time = any(columnIndex).asInstanceOf[java.sql.Time]
  override def time(columnLabel: String): java.sql.Time = any(columnLabel).asInstanceOf[java.sql.Time]
  override def time(columnIndex: Int, cal: Calendar): java.sql.Time = throw new UnsupportedOperationException
  override def time(columnLabel: String, cal: Calendar): java.sql.Time = throw new UnsupportedOperationException

  override def timestamp(columnIndex: Int): java.sql.Timestamp = any(columnIndex) match {
    case null => null
    case t: java.sql.Timestamp => t
    case dt: LocalDateTime => new java.sql.Timestamp(dt.toDateTime.getMillis)
    case dt: DateTime => new java.sql.Timestamp(dt.getMillis)
    case other => throw new UnsupportedOperationException
  }

  override def timestamp(columnLabel: String): java.sql.Timestamp = any(columnLabel) match {
    case null => null
    case t: java.sql.Timestamp => t
    case dt: LocalDateTime => new java.sql.Timestamp(dt.toDateTime.getMillis)
    case dt: DateTime => new java.sql.Timestamp(dt.getMillis)
    case other => throw new UnsupportedOperationException
  }

  override def timestamp(columnIndex: Int, cal: Calendar): java.sql.Timestamp = throw new UnsupportedOperationException
  override def timestamp(columnLabel: String, cal: Calendar): java.sql.Timestamp = throw new UnsupportedOperationException

  override def url(columnIndex: Int): java.net.URL = any(columnIndex).asInstanceOf[java.net.URL]
  override def url(columnLabel: String): java.net.URL = any(columnLabel).asInstanceOf[java.net.URL]

  override def warnings: java.sql.SQLWarning = throw new UnsupportedOperationException

  override def toMap(): Map[String, Any] = throw new UnsupportedOperationException
  override def toSymbolMap(): Map[Symbol, Any] = throw new UnsupportedOperationException

}

