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

import com.github.mauricio.async.db.RowData
import scalikejdbc.WrappedResultSet
import scalikejdbc.async.AsyncResultSet

/**
 * ResultSet implementation
 */
private[scalikejdbc] class MauricioAsyncResultSet(rows: IndexedSeq[RowData])
    extends WrappedResultSet(null, null, 0)
    with AsyncResultSet {

  private[this] val currentRow: Option[RowData] = rows.headOption

  // AsyncResultSet API
  override def next(): Boolean = rows.headOption.isDefined
  override def tail(): AsyncResultSet = new MauricioAsyncResultSet(rows.tail)

  // WrappedResultSet API
  override def any(columnIndex: Int): Any = currentRow.map(_.apply(columnIndex)).orNull[Any]
  override def any(columnLabel: String): Any = currentRow.map(_.apply(columnLabel)).orNull[Any]

}
