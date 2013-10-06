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

import scalikejdbc._
import com.github.mauricio.async.db.RowData
import scalikejdbc.WrappedResultSet
import scalikejdbc.async.AsyncResultSet
import org.joda.time._
import scala.concurrent.duration.FiniteDuration

/**
 * ResultSet Implementation
 */
private[scalikejdbc] class AsyncResultSetImpl(rows: IndexedSeq[RowData])
    extends WrappedResultSet(null, null, 0)
    with AsyncResultSet {

  private[this] val currentRow: Option[RowData] = rows.headOption

  // AsyncResultSet API

  override def next(): Boolean = rows.headOption.isDefined
  override def tail(): AsyncResultSet = new AsyncResultSetImpl(rows.tail)

  // WrappedResultSet API

  override def any(columnIndex: Int): Any = currentRow.map(_.apply(columnIndex)).orNull[Any]
  override def any(columnLabel: String): Any = currentRow.map(_.apply(columnLabel)).orNull[Any]

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

  override def bytes(columnIndex: Int): Array[Byte] = any(columnIndex) match {
    case null => null
    case any => any.asInstanceOf[Array[Byte]]
  }

  override def bytes(columnLabel: String): Array[Byte] = any(columnLabel) match {
    case null => null
    case any => any.asInstanceOf[Array[Byte]]
  }

  override def date(columnIndex: Int): java.sql.Date = any(columnIndex) match {
    case null => null
    case d: java.sql.Date => d
    case t: java.sql.Time => new java.sql.Date(t.getTime)
    case t: java.sql.Timestamp => new java.sql.Date(t.getTime)
    case ldt: LocalDateTime => new java.sql.Date(ldt.toDateTime.getMillis)
    case ld: LocalDate => new java.sql.Date(ld.toDate.getTime)
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
    case ld: LocalDate => new java.sql.Date(ld.toDate.getTime)
    case dt: DateTime => new java.sql.Date(dt.getMillis)
    case t: LocalTime => t.toSqlTimestamp.toSqlDate
    case dt: java.util.Date => new java.sql.Date(dt.getTime)
    case fd: FiniteDuration => new java.sql.Date(fd.toMillis)
    case other => throw new UnsupportedOperationException(
      s"Please send a feedback to the library maintainers about supporting ${other.getClass} for #date(String)!")
  }

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
    case ld: LocalDate => new java.sql.Time(ld.toDate.getTime)
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
    case ld: LocalDate => new java.sql.Time(ld.toDate.getTime)
    case other => throw new UnsupportedOperationException(
      s"Please send a feedback to the library maintainers about supporting ${other.getClass} for #time(String)!")
  }

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

}
