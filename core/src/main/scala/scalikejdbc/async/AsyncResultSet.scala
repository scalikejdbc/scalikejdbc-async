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

  override final def get[A: TypeBinder](columnLabel: String): A = throw new UnsupportedOperationException
  override final def get[A: TypeBinder](columnIndex: Int): A = throw new UnsupportedOperationException

  override final def ensureCursor(): Unit = {}

  override final def fetchDirection: Int = throw new UnsupportedOperationException
  override final def fetchSize: Int = throw new UnsupportedOperationException
  override final def holdability: Int = throw new UnsupportedOperationException
  override final def metaData: java.sql.ResultSetMetaData = throw new UnsupportedOperationException
  override final def row: Int = throw new UnsupportedOperationException
  override final def statement: java.sql.Statement = throw new UnsupportedOperationException
  override final def warnings: java.sql.SQLWarning = throw new UnsupportedOperationException

  override def any(columnIndex: Int): Any
  override def any(columnLabel: String): Any
  override def anyOpt(columnIndex: Int): Option[Any]
  override def anyOpt(columnLabel: String): Option[Any]

  override def bigDecimal(columnIndex: Int): java.math.BigDecimal
  override def bigDecimal(columnLabel: String): java.math.BigDecimal
  override def bigDecimalOpt(columnIndex: Int): Option[java.math.BigDecimal]
  override def bigDecimalOpt(columnLabel: String): Option[java.math.BigDecimal]

  override def bytes(columnIndex: Int): Array[Byte]
  override def bytes(columnLabel: String): Array[Byte]
  override def bytesOpt(columnIndex: Int): Option[Array[Byte]]
  override def bytesOpt(columnLabel: String): Option[Array[Byte]]

  override def date(columnIndex: Int): java.sql.Date
  override def date(columnLabel: String): java.sql.Date
  override def dateOpt(columnIndex: Int): Option[java.sql.Date]
  override def dateOpt(columnLabel: String): Option[java.sql.Date]

  override def string(columnIndex: Int): String
  override def string(columnLabel: String): String
  override def stringOpt(columnIndex: Int): Option[String]
  override def stringOpt(columnLabel: String): Option[String]

  override def time(columnIndex: Int): java.sql.Time
  override def time(columnLabel: String): java.sql.Time
  override def timeOpt(columnIndex: Int): Option[java.sql.Time]
  override def timeOpt(columnLabel: String): Option[java.sql.Time]

  override def timestamp(columnIndex: Int): java.sql.Timestamp
  override def timestamp(columnLabel: String): java.sql.Timestamp
  override def timestampOpt(columnIndex: Int): Option[java.sql.Timestamp]
  override def timestampOpt(columnLabel: String): Option[java.sql.Timestamp]

  override def dateTime(columnIndex: Int): DateTime
  override def dateTime(columnLabel: String): DateTime
  override def dateTimeOpt(columnIndex: Int): Option[DateTime]
  override def dateTimeOpt(columnLabel: String): Option[DateTime]

  override def localDate(columnIndex: Int): LocalDate
  override def localDate(columnLabel: String): LocalDate
  override def localDateOpt(columnIndex: Int): Option[LocalDate]
  override def localDateOpt(columnLabel: String): Option[LocalDate]

  override def localDateTime(columnIndex: Int): LocalDateTime
  override def localDateTime(columnLabel: String): LocalDateTime
  override def localDateTimeOpt(columnIndex: Int): Option[LocalDateTime]
  override def localDateTimeOpt(columnLabel: String): Option[LocalDateTime]

  override def localTime(columnIndex: Int): LocalTime
  override def localTime(columnLabel: String): LocalTime
  override def localTimeOpt(columnIndex: Int): Option[LocalTime]
  override def localTimeOpt(columnLabel: String): Option[LocalTime]

  override def url(columnIndex: Int): java.net.URL
  override def url(columnLabel: String): java.net.URL
  override def urlOpt(columnIndex: Int): Option[java.net.URL]
  override def urlOpt(columnLabel: String): Option[java.net.URL]

  override def nullableBoolean(columnIndex: Int): java.lang.Boolean
  override def nullableBoolean(columnLabel: String): java.lang.Boolean
  override def boolean(columnIndex: Int): Boolean
  override def boolean(columnLabel: String): Boolean
  override def booleanOpt(columnIndex: Int): Option[Boolean]
  override def booleanOpt(columnLabel: String): Option[Boolean]

  override def nullableByte(columnIndex: Int): java.lang.Byte
  override def nullableByte(columnLabel: String): java.lang.Byte
  override def byte(columnIndex: Int): Byte
  override def byte(columnLabel: String): Byte
  override def byteOpt(columnIndex: Int): Option[Byte]
  override def byteOpt(columnLabel: String): Option[Byte]

  override def nullableDouble(columnIndex: Int): java.lang.Double
  override def nullableDouble(columnLabel: String): java.lang.Double
  override def double(columnIndex: Int): Double
  override def double(columnLabel: String): Double
  override def doubleOpt(columnIndex: Int): Option[Double]
  override def doubleOpt(columnLabel: String): Option[Double]

  override def nullableFloat(columnIndex: Int): java.lang.Float
  override def nullableFloat(columnLabel: String): java.lang.Float
  override def float(columnIndex: Int): Float
  override def float(columnLabel: String): Float
  override def floatOpt(columnIndex: Int): Option[Float]
  override def floatOpt(columnLabel: String): Option[Float]

  override def nullableInt(columnIndex: Int): java.lang.Integer
  override def nullableInt(columnLabel: String): java.lang.Integer
  override def int(columnIndex: Int): Int
  override def int(columnLabel: String): Int
  override def intOpt(columnIndex: Int): Option[Int]
  override def intOpt(columnLabel: String): Option[Int]

  override def nullableLong(columnIndex: Int): java.lang.Long
  override def nullableLong(columnLabel: String): java.lang.Long
  override def long(columnIndex: Int): Long
  override def long(columnLabel: String): Long
  override def longOpt(columnIndex: Int): Option[Long]
  override def longOpt(columnLabel: String): Option[Long]

  override def nullableShort(columnIndex: Int): java.lang.Short
  override def nullableShort(columnLabel: String): java.lang.Short
  override def short(columnIndex: Int): Short
  override def short(columnLabel: String): Short
  override def shortOpt(columnIndex: Int): Option[Short]
  override def shortOpt(columnLabel: String): Option[Short]

  override def toMap(): Map[String, Any] = throw new UnsupportedOperationException
  override def toSymbolMap(): Map[Symbol, Any] = throw new UnsupportedOperationException

}
