package scalikejdbc.async.internal.r2dbc

import io.r2dbc.spi.{ Result, Row }
import scalikejdbc.WrappedResultSet
import scalikejdbc.async.AsyncResultSet
import scalikejdbc.async.internal.RowDataResultSet

class R2DBCAsyncResultSetImpl(rows: IndexedSeq[XPTO])
  extends WrappedResultSet(
    new R2DBCRowResultSet(rows.headOption, rows.drop(1)),
    null,
    0
  )
  with AsyncResultSet {
  override def next(): Boolean = rows.nonEmpty

  override def tail(): AsyncResultSet = new R2DBCAsyncResultSetImpl(rows.tail)
}
