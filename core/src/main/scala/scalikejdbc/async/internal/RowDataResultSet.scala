package scalikejdbc.async.internal

import java.io.{ StringReader, ByteArrayInputStream, Reader, InputStream }
import java.math.BigDecimal
import java.net.URL
import java.sql._
import java.time.Duration
import java.util
import java.util.Calendar

import com.github.jasync.sql.db.RowData
import scalikejdbc._

private[scalikejdbc] class RowDataResultSet(
  var currentRow: Option[RowData],
  var other: Iterable[RowData]
) extends ResultSet {

  var closed = false

  override def next(): Boolean = {
    currentRow = other.headOption
    other = other.drop(1)
    currentRow.isDefined
  }

  private def notsupported = throw new SQLFeatureNotSupportedException
  private def notvalid = throw new SQLException(
    s"Not valid method called on forward only, read only result set"
  )
  private def expectedbutnotsupported = throw new UnsupportedOperationException

  override def getType: Int = ResultSet.TYPE_FORWARD_ONLY

  override def isBeforeFirst: Boolean = notsupported

  override def updateString(columnIndex: Int, x: String): Unit = notvalid

  override def updateString(columnLabel: String, x: String): Unit = notvalid

  override def getTimestamp(columnIndex: Int): Timestamp = timestamp(
    columnIndex
  )

  override def getTimestamp(columnLabel: String): Timestamp = timestamp(
    columnLabel
  )

  override def getTimestamp(columnIndex: Int, cal: Calendar): Timestamp = {
    cal.setTimeInMillis(timestamp(columnIndex).getTime)
    new Timestamp(cal.getTimeInMillis)
  }

  override def getTimestamp(columnLabel: String, cal: Calendar): Timestamp = {
    cal.setTimeInMillis(timestamp(columnLabel).getTime)
    new Timestamp(cal.getTimeInMillis)
  }

  override def updateNString(columnIndex: Int, nString: String): Unit = notvalid

  override def updateNString(columnLabel: String, nString: String): Unit =
    notvalid

  override def clearWarnings(): Unit = {
    // noop
  }

  override def updateTimestamp(columnIndex: Int, x: Timestamp): Unit = notvalid

  override def updateTimestamp(columnLabel: String, x: Timestamp): Unit =
    notvalid

  override def updateByte(columnIndex: Int, x: Byte): Unit = notvalid

  override def updateByte(columnLabel: String, x: Byte): Unit = notvalid

  override def updateBigDecimal(columnIndex: Int, x: BigDecimal): Unit =
    notvalid

  override def updateBigDecimal(columnLabel: String, x: BigDecimal): Unit =
    notvalid

  override def updateDouble(columnIndex: Int, x: Double): Unit = notvalid

  override def updateDouble(columnLabel: String, x: Double): Unit = notvalid

  override def updateDate(columnIndex: Int, x: Date): Unit = notvalid

  override def updateDate(columnLabel: String, x: Date): Unit = notvalid

  override def isAfterLast: Boolean = notsupported

  override def updateBoolean(columnIndex: Int, x: Boolean): Unit = notvalid

  override def updateBoolean(columnLabel: String, x: Boolean): Unit = notvalid

  override def getBinaryStream(columnIndex: Int): InputStream =
    new ByteArrayInputStream(bytes(columnIndex))

  override def getBinaryStream(columnLabel: String): InputStream =
    new ByteArrayInputStream(bytes(columnLabel))

  override def beforeFirst(): Unit = notvalid

  override def updateNCharacterStream(
    columnIndex: Int,
    x: Reader,
    length: Long
  ): Unit = notvalid

  override def updateNCharacterStream(
    columnLabel: String,
    reader: Reader,
    length: Long
  ): Unit = notvalid

  override def updateNCharacterStream(columnIndex: Int, x: Reader): Unit =
    notvalid

  override def updateNCharacterStream(
    columnLabel: String,
    reader: Reader
  ): Unit = notvalid

  override def updateNClob(columnIndex: Int, nClob: NClob): Unit = notvalid

  override def updateNClob(columnLabel: String, nClob: NClob): Unit = notvalid

  override def updateNClob(
    columnIndex: Int,
    reader: Reader,
    length: Long
  ): Unit = notvalid

  override def updateNClob(
    columnLabel: String,
    reader: Reader,
    length: Long
  ): Unit = notvalid

  override def updateNClob(columnIndex: Int, reader: Reader): Unit = notvalid

  override def updateNClob(columnLabel: String, reader: Reader): Unit = notvalid

  override def last(): Boolean = notvalid

  override def isLast: Boolean = notsupported

  override def getNClob(columnIndex: Int): NClob = notsupported

  override def getNClob(columnLabel: String): NClob = notsupported

  override def getCharacterStream(columnIndex: Int): Reader = new StringReader(
    string(columnIndex)
  )

  override def getCharacterStream(columnLabel: String): Reader =
    new StringReader(string(columnLabel))

  override def updateArray(columnIndex: Int, x: Array): Unit = notvalid

  override def updateArray(columnLabel: String, x: Array): Unit = notvalid

  override def updateBlob(columnIndex: Int, x: Blob): Unit = notvalid

  override def updateBlob(columnLabel: String, x: Blob): Unit = notvalid

  override def updateBlob(
    columnIndex: Int,
    inputStream: InputStream,
    length: Long
  ): Unit = notvalid

  override def updateBlob(
    columnLabel: String,
    inputStream: InputStream,
    length: Long
  ): Unit = notvalid

  override def updateBlob(columnIndex: Int, inputStream: InputStream): Unit =
    notvalid

  override def updateBlob(columnLabel: String, inputStream: InputStream): Unit =
    notvalid

  override def getDouble(columnIndex: Int): Double = double(columnIndex)

  override def getDouble(columnLabel: String): Double = double(columnLabel)

  override def getArray(columnIndex: Int): Array = notsupported

  override def getArray(columnLabel: String): Array = notsupported

  override def isFirst: Boolean = notsupported

  override def getURL(columnIndex: Int): URL = url(columnIndex)

  override def getURL(columnLabel: String): URL = url(columnLabel)

  override def updateRow(): Unit = notvalid

  override def insertRow(): Unit = notvalid

  override def getMetaData: ResultSetMetaData = expectedbutnotsupported

  override def updateBinaryStream(
    columnIndex: Int,
    x: InputStream,
    length: Int
  ): Unit = notvalid

  override def updateBinaryStream(
    columnLabel: String,
    x: InputStream,
    length: Int
  ): Unit = notvalid

  override def updateBinaryStream(
    columnIndex: Int,
    x: InputStream,
    length: Long
  ): Unit = notvalid

  override def updateBinaryStream(
    columnLabel: String,
    x: InputStream,
    length: Long
  ): Unit = notvalid

  override def updateBinaryStream(columnIndex: Int, x: InputStream): Unit =
    notvalid

  override def updateBinaryStream(columnLabel: String, x: InputStream): Unit =
    notvalid

  override def absolute(row: Int): Boolean = notvalid

  override def updateRowId(columnIndex: Int, x: RowId): Unit = notvalid

  override def updateRowId(columnLabel: String, x: RowId): Unit = notvalid

  override def getRowId(columnIndex: Int): RowId = notsupported

  override def getRowId(columnLabel: String): RowId = notsupported

  override def moveToInsertRow(): Unit = notvalid

  override def rowInserted(): Boolean = notsupported

  override def getFloat(columnIndex: Int): Float = float(columnIndex)

  override def getFloat(columnLabel: String): Float = float(columnLabel)

  override def getBigDecimal(columnIndex: Int, scale: Int): BigDecimal =
    bigDecimal(columnIndex).setScale(scale)

  override def getBigDecimal(columnLabel: String, scale: Int): BigDecimal =
    bigDecimal(columnLabel).setScale(scale)

  override def getBigDecimal(columnIndex: Int): BigDecimal = bigDecimal(
    columnIndex
  )

  override def getBigDecimal(columnLabel: String): BigDecimal = bigDecimal(
    columnLabel
  )

  override def getClob(columnIndex: Int): Clob = notsupported

  override def getClob(columnLabel: String): Clob = notsupported

  override def getRow: Int = notsupported

  override def getLong(columnIndex: Int): Long = long(columnIndex)

  override def getLong(columnLabel: String): Long = long(columnLabel)

  override def getHoldability: Int = ResultSet.CLOSE_CURSORS_AT_COMMIT

  override def updateFloat(columnIndex: Int, x: Float): Unit = notsupported

  override def updateFloat(columnLabel: String, x: Float): Unit = notsupported

  override def afterLast(): Unit = notvalid

  override def refreshRow(): Unit = notvalid

  override def getNString(columnIndex: Int): String = notsupported

  override def getNString(columnLabel: String): String = notsupported

  override def deleteRow(): Unit = notvalid

  override def getConcurrency: Int = ResultSet.CONCUR_READ_ONLY

  override def updateObject(
    columnIndex: Int,
    x: scala.Any,
    scaleOrLength: Int
  ): Unit = notvalid

  override def updateObject(columnIndex: Int, x: scala.Any): Unit = notvalid

  override def updateObject(
    columnLabel: String,
    x: scala.Any,
    scaleOrLength: Int
  ): Unit = notvalid

  override def updateObject(columnLabel: String, x: scala.Any): Unit = notvalid

  override def getFetchSize: Int = expectedbutnotsupported

  override def getTime(columnIndex: Int): Time = time(columnIndex)

  override def getTime(columnLabel: String): Time = time(columnLabel)

  override def getTime(columnIndex: Int, cal: Calendar): Time = {
    cal.setTimeInMillis(time(columnIndex).getTime)
    new Time(cal.getTimeInMillis)
  }

  override def getTime(columnLabel: String, cal: Calendar): Time = {
    cal.setTimeInMillis(time(columnLabel).getTime)
    new Time(cal.getTimeInMillis)
  }

  override def updateCharacterStream(
    columnIndex: Int,
    x: Reader,
    length: Int
  ): Unit = notvalid

  override def updateCharacterStream(
    columnLabel: String,
    reader: Reader,
    length: Int
  ): Unit = notvalid

  override def updateCharacterStream(
    columnIndex: Int,
    x: Reader,
    length: Long
  ): Unit = notvalid

  override def updateCharacterStream(
    columnLabel: String,
    reader: Reader,
    length: Long
  ): Unit = notvalid

  override def updateCharacterStream(columnIndex: Int, x: Reader): Unit =
    notvalid

  override def updateCharacterStream(
    columnLabel: String,
    reader: Reader
  ): Unit = notvalid

  override def getByte(columnIndex: Int): Byte = byte(columnIndex)

  override def getByte(columnLabel: String): Byte = byte(columnLabel)

  override def getBoolean(columnIndex: Int): Boolean = boolean(columnIndex)

  override def getBoolean(columnLabel: String): Boolean = boolean(columnLabel)

  override def setFetchDirection(direction: Int): Unit = {
    if (direction != ResultSet.FETCH_FORWARD) {
      notvalid
    }
  }

  override def getFetchDirection: Int = ResultSet.FETCH_FORWARD

  override def updateRef(columnIndex: Int, x: Ref): Unit = notvalid

  override def updateRef(columnLabel: String, x: Ref): Unit = notvalid

  override def getAsciiStream(columnIndex: Int): InputStream =
    new ByteArrayInputStream(string(columnIndex).getBytes("UTF8"))

  override def getAsciiStream(columnLabel: String): InputStream =
    new ByteArrayInputStream(string(columnLabel).getBytes("UTF8"))

  override def getShort(columnIndex: Int): Short = short(columnIndex)

  override def getShort(columnLabel: String): Short = short(columnLabel)

  override def getObject(columnIndex: Int): AnyRef = whatIsAny(any(columnIndex))

  private def whatIsAny(any: Any): AnyRef = {
    if (any == null) {
      null
    } else {
      any match {
        case s: Short => short2Short(s)
        case i: Int => int2Integer(i)
        case f: Float => float2Float(f)
        case d: Double => double2Double(d)
        case b: Byte => byte2Byte(b)
        case c: Char => char2Character(c)
        case b: Boolean => boolean2Boolean(b)
        case ref: AnyRef => ref
        case _ =>
          throw new SQLException(
            s"Unknown class for getObject: ${any.getClass}"
          )
      }
    }
  }

  override def getObject(columnLabel: String): AnyRef = whatIsAny(
    any(columnLabel)
  )

  override def getObject(
    columnIndex: Int,
    map: util.Map[String, Class[_]]
  ): AnyRef = notsupported

  override def getObject(
    columnLabel: String,
    map: util.Map[String, Class[_]]
  ): AnyRef = notsupported

  override def getObject[T](columnIndex: Int, `type`: Class[T]): T = {
    val ref = getObject(columnIndex)

    if (ref != null && `type`.isInstance(ref)) {
      ref.asInstanceOf[T]
    } else {
      throw new SQLException(
        s"Object of class ${ref.getClass} is not an instance of ${`type`}"
      )
    }
  }

  override def getObject[T](columnLabel: String, `type`: Class[T]): T = {
    val ref = getObject(columnLabel)

    if (ref != null && `type`.isInstance(ref)) {
      ref.asInstanceOf[T]
    } else {
      throw new SQLException(
        s"Object of class ${ref.getClass} is not an instance of ${`type`}"
      )
    }
  }

  override def updateShort(columnIndex: Int, x: Short): Unit = notvalid

  override def updateShort(columnLabel: String, x: Short): Unit = notvalid

  override def getNCharacterStream(columnIndex: Int): Reader = notsupported

  override def getNCharacterStream(columnLabel: String): Reader = notsupported

  override def close(): Unit = {
    closed = true
  }

  override def relative(rows: Int): Boolean = notvalid

  override def updateInt(columnIndex: Int, x: Int): Unit = notvalid

  override def updateInt(columnLabel: String, x: Int): Unit = notvalid

  override def wasNull(): Boolean = expectedbutnotsupported

  override def rowUpdated(): Boolean = notsupported

  override def getRef(columnIndex: Int): Ref = notsupported

  override def getRef(columnLabel: String): Ref = notsupported

  override def updateLong(columnIndex: Int, x: Long): Unit = notvalid

  override def updateLong(columnLabel: String, x: Long): Unit = notvalid

  override def moveToCurrentRow(): Unit = notvalid

  override def isClosed: Boolean = closed

  override def updateClob(columnIndex: Int, x: Clob): Unit = notvalid

  override def updateClob(columnLabel: String, x: Clob): Unit = notvalid

  override def updateClob(
    columnIndex: Int,
    reader: Reader,
    length: Long
  ): Unit = notvalid

  override def updateClob(
    columnLabel: String,
    reader: Reader,
    length: Long
  ): Unit = notvalid

  override def updateClob(columnIndex: Int, reader: Reader): Unit = notvalid

  override def updateClob(columnLabel: String, reader: Reader): Unit = notvalid

  override def findColumn(columnLabel: String): Int = expectedbutnotsupported

  override def getWarnings: SQLWarning = null

  override def getDate(columnIndex: Int): Date = date(columnIndex)

  override def getDate(columnLabel: String): Date = date(columnLabel)

  override def getDate(columnIndex: Int, cal: Calendar): Date = {
    cal.setTimeInMillis(date(columnIndex).getTime)
    new Date(cal.getTimeInMillis)
  }

  override def getDate(columnLabel: String, cal: Calendar): Date = {
    cal.setTimeInMillis(date(columnLabel).getTime)
    new Date(cal.getTimeInMillis)
  }

  override def getCursorName: String = notsupported

  override def updateNull(columnIndex: Int): Unit = notvalid

  override def updateNull(columnLabel: String): Unit = notvalid

  override def getStatement: Statement = expectedbutnotsupported

  override def cancelRowUpdates(): Unit = notvalid

  override def getSQLXML(columnIndex: Int): SQLXML = notsupported

  override def getSQLXML(columnLabel: String): SQLXML = notsupported

  override def getUnicodeStream(columnIndex: Int): InputStream =
    new ByteArrayInputStream(string(columnIndex).getBytes("UTF8"))

  override def getUnicodeStream(columnLabel: String): InputStream =
    new ByteArrayInputStream(string(columnLabel).getBytes("UTF8"))

  override def getInt(columnIndex: Int): Int = int(columnIndex)

  override def getInt(columnLabel: String): Int = int(columnLabel)

  override def updateTime(columnIndex: Int, x: Time): Unit = notvalid

  override def updateTime(columnLabel: String, x: Time): Unit = notvalid

  override def setFetchSize(rows: Int): Unit = expectedbutnotsupported

  override def previous(): Boolean = notvalid

  override def updateAsciiStream(
    columnIndex: Int,
    x: InputStream,
    length: Int
  ): Unit = notvalid

  override def updateAsciiStream(
    columnLabel: String,
    x: InputStream,
    length: Int
  ): Unit = notvalid

  override def updateAsciiStream(
    columnIndex: Int,
    x: InputStream,
    length: Long
  ): Unit = notvalid

  override def updateAsciiStream(
    columnLabel: String,
    x: InputStream,
    length: Long
  ): Unit = notvalid

  override def updateAsciiStream(columnIndex: Int, x: InputStream): Unit =
    notvalid

  override def updateAsciiStream(columnLabel: String, x: InputStream): Unit =
    notvalid

  override def rowDeleted(): Boolean = notsupported

  override def getBlob(columnIndex: Int): Blob = notsupported

  override def getBlob(columnLabel: String): Blob = notsupported

  override def first(): Boolean = notvalid

  override def getBytes(columnIndex: Int): scala.Array[Byte] = bytes(
    columnIndex
  )

  override def getBytes(columnLabel: String): scala.Array[Byte] = bytes(
    columnLabel
  )

  override def updateBytes(columnIndex: Int, x: scala.Array[Byte]): Unit =
    notvalid

  override def updateBytes(columnLabel: String, x: scala.Array[Byte]): Unit =
    notvalid

  override def updateSQLXML(columnIndex: Int, xmlObject: SQLXML): Unit =
    notvalid

  override def updateSQLXML(columnLabel: String, xmlObject: SQLXML): Unit =
    notvalid

  override def getString(columnIndex: Int): String = string(columnIndex)

  override def getString(columnLabel: String): String = string(columnLabel)

  override def unwrap[T](iface: Class[T]): T = notvalid

  override def isWrapperFor(iface: Class[?]): Boolean = notvalid

  private def any(columnIndex: Int): Any = {
    // To be compatible with JDBC, index should be 1-origin
    // But jasync-sql is 0-origin?
    val index0origin = columnIndex - 1
    currentRow.map(_.get(index0origin)).orNull[Any]
  }

  private def any(columnLabel: String): Any =
    currentRow.map(_.get(columnLabel)).orNull[Any]

  private def anyToBigDecimal(any: Any): java.math.BigDecimal = any match {
    case null => null
    case bd: java.math.BigDecimal => bd
    case bd: scala.BigDecimal => bd.underlying
    case str: String => new java.math.BigDecimal(str)
    case _ => new java.math.BigDecimal(any.toString)
  }
  private def bigDecimal(columnIndex: Int): java.math.BigDecimal =
    anyToBigDecimal(any(columnIndex))
  private def bigDecimal(columnLabel: String): java.math.BigDecimal =
    anyToBigDecimal(any(columnLabel))

  private def anyToBytes(any: Any): scala.Array[Byte] =
    any.asInstanceOf[scala.Array[Byte]]
  private def bytes(columnIndex: Int): scala.Array[Byte] = anyToBytes(
    any(columnIndex)
  )
  private def bytes(columnLabel: String): scala.Array[Byte] = anyToBytes(
    any(columnLabel)
  )

  private def anyToDate(any: Any): java.sql.Date = any match {
    case null => null
    case d: java.sql.Date => d
    case d: java.util.Date => new JavaUtilDateConverter(d).toSqlDate
    case DateConvert(d) =>
      new JavaUtilDateConverter(d).toSqlDate
    case other =>
      throw new UnsupportedOperationException(
        s"Please send a feedback to the library maintainers about supporting ${other.getClass} for #date!"
      )
  }
  private def date(columnIndex: Int): java.sql.Date = anyToDate(
    any(columnIndex)
  )
  private def date(columnLabel: String): java.sql.Date = anyToDate(
    any(columnLabel)
  )

  private def anyToString(any: Any): String = any match {
    case null => null
    case str: String => str
    case _ => any.toString
  }
  private def string(columnIndex: Int): String = anyToString(any(columnIndex))
  private def string(columnLabel: String): String = anyToString(
    any(columnLabel)
  )

  private def anyToTime(any: Any): java.sql.Time = any match {
    case null => null
    case t: java.sql.Time => t
    case DateConvert(d) => new JavaUtilDateConverter(d).toSqlTime
    case d: Duration => new java.sql.Time(d.toMillis)
    case other =>
      throw new UnsupportedOperationException(
        s"Please send a feedback to the library maintainers about supporting ${other.getClass} for #time!"
      )
  }
  private def time(columnIndex: Int): java.sql.Time = anyToTime(
    any(columnIndex)
  )
  private def time(columnLabel: String): java.sql.Time = anyToTime(
    any(columnLabel)
  )

  private def anyToTimestamp(any: Any): java.sql.Timestamp = any match {
    case null => null
    case t: java.sql.Timestamp => t
    case DateConvert(d) => new JavaUtilDateConverter(d).toSqlTimestamp
    case d: Duration =>
      val t = new java.sql.Timestamp(d.toMillis)
      t.setNanos(d.getNano)
      t
    case other =>
      throw new UnsupportedOperationException(
        s"Please send a feedback to the library maintainers about supporting ${other.getClass} for #timestamp!"
      )
  }
  private def timestamp(columnIndex: Int): java.sql.Timestamp = anyToTimestamp(
    any(columnIndex)
  )
  private def timestamp(columnLabel: String): java.sql.Timestamp =
    anyToTimestamp(any(columnLabel))

  private def anyToUrl(any: Any): java.net.URL = any match {
    case null => null
    case url: java.net.URL => url
    case str: String => new java.net.URI(str).toURL
    case _ => new java.net.URI(any.toString).toURL
  }
  private def url(columnIndex: Int): java.net.URL = anyToUrl(any(columnIndex))
  private def url(columnLabel: String): java.net.URL = anyToUrl(
    any(columnLabel)
  )

  private def anyToNullableBoolean(any: Any): java.lang.Boolean = any match {
    case null => null
    case b: java.lang.Boolean => b
    case b: Boolean => b
    case s: String =>
      {
        try s.toInt != 0
        catch { case e: NumberFormatException => !s.isEmpty }
      }.asInstanceOf[java.lang.Boolean]
    case v => (v != 0).asInstanceOf[java.lang.Boolean]
  }
  private def nullableBoolean(columnIndex: Int): java.lang.Boolean =
    anyToNullableBoolean(any(columnIndex))
  private def nullableBoolean(columnLabel: String): java.lang.Boolean =
    anyToNullableBoolean(any(columnLabel))
  private def boolean(columnIndex: Int): Boolean =
    nullableBoolean(columnIndex).asInstanceOf[Boolean]
  private def boolean(columnLabel: String): Boolean = nullableBoolean(
    columnLabel
  ).asInstanceOf[Boolean]

  private def anyToNullableByte(any: Any): java.lang.Byte = any match {
    case null => null
    case _ => java.lang.Byte.valueOf(any.toString)
  }
  private def nullableByte(columnIndex: Int): java.lang.Byte =
    anyToNullableByte(any(columnIndex))
  private def nullableByte(columnLabel: String): java.lang.Byte =
    anyToNullableByte(any(columnLabel))
  private def byte(columnIndex: Int): Byte =
    nullableByte(columnIndex).asInstanceOf[Byte]
  private def byte(columnLabel: String): Byte =
    nullableByte(columnLabel).asInstanceOf[Byte]

  private def anyToNullableDouble(any: Any): java.lang.Double = any match {
    case null => null
    case _ => java.lang.Double.valueOf(any.toString)
  }
  private def nullableDouble(columnIndex: Int): java.lang.Double =
    anyToNullableDouble(any(columnIndex))
  private def nullableDouble(columnLabel: String): java.lang.Double =
    anyToNullableDouble(any(columnLabel))
  private def double(columnIndex: Int): Double =
    nullableDouble(columnIndex).asInstanceOf[Double]
  private def double(columnLabel: String): Double =
    nullableDouble(columnLabel).asInstanceOf[Double]

  private def anyToNullableFloat(any: Any): java.lang.Float = any match {
    case null => null
    case _ => java.lang.Float.valueOf(any.toString)
  }
  private def nullableFloat(columnIndex: Int): java.lang.Float =
    anyToNullableFloat(any(columnIndex))
  private def nullableFloat(columnLabel: String): java.lang.Float =
    anyToNullableFloat(any(columnLabel))
  private def float(columnIndex: Int): Float =
    nullableFloat(columnIndex).asInstanceOf[Float]
  private def float(columnLabel: String): Float =
    nullableFloat(columnLabel).asInstanceOf[Float]

  private def anyToNullableInt(any: Any): java.lang.Integer = any match {
    case null => null
    case v: Float => v.toInt.asInstanceOf[java.lang.Integer]
    case v: Double => v.toInt.asInstanceOf[java.lang.Integer]
    case v => java.lang.Integer.valueOf(v.toString)
  }
  private def nullableInt(columnIndex: Int): java.lang.Integer =
    anyToNullableInt(any(columnIndex))
  private def nullableInt(columnLabel: String): java.lang.Integer =
    anyToNullableInt(any(columnLabel))
  private def int(columnIndex: Int): Int =
    nullableInt(columnIndex).asInstanceOf[Int]
  private def int(columnLabel: String): Int =
    nullableInt(columnLabel).asInstanceOf[Int]

  private def anyToNullableLong(any: Any): java.lang.Long = any match {
    case null => null
    case v: Float => v.toLong.asInstanceOf[java.lang.Long]
    case v: Double => v.toLong.asInstanceOf[java.lang.Long]
    case v => java.lang.Long.valueOf(v.toString)
  }
  private def nullableLong(columnIndex: Int): java.lang.Long =
    anyToNullableLong(any(columnIndex))
  private def nullableLong(columnLabel: String): java.lang.Long =
    anyToNullableLong(any(columnLabel))
  private def long(columnIndex: Int): Long =
    nullableLong(columnIndex).asInstanceOf[Long]
  private def long(columnLabel: String): Long =
    nullableLong(columnLabel).asInstanceOf[Long]

  private def anyToNullableShort(any: Any): java.lang.Short = any match {
    case null => null
    case v: Float => v.toShort.asInstanceOf[java.lang.Short]
    case v: Double => v.toShort.asInstanceOf[java.lang.Short]
    case v => java.lang.Short.valueOf(v.toString)
  }
  private def nullableShort(columnIndex: Int): java.lang.Short =
    anyToNullableShort(any(columnIndex))
  private def nullableShort(columnLabel: String): java.lang.Short =
    anyToNullableShort(any(columnLabel))
  private def short(columnIndex: Int): Short =
    nullableShort(columnIndex).asInstanceOf[Short]
  private def short(columnLabel: String): Short =
    nullableShort(columnLabel).asInstanceOf[Short]

}
