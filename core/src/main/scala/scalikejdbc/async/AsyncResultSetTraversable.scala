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

/**
 * AsyncResultSet Traversable
 */
@deprecated("will be removed. use AsyncResultSetIterator", "0.12.0")
class AsyncResultSetTraversable(var rs: AsyncResultSet) extends Traversable[WrappedResultSet] {

  override def foreach[U](f: WrappedResultSet => U): Unit =
    iterator.foreach(f)

  def iterator: Iterator[WrappedResultSet] = new AsyncResultSetIterator(rs)

}

