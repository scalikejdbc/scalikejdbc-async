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
  def underlying: SQLExecution
  def future()(implicit session: AsyncDBSession, cxt: EC = ECGlobal): Future[Boolean] = {
    session.execute(underlying.statement, underlying.parameters.toSeq: _*)
  }
}
class AsyncSQLExecutionImpl(val underlying: SQLExecution) extends AnyVal with AsyncSQLExecution

trait AsyncSQLUpdate extends Any {
  def underlying: SQLUpdate
  def future()(implicit session: AsyncDBSession, cxt: EC = ECGlobal): Future[Int] = {
    session.update(underlying.statement, underlying.parameters.toSeq: _*)
  }
}
class AsyncSQLUpdateImpl(val underlying: SQLUpdate) extends AnyVal with AsyncSQLUpdate

trait AsyncSQLUpdateAndReturnGeneratedKey extends Any {
  def underlying: SQLUpdateWithGeneratedKey
  def future()(implicit
    session: AsyncDBSession,
    cxt: EC = ECGlobal): Future[Long] = {
    session.updateAndReturnGeneratedKey(underlying.statement, underlying.parameters.toSeq: _*)
  }
}
class AsyncSQLUpdateAndReturnGeneratedKeyImpl(val underlying: SQLUpdateWithGeneratedKey) extends AnyVal with AsyncSQLUpdateAndReturnGeneratedKey

trait AsyncSQLToOption[A] extends Any {
  def underlying: SQLToOption[A, HasExtractor]
  def future()(implicit session: AsyncDBSession, cxt: EC = ECGlobal): Future[Option[A]] = {
    session.single(underlying.statement, underlying.rawParameters.toSeq: _*)(underlying.extractor)
  }
}
class AsyncSQLToOptionImpl[A](val underlying: SQLToOption[A, HasExtractor]) extends AnyVal with AsyncSQLToOption[A]

trait AsyncSQLToIterable[A] extends Any {
  def underlying: SQLToIterable[A, HasExtractor]
  def future()(implicit session: AsyncDBSession, cxt: EC = ECGlobal): Future[Iterable[A]] = {
    session.iterable(underlying.statement, underlying.rawParameters.toSeq: _*)(underlying.extractor)
  }
}
class AsyncSQLToIterableImpl[A](val underlying: SQLToIterable[A, HasExtractor]) extends AnyVal with AsyncSQLToIterable[A]

trait AsyncSQLToList[A] extends Any {
  def underlying: SQLToList[A, HasExtractor]
  def future()(implicit session: AsyncDBSession, cxt: EC = ECGlobal): Future[List[A]] = {
    session.list(underlying.statement, underlying.rawParameters.toSeq: _*)(underlying.extractor)
  }
}
class AsyncSQLToListImpl[A](val underlying: SQLToList[A, HasExtractor]) extends AnyVal with AsyncSQLToList[A]

