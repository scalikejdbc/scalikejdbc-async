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
 * WrappedResultSet for AsyncDBSession
 */
trait AsyncResultSet extends WrappedResultSet {

  def next(): Boolean

  def tail(): AsyncResultSet

  override def ensureCursor(): Unit = {}

  override def array(columnIndex: Int): java.sql.Array = throw new UnsupportedOperationException
  override def array(columnLabel: String): java.sql.Array = throw new UnsupportedOperationException

  override def asciiStream(columnIndex: Int): java.io.InputStream = throw new UnsupportedOperationException
  override def asciiStream(columnLabel: String): java.io.InputStream = throw new UnsupportedOperationException

  override def bigDecimal(columnIndex: Int): java.math.BigDecimal = any(columnIndex) match {
    case null => null
    case bd: java.math.BigDecimal => bd
    case bd: scala.BigDecimal => bd.underlying
    case str: String => new java.math.BigDecimal(str)
    case any => new java.math.BigDecimal(any.toString)
  }

  override def bigDecimal(columnLabel: String): java.math.BigDecimal = any(columnLabel) match {
    case null => null
    case bd: java.math.BigDecimal => bd
    case bd: scala.BigDecimal => bd.underlying
    case str: String => new java.math.BigDecimal(str)
    case any => new java.math.BigDecimal(any.toString)
  }

  override def binaryStream(columnIndex: Int): java.io.InputStream = throw new UnsupportedOperationException
  override def binaryStream(columnLabel: String): java.io.InputStream = throw new UnsupportedOperationException

  override def blob(columnIndex: Int): java.sql.Blob = throw new UnsupportedOperationException
  override def blob(columnLabel: String): java.sql.Blob = throw new UnsupportedOperationException

  override def bytes(columnIndex: Int): Array[Byte] = any(columnIndex) match {
    case null => null
    case any => any.asInstanceOf[Array[Byte]]
  }

  override def bytes(columnLabel: String): Array[Byte] = any(columnLabel) match {
    case null => null
    case any => any.asInstanceOf[Array[Byte]]
  }

  override def characterStream(columnIndex: Int): java.io.Reader = throw new UnsupportedOperationException
  override def characterStream(columnLabel: String): java.io.Reader = throw new UnsupportedOperationException

  override def clob(columnIndex: Int): java.sql.Clob = throw new UnsupportedOperationException
  override def clob(columnLabel: String): java.sql.Clob = throw new UnsupportedOperationException

  override def concurrency: Int = throw new UnsupportedOperationException
  override def cursorName: String = throw new UnsupportedOperationException

  override def date(columnIndex: Int): java.sql.Date = any(columnIndex) match {
    case null => null
    case d: java.sql.Date => d
    case t: java.sql.Time => new java.sql.Date(t.getTime)
    case t: java.sql.Timestamp => new java.sql.Date(t.getTime)
    case ldt: LocalDateTime => new java.sql.Date(ldt.toDateTime.getMillis)
    case dt: DateTime => new java.sql.Date(dt.getMillis)
    case t: LocalTime => t.toSqlTimestamp.toSqlDate
    case dt: java.util.Date => new java.sql.Date(dt.getTime)
    case fd: FiniteDuration => new java.sql.Date(fd.toMillis)
    case other => throw new UnsupportedOperationException(
      s"Please send a feedback to the library maintainers about supporting ${other.getClass} for #date(Int)!")
  }

  override def date(columnLabel: String): java.sql.Date = any(columnLabel) match {
    case null => null
    case d: java.sql.Date => d
    case t: java.sql.Time => new java.sql.Date(t.getTime)
    case t: java.sql.Timestamp => new java.sql.Date(t.getTime)
    case ldt: LocalDateTime => new java.sql.Date(ldt.toDateTime.getMillis)
    case dt: DateTime => new java.sql.Date(dt.getMillis)
    case t: LocalTime => t.toSqlTimestamp.toSqlDate
    case dt: java.util.Date => new java.sql.Date(dt.getTime)
    case fd: FiniteDuration => new java.sql.Date(fd.toMillis)
    case other => throw new UnsupportedOperationException(
      s"Please send a feedback to the library maintainers about supporting ${other.getClass} for #date(String)!")
  }

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

  override def any(columnIndex: Int): Any = throw new IllegalStateException("This method should be implemented.")
  override def any(columnLabel: String): Any = throw new IllegalStateException("This method should be implemented.")

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

  override def string(columnIndex: Int): String = any(columnIndex) match {
    case null => null
    case str: String => str
    case any => any.toString
  }

  override def string(columnLabel: String): String = any(columnLabel) match {
    case null => null
    case str: String => str
    case any => any.toString
  }

  override def time(columnIndex: Int): java.sql.Time = any(columnIndex) match {
    case null => null
    case t: java.sql.Time => t
    case d: java.sql.Date => new java.sql.Time(d.getTime)
    case t: java.sql.Timestamp => new java.sql.Time(t.getTime)
    case t: LocalTime => t.toSqlTime
    case ldt: LocalDateTime => new java.sql.Time(ldt.toDateTime.getMillis)
    case dt: DateTime => new java.sql.Time(dt.getMillis)
    case dt: java.util.Date => new java.sql.Time(dt.getTime)
    case fd: FiniteDuration => new java.sql.Time(fd.toMillis)
    case other => throw new UnsupportedOperationException(
      s"Please send a feedback to the library maintainers about supporting ${other.getClass} for #time(Int)!")
  }

  override def time(columnLabel: String): java.sql.Time = any(columnLabel) match {
    case null => null
    case t: java.sql.Time => t
    case d: java.sql.Date => new java.sql.Time(d.getTime)
    case t: java.sql.Timestamp => new java.sql.Time(t.getTime)
    case t: LocalTime => t.toSqlTime
    case ldt: LocalDateTime => new java.sql.Time(ldt.toDateTime.getMillis)
    case dt: DateTime => new java.sql.Time(dt.getMillis)
    case d: java.util.Date => new java.sql.Time(d.getTime)
    case fd: FiniteDuration => new java.sql.Time(fd.toMillis)
    case other => throw new UnsupportedOperationException(
      s"Please send a feedback to the library maintainers about supporting ${other.getClass} for #time(String)!")
  }

  override def time(columnIndex: Int, cal: Calendar): java.sql.Time = throw new UnsupportedOperationException
  override def time(columnLabel: String, cal: Calendar): java.sql.Time = throw new UnsupportedOperationException

  override def timestamp(columnIndex: Int): java.sql.Timestamp = any(columnIndex) match {
    case null => null
    case t: java.sql.Timestamp => t
    case t: java.sql.Time => new java.sql.Timestamp(t.getTime)
    case d: java.sql.Date => new java.sql.Timestamp(d.getTime)
    case ldt: LocalDateTime => new java.sql.Timestamp(ldt.toDate.getTime)
    case dt: DateTime => new java.sql.Timestamp(dt.getMillis)
    case t: LocalTime => t.toSqlTimestamp
    case d: java.util.Date => new java.sql.Timestamp(d.getTime)
    case fd: FiniteDuration => new java.sql.Timestamp(fd.toMillis)
    case other => throw new UnsupportedOperationException(
      s"Please send a feedback to the library maintainers about supporting ${other.getClass} for #timestamp(Int)!")
  }

  override def timestamp(columnLabel: String): java.sql.Timestamp = any(columnLabel) match {
    case null => null
    case t: java.sql.Timestamp => t
    case t: java.sql.Time => new java.sql.Timestamp(t.getTime)
    case d: java.sql.Date => new java.sql.Timestamp(d.getTime)
    case ldt: LocalDateTime => new java.sql.Timestamp(ldt.toDate.getTime)
    case dt: DateTime => new java.sql.Timestamp(dt.getMillis)
    case t: LocalTime => t.toSqlTimestamp
    case d: java.util.Date => new java.sql.Timestamp(d.getTime)
    case fd: FiniteDuration => new java.sql.Timestamp(fd.toMillis)
    case other => throw new UnsupportedOperationException(
      s"Please send a feedback to the library maintainers about supporting ${other.getClass} for #timestamp(String)!")
  }

  override def timestamp(columnIndex: Int, cal: Calendar): java.sql.Timestamp = throw new UnsupportedOperationException
  override def timestamp(columnLabel: String, cal: Calendar): java.sql.Timestamp = throw new UnsupportedOperationException

  override def url(columnIndex: Int): java.net.URL = any(columnIndex) match {
    case null => null
    case url: java.net.URL => url
    case str: String => new java.net.URL(str)
    case any => new java.net.URL(any.toString)
  }

  override def url(columnLabel: String): java.net.URL = any(columnLabel) match {
    case null => null
    case url: java.net.URL => url
    case str: String => new java.net.URL(str)
    case any => new java.net.URL(any.toString)
  }

  override def warnings: java.sql.SQLWarning = throw new UnsupportedOperationException

  override def toMap(): Map[String, Any] = throw new UnsupportedOperationException
  override def toSymbolMap(): Map[Symbol, Any] = throw new UnsupportedOperationException

}

