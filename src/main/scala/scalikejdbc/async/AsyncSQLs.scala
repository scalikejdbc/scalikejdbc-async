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
import scala.concurrent._
import ShortenedNames._

trait AsyncSQLExecution extends Any {
  val underlying: SQLExecution
  def future()(implicit session: AsyncDBSession, cxt: EC = ECGlobal): Future[Boolean] = {
    session.execute(underlying.statement, underlying.parameters: _*)
  }
}
class AsyncSQLExecutionImpl(val underlying: SQLExecution) extends AnyVal with AsyncSQLExecution

trait AsyncSQLUpdate extends Any {
  val underlying: SQLUpdate
  def future()(implicit session: AsyncDBSession, cxt: EC = ECGlobal): Future[Int] = {
    session.update(underlying.statement, underlying.parameters: _*)
  }
}
class AsyncSQLUpdateImpl(val underlying: SQLUpdate) extends AnyVal with AsyncSQLUpdate

trait AsyncSQLUpdateAndReturnGeneratedKey extends Any {
  val underlying: SQLUpdateWithGeneratedKey
  def future()(implicit session: AsyncDBSession,
    cxt: EC = ECGlobal): Future[Long] = {
    session.updateAndReturnGeneratedKey(underlying.statement, underlying.parameters: _*)
  }
}
class AsyncSQLUpdateAndReturnGeneratedKeyImpl(val underlying: SQLUpdateWithGeneratedKey) extends AnyVal with AsyncSQLUpdateAndReturnGeneratedKey

trait AsyncSQLToOption[A] extends Any {
  val underlying: SQLToOption[A, HasExtractor]
  def future()(implicit session: AsyncDBSession, cxt: EC = ECGlobal): Future[Option[A]] = {
    session.single(underlying.statement, underlying.parameters: _*)(underlying.extractor)
  }
}
class AsyncSQLToOptionImpl[A](val underlying: SQLToOption[A, HasExtractor]) extends AnyVal with AsyncSQLToOption[A]

trait AsyncSQLToTraversable[A] extends Any {
  val underlying: SQLToTraversable[A, HasExtractor]
  def future()(implicit session: AsyncDBSession, cxt: EC = ECGlobal): Future[Traversable[A]] = {
    session.traversable(underlying.statement, underlying.parameters: _*)(underlying.extractor)
  }
}
class AsyncSQLToTraversableImpl[A](val underlying: SQLToTraversable[A, HasExtractor]) extends AnyVal with AsyncSQLToTraversable[A]

trait AsyncSQLToList[A] extends Any {
  val underlying: SQLToList[A, HasExtractor]
  def future()(implicit session: AsyncDBSession, cxt: EC = ECGlobal): Future[List[A]] = {
    session.list(underlying.statement, underlying.parameters: _*)(underlying.extractor)
  }
}
class AsyncSQLToListImpl[A](val underlying: SQLToList[A, HasExtractor]) extends AnyVal with AsyncSQLToList[A]

