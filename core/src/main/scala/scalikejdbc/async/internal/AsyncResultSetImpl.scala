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
import org.joda.time.{ LocalDateTime, LocalTime, LocalDate, DateTime }

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

  override def any(columnIndex: Int): Any = {
    // To be compatible with JDBC, index should be 1-origin
    // But postgresql-async/mysql-async is 0-origin
    val index0origin = columnIndex - 1
    currentRow.map(_.apply(index0origin)).orNull[Any]
  }

  override def any(columnLabel: String): Any = currentRow.map(_.apply(columnLabel)).orNull[Any]

  override def anyOpt(columnIndex: Int): Option[Any] = Option(any(columnIndex))

  override def anyOpt(columnLabel: String): Option[Any] = Option(any(columnLabel))

  private def anyToBigDecimal(any: Any): java.math.BigDecimal = any match {
    case null => null
    case bd: java.math.BigDecimal => bd
    case bd: scala.BigDecimal => bd.underlying
    case str: String => new java.math.BigDecimal(str)
    case _ => new java.math.BigDecimal(any.toString)
  }
  override def bigDecimal(columnIndex: Int): java.math.BigDecimal = anyToBigDecimal(any(columnIndex))
  override def bigDecimal(columnLabel: String): java.math.BigDecimal = anyToBigDecimal(any(columnLabel))
  override def bigDecimalOpt(columnIndex: Int): Option[java.math.BigDecimal] = Option(bigDecimal(columnIndex))
  override def bigDecimalOpt(columnLabel: String): Option[java.math.BigDecimal] = Option(bigDecimal(columnLabel))

  private def anyToBytes(any: Any): Array[Byte] = any.asInstanceOf[Array[Byte]]
  override def bytes(columnIndex: Int): Array[Byte] = anyToBytes(any(columnIndex))
  override def bytes(columnLabel: String): Array[Byte] = anyToBytes(any(columnLabel))
  override def bytesOpt(columnIndex: Int): Option[Array[Byte]] = Option(bytes(columnIndex))
  override def bytesOpt(columnLabel: String): Option[Array[Byte]] = Option(bytes(columnLabel))

  private def anyToDate(any: Any): java.sql.Date = any match {
    case null => null
    case d: java.sql.Date => d
    case TimeInMillis(ms) => new UnixTimeInMillisConverter(ms).toSqlDate
    case other => throw new UnsupportedOperationException(
      s"Please send a feedback to the library maintainers about supporting ${other.getClass} for #date!")
  }
  override def date(columnIndex: Int): java.sql.Date = anyToDate(any(columnIndex))
  override def date(columnLabel: String): java.sql.Date = anyToDate(any(columnLabel))
  override def dateOpt(columnIndex: Int): Option[java.sql.Date] = Option(date(columnIndex))
  override def dateOpt(columnLabel: String): Option[java.sql.Date] = Option(date(columnLabel))

  private def anyToString(any: Any): String = any match {
    case null => null
    case str: String => str
    case _ => any.toString
  }
  override def string(columnIndex: Int): String = anyToString(any(columnIndex))
  override def string(columnLabel: String): String = anyToString(any(columnLabel))
  override def stringOpt(columnIndex: Int): Option[String] = Option(string(columnIndex))
  override def stringOpt(columnLabel: String): Option[String] = Option(string(columnLabel))

  private def anyToTime(any: Any): java.sql.Time = any match {
    case null => null
    case t: java.sql.Time => t
    case TimeInMillis(ms) => new java.sql.Time(ms)
    case other => throw new UnsupportedOperationException(
      s"Please send a feedback to the library maintainers about supporting ${other.getClass} for #time!")
  }
  override def time(columnIndex: Int): java.sql.Time = anyToTime(any(columnIndex))
  override def time(columnLabel: String): java.sql.Time = anyToTime(any(columnLabel))
  override def timeOpt(columnIndex: Int): Option[java.sql.Time] = Option(time(columnIndex))
  override def timeOpt(columnLabel: String): Option[java.sql.Time] = Option(time(columnLabel))

  private def anyToTimestamp(any: Any): java.sql.Timestamp = any match {
    case null => null
    case t: java.sql.Timestamp => t
    case TimeInMillis(ms) => new java.sql.Timestamp(ms)
    case other => throw new UnsupportedOperationException(
      s"Please send a feedback to the library maintainers about supporting ${other.getClass} for #timestamp!")
  }
  override def timestamp(columnIndex: Int): java.sql.Timestamp = anyToTimestamp(any(columnIndex))
  override def timestamp(columnLabel: String): java.sql.Timestamp = anyToTimestamp(any(columnLabel))
  override def timestampOpt(columnIndex: Int): Option[java.sql.Timestamp] = Option(timestamp(columnIndex))
  override def timestampOpt(columnLabel: String): Option[java.sql.Timestamp] = Option(timestamp(columnLabel))

  private def anyToDateTime(any: Any): DateTime = any match {
    case null => null
    case dt: DateTime => dt
    case TimeInMillis(ms) => new DateTime(ms)
    case other => throw new UnsupportedOperationException(
      s"Please send a feedback to the library maintainers about supporting ${other.getClass} for #dateTime!")
  }
  override def dateTime(columnIndex: Int): DateTime = anyToDateTime(any(columnIndex))
  override def dateTime(columnLabel: String): DateTime = anyToDateTime(any(columnLabel))
  override def dateTimeOpt(columnIndex: Int): Option[DateTime] = Option(dateTime(columnIndex))
  override def dateTimeOpt(columnLabel: String): Option[DateTime] = Option(dateTime(columnLabel))

  private def anyToLocalDateTime(any: Any): LocalDateTime = any match {
    case null => null
    case ldt: LocalDateTime => ldt
    case TimeInMillis(ms) => new LocalDateTime(ms)
    case other => throw new UnsupportedOperationException(
      s"Please send a feedback to the library maintainers about supporting ${other.getClass} for #localDateTime!")
  }
  override def localDateTime(columnIndex: Int): LocalDateTime = anyToLocalDateTime(any(columnIndex))
  override def localDateTime(columnLabel: String): LocalDateTime = anyToLocalDateTime(any(columnLabel))
  override def localDateTimeOpt(columnIndex: Int): Option[LocalDateTime] = Option(localDateTime(columnIndex))
  override def localDateTimeOpt(columnLabel: String): Option[LocalDateTime] = Option(localDateTime(columnLabel))

  private def anyToLocalDate(any: Any): LocalDate = any match {
    case null => null
    case ld: LocalDate => ld
    case TimeInMillis(ms) => new LocalDate(ms)
    case other => throw new UnsupportedOperationException(
      s"Please send a feedback to the library maintainers about supporting ${other.getClass} for #localDate!")
  }
  override def localDate(columnIndex: Int): LocalDate = anyToLocalDate(any(columnIndex))
  override def localDate(columnLabel: String): LocalDate = anyToLocalDate(any(columnLabel))
  override def localDateOpt(columnIndex: Int): Option[LocalDate] = Option(localDate(columnIndex))
  override def localDateOpt(columnLabel: String): Option[LocalDate] = Option(localDate(columnLabel))

  private def anyToLocalTime(any: Any): LocalTime = any match {
    case null => null
    case lt: LocalTime => lt
    case TimeInMillis(ms) => new LocalTime(ms)
    case other => throw new UnsupportedOperationException(
      s"Please send a feedback to the library maintainers about supporting ${other.getClass} for #localTime!")
  }
  override def localTime(columnIndex: Int): LocalTime = anyToLocalTime(any(columnIndex))
  override def localTime(columnLabel: String): LocalTime = anyToLocalTime(any(columnLabel))
  override def localTimeOpt(columnIndex: Int): Option[LocalTime] = Option(localTime(columnIndex))
  override def localTimeOpt(columnLabel: String): Option[LocalTime] = Option(localTime(columnLabel))

  private def anyToUrl(any: Any): java.net.URL = any match {
    case null => null
    case url: java.net.URL => url
    case str: String => new java.net.URL(str)
    case _ => new java.net.URL(any.toString)
  }
  override def url(columnIndex: Int): java.net.URL = anyToUrl(any(columnIndex))
  override def url(columnLabel: String): java.net.URL = anyToUrl(any(columnLabel))
  override def urlOpt(columnIndex: Int): Option[java.net.URL] = Option(url(columnIndex))
  override def urlOpt(columnLabel: String): Option[java.net.URL] = Option(url(columnLabel))

  private def anyToNullableBoolean(any: Any): java.lang.Boolean = any match {
    case null => null
    case b: java.lang.Boolean => b
    case b: Boolean => b
    case s: String => {
      try s.toInt != 0
      catch { case e: NumberFormatException => !s.isEmpty }
    }.asInstanceOf[java.lang.Boolean]
    case v => (v != 0).asInstanceOf[java.lang.Boolean]
  }
  override def nullableBoolean(columnIndex: Int): java.lang.Boolean = anyToNullableBoolean(any(columnIndex))
  override def nullableBoolean(columnLabel: String): java.lang.Boolean = anyToNullableBoolean(any(columnLabel))
  override def boolean(columnIndex: Int): Boolean = nullableBoolean(columnIndex).asInstanceOf[Boolean]
  override def boolean(columnLabel: String): Boolean = nullableBoolean(columnLabel).asInstanceOf[Boolean]
  override def booleanOpt(columnIndex: Int): Option[Boolean] = opt[Boolean](nullableBoolean(columnIndex))
  override def booleanOpt(columnLabel: String): Option[Boolean] = opt[Boolean](nullableBoolean(columnLabel))

  private def anyToNullableByte(any: Any): java.lang.Byte = any match {
    case null => null
    case _ => java.lang.Byte.valueOf(any.toString)
  }
  override def nullableByte(columnIndex: Int): java.lang.Byte = anyToNullableByte(any(columnIndex))
  override def nullableByte(columnLabel: String): java.lang.Byte = anyToNullableByte(any(columnLabel))
  override def byte(columnIndex: Int): Byte = nullableByte(columnIndex).asInstanceOf[Byte]
  override def byte(columnLabel: String): Byte = nullableByte(columnLabel).asInstanceOf[Byte]
  override def byteOpt(columnIndex: Int): Option[Byte] = opt[Byte](nullableByte(columnIndex))
  override def byteOpt(columnLabel: String): Option[Byte] = opt[Byte](nullableByte(columnLabel))

  private def anyToNullableDouble(any: Any): java.lang.Double = any match {
    case null => null
    case _ => java.lang.Double.valueOf(any.toString)
  }
  override def nullableDouble(columnIndex: Int): java.lang.Double = anyToNullableDouble(any(columnIndex))
  override def nullableDouble(columnLabel: String): java.lang.Double = anyToNullableDouble(any(columnLabel))
  override def double(columnIndex: Int): Double = nullableDouble(columnIndex).asInstanceOf[Double]
  override def double(columnLabel: String): Double = nullableDouble(columnLabel).asInstanceOf[Double]
  override def doubleOpt(columnIndex: Int): Option[Double] = opt[Double](nullableDouble(columnIndex))
  override def doubleOpt(columnLabel: String): Option[Double] = opt[Double](nullableDouble(columnLabel))

  private def anyToNullableFloat(any: Any): java.lang.Float = any match {
    case null => null
    case _ => java.lang.Float.valueOf(any.toString)
  }
  override def nullableFloat(columnIndex: Int): java.lang.Float = anyToNullableFloat(any(columnIndex))
  override def nullableFloat(columnLabel: String): java.lang.Float = anyToNullableFloat(any(columnLabel))
  override def float(columnIndex: Int): Float = nullableFloat(columnIndex).asInstanceOf[Float]
  override def float(columnLabel: String): Float = nullableFloat(columnLabel).asInstanceOf[Float]
  override def floatOpt(columnIndex: Int): Option[Float] = opt[Float](nullableFloat(columnIndex))
  override def floatOpt(columnLabel: String): Option[Float] = opt[Float](nullableFloat(columnLabel))

  private def anyToNullableInt(any: Any): java.lang.Integer = any match {
    case null => null
    case v: Float => v.toInt.asInstanceOf[java.lang.Integer]
    case v: Double => v.toInt.asInstanceOf[java.lang.Integer]
    case v => java.lang.Integer.valueOf(v.toString)
  }
  override def nullableInt(columnIndex: Int): java.lang.Integer = anyToNullableInt(any(columnIndex))
  override def nullableInt(columnLabel: String): java.lang.Integer = anyToNullableInt(any(columnLabel))
  override def int(columnIndex: Int): Int = nullableInt(columnIndex).asInstanceOf[Int]
  override def int(columnLabel: String): Int = nullableInt(columnLabel).asInstanceOf[Int]
  override def intOpt(columnIndex: Int): Option[Int] = opt[Int](nullableInt(columnIndex))
  override def intOpt(columnLabel: String): Option[Int] = opt[Int](nullableInt(columnLabel))

  private def anyToNullableLong(any: Any): java.lang.Long = any match {
    case null => null
    case v: Float => v.toLong.asInstanceOf[java.lang.Long]
    case v: Double => v.toLong.asInstanceOf[java.lang.Long]
    case v => java.lang.Long.valueOf(v.toString)
  }
  override def nullableLong(columnIndex: Int): java.lang.Long = anyToNullableLong(any(columnIndex))
  override def nullableLong(columnLabel: String): java.lang.Long = anyToNullableLong(any(columnLabel))
  override def long(columnIndex: Int): Long = nullableLong(columnIndex).asInstanceOf[Long]
  override def long(columnLabel: String): Long = nullableLong(columnLabel).asInstanceOf[Long]
  override def longOpt(columnIndex: Int): Option[Long] = opt[Long](nullableLong(columnIndex))
  override def longOpt(columnLabel: String): Option[Long] = opt[Long](nullableLong(columnLabel))

  private def anyToNullableShort(any: Any): java.lang.Short = any match {
    case null => null
    case v: Float => v.toShort.asInstanceOf[java.lang.Short]
    case v: Double => v.toShort.asInstanceOf[java.lang.Short]
    case v => java.lang.Short.valueOf(v.toString)
  }
  override def nullableShort(columnIndex: Int): java.lang.Short = anyToNullableShort(any(columnIndex))
  override def nullableShort(columnLabel: String): java.lang.Short = anyToNullableShort(any(columnLabel))
  override def short(columnIndex: Int): Short = nullableShort(columnIndex).asInstanceOf[Short]
  override def short(columnLabel: String): Short = nullableShort(columnLabel).asInstanceOf[Short]
  override def shortOpt(columnIndex: Int): Option[Short] = opt[Short](nullableShort(columnIndex))
  override def shortOpt(columnLabel: String): Option[Short] = opt[Short](nullableShort(columnLabel))

}
