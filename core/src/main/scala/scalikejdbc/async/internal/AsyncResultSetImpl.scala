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
import com.github.jasync.sql.db.RowData
import scalikejdbc.WrappedResultSet
import scalikejdbc.async.AsyncResultSet
import org.joda.time.{ LocalDateTime, LocalTime, LocalDate, DateTime }

/**
 * ResultSet Implementation
 */
private[scalikejdbc] class AsyncResultSetImpl(rows: IndexedSeq[RowData])
  extends WrappedResultSet(new RowDataResultSet(rows.headOption, rows.drop(1)), null, 0)
  with AsyncResultSet {

  // AsyncResultSet API
  override def next(): Boolean = rows.nonEmpty
  override def tail(): AsyncResultSet = new AsyncResultSetImpl(rows.tail)

}
