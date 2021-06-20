/*
 * Copyright 2017 Mike Toggweiler
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

import java.sql.PreparedStatement

/**
 * Class to transformed value from PreparedStatement interface to be able to stick to the same process as scalikejdbc does convert parameter binder.
 */
class MockPreparedStatement extends PreparedStatement {
  var value: Any = _

  def addBatch(): Unit = notSupported
  def clearParameters(): Unit = notSupported
  def execute(): Boolean = notSupported
  def executeQuery(): java.sql.ResultSet = notSupported
  def executeUpdate(): Int = notSupported
  def getMetaData(): java.sql.ResultSetMetaData = notSupported
  def getParameterMetaData(): java.sql.ParameterMetaData = notSupported
  def setArray(index: Int, pValue: java.sql.Array): Unit = value = pValue
  def setAsciiStream(index: Int, pValue: java.io.InputStream): Unit = value =
    pValue
  def setAsciiStream(index: Int, pValue: java.io.InputStream, x$3: Long): Unit =
    value = pValue
  def setAsciiStream(index: Int, pValue: java.io.InputStream, x$3: Int): Unit =
    value = pValue
  def setBigDecimal(index: Int, pValue: java.math.BigDecimal): Unit = value =
    pValue
  def setBinaryStream(index: Int, pValue: java.io.InputStream): Unit = value =
    pValue
  def setBinaryStream(
    index: Int,
    pValue: java.io.InputStream,
    x$3: Long
  ): Unit = value = pValue
  def setBinaryStream(index: Int, pValue: java.io.InputStream, x$3: Int): Unit =
    value = pValue
  def setBlob(index: Int, pValue: java.io.InputStream): Unit = value = pValue
  def setBlob(index: Int, pValue: java.io.InputStream, x$3: Long): Unit =
    value = pValue
  def setBlob(index: Int, pValue: java.sql.Blob): Unit = value = pValue
  def setBoolean(index: Int, pValue: Boolean): Unit = value = pValue
  def setByte(index: Int, pValue: Byte): Unit = value = pValue
  def setBytes(index: Int, pValue: Array[Byte]): Unit = value = pValue
  def setCharacterStream(index: Int, pValue: java.io.Reader): Unit = value =
    pValue
  def setCharacterStream(index: Int, pValue: java.io.Reader, x$3: Long): Unit =
    value = pValue
  def setCharacterStream(index: Int, pValue: java.io.Reader, x$3: Int): Unit =
    value = pValue
  def setClob(index: Int, pValue: java.io.Reader): Unit = value = pValue
  def setClob(index: Int, pValue: java.io.Reader, x$3: Long): Unit = value =
    pValue
  def setClob(index: Int, pValue: java.sql.Clob): Unit = value = pValue
  def setDate(
    index: Int,
    pValue: java.sql.Date,
    x$3: java.util.Calendar
  ): Unit = value = pValue
  def setDate(index: Int, pValue: java.sql.Date): Unit = value = pValue
  def setDouble(index: Int, pValue: Double): Unit = value = pValue
  def setFloat(index: Int, pValue: Float): Unit = value = pValue
  def setInt(index: Int, pValue: Int): Unit = value = pValue
  def setLong(index: Int, pValue: Long) = { value = pValue }
  def setNCharacterStream(index: Int, pValue: java.io.Reader): Unit = value =
    pValue
  def setNCharacterStream(index: Int, pValue: java.io.Reader, x$3: Long): Unit =
    value = pValue
  def setNClob(index: Int, pValue: java.io.Reader): Unit = value = pValue
  def setNClob(index: Int, pValue: java.io.Reader, x$3: Long): Unit = value =
    pValue
  def setNClob(index: Int, pValue: java.sql.NClob): Unit = value = pValue
  def setNString(index: Int, pValue: String): Unit = value = pValue
  def setNull(index: Int, pValue: Int, x$3: String): Unit = value = pValue
  def setNull(index: Int, pValue: Int): Unit = value = pValue
  def setObject(index: Int, pValue: Any, x$3: Int, x$4: Int): Unit = value =
    pValue
  def setObject(index: Int, pValue: Any): Unit = value = pValue
  def setObject(index: Int, pValue: Any, x$3: Int): Unit = value = pValue
  def setRef(index: Int, pValue: java.sql.Ref): Unit = value = pValue
  def setRowId(index: Int, pValue: java.sql.RowId): Unit = value = pValue
  def setSQLXML(index: Int, pValue: java.sql.SQLXML): Unit = value = pValue
  def setShort(index: Int, pValue: Short): Unit = value = pValue
  def setString(index: Int, pValue: String): Unit = value = pValue
  def setTime(
    index: Int,
    pValue: java.sql.Time,
    x$3: java.util.Calendar
  ): Unit = value = pValue
  def setTime(index: Int, pValue: java.sql.Time): Unit = value = pValue
  def setTimestamp(
    index: Int,
    pValue: java.sql.Timestamp,
    x$3: java.util.Calendar
  ): Unit = value = pValue
  def setTimestamp(index: Int, pValue: java.sql.Timestamp): Unit = value =
    pValue
  def setURL(index: Int, pValue: java.net.URL): Unit = value = pValue
  def setUnicodeStream(
    index: Int,
    pValue: java.io.InputStream,
    x$3: Int
  ): Unit = value = pValue

  // Members declared in java.sql.Statement
  def addBatch(index: String): Unit = notSupported
  def cancel(): Unit = notSupported
  def clearBatch(): Unit = notSupported
  def clearWarnings(): Unit = notSupported
  def close(): Unit = notSupported
  def closeOnCompletion(): Unit = notSupported
  def execute(index: String, pValue: Array[String]): Boolean = notSupported
  def execute(index: String, pValue: Array[Int]): Boolean = notSupported
  def execute(index: String, pValue: Int): Boolean = notSupported
  def execute(index: String): Boolean = notSupported
  def executeBatch(): Array[Int] = notSupported
  def executeQuery(index: String): java.sql.ResultSet = notSupported
  def executeUpdate(index: String, pValue: Array[String]): Int = notSupported
  def executeUpdate(index: String, pValue: Array[Int]): Int = notSupported
  def executeUpdate(index: String, pValue: Int): Int = notSupported
  def executeUpdate(index: String): Int = throw new RuntimeException
  def getConnection(): java.sql.Connection = notSupported
  def getFetchDirection(): Int = notSupported
  def getFetchSize(): Int = notSupported
  def getGeneratedKeys(): java.sql.ResultSet = notSupported
  def getMaxFieldSize(): Int = notSupported
  def getMaxRows(): Int = notSupported
  def getMoreResults(index: Int): Boolean = notSupported
  def getMoreResults(): Boolean = notSupported
  def getQueryTimeout(): Int = notSupported
  def getResultSet(): java.sql.ResultSet = notSupported
  def getResultSetConcurrency(): Int = notSupported
  def getResultSetHoldability(): Int = notSupported
  def getResultSetType(): Int = notSupported
  def getUpdateCount(): Int = notSupported
  def getWarnings(): java.sql.SQLWarning = notSupported
  def isCloseOnCompletion(): Boolean = notSupported
  def isClosed(): Boolean = notSupported
  def isPoolable(): Boolean = notSupported
  def setCursorName(index: String): Unit = notSupported
  def setEscapeProcessing(index: Boolean): Unit = notSupported
  def setFetchDirection(index: Int): Unit = notSupported
  def setFetchSize(index: Int): Unit = notSupported
  def setMaxFieldSize(index: Int): Unit = notSupported
  def setMaxRows(index: Int): Unit = notSupported
  def setPoolable(index: Boolean): Unit = notSupported
  def setQueryTimeout(index: Int): Unit = notSupported

  // Members declared in java.sql.Wrapper
  def isWrapperFor(index: Class[_]): Boolean = notSupported
  def unwrap[T](index: Class[T]): T = notSupported

  private def notSupported = throw new RuntimeException("Not supported")
}
