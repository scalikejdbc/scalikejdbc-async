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
import scalikejdbc.async.ShortenedNames._

// -------------------
// one-to-one
// -------------------

class AsyncOneToOneSQLToOption[A, B, Z](
  val underlying: OneToOneSQLToOption[A, B, HasExtractor, Z]
) extends AnyVal
  with AsyncSQLToOption[Z] {
  override def future()(implicit
    session: AsyncDBSession,
    cxt: EC = ECGlobal
  ): Future[Option[Z]] = {
    session
      .oneToOneIterable(
        underlying.statement,
        underlying.rawParameters.toSeq: _*
      )(underlying.extractOne)(underlying.extractTo)(underlying.transform)
      .map {
        case Nil => None
        case results if results.size == 1 => results.headOption
        case results => throw new TooManyRowsException(1, results.size)
      }
  }
}

class AsyncOneToOneSQLToIterable[A, B, Z](
  val underlying: OneToOneSQLToIterable[A, B, HasExtractor, Z]
) extends AnyVal
  with AsyncSQLToIterable[Z] {
  override def future()(implicit
    session: AsyncDBSession,
    cxt: EC = ECGlobal
  ): Future[Iterable[Z]] = {
    session.oneToOneIterable(
      underlying.statement,
      underlying.rawParameters.toSeq: _*
    )(underlying.extractOne)(underlying.extractTo)(underlying.transform)
  }
}

class AsyncOneToOneSQLToList[A, B, Z](
  val underlying: OneToOneSQLToList[A, B, HasExtractor, Z]
) extends AnyVal
  with AsyncSQLToList[Z] {
  override def future()(implicit
    session: AsyncDBSession,
    cxt: EC = ECGlobal
  ): Future[List[Z]] = {
    val iterable = session.oneToOneIterable(
      underlying.statement,
      underlying.rawParameters.toSeq: _*
    )(underlying.extractOne)(underlying.extractTo)(underlying.transform)
    iterable.map(_.toList)
  }
}
// -------------------
// one-to-many
// -------------------

class AsyncOneToManySQLToOption[A, B, Z](
  val underlying: OneToManySQLToOption[A, B, HasExtractor, Z]
) extends AnyVal
  with AsyncSQLToOption[Z] {
  override def future()(implicit
    session: AsyncDBSession,
    cxt: EC = ECGlobal
  ): Future[Option[Z]] = {
    session
      .oneToManyIterable(
        underlying.statement,
        underlying.rawParameters.toSeq: _*
      )(underlying.extractOne)(underlying.extractTo)(underlying.transform)
      .map {
        case Nil => None
        case results if results.size == 1 => results.headOption
        case results => throw new TooManyRowsException(1, results.size)
      }
  }
}

class AsyncOneToManySQLToIterable[A, B, Z](
  val underlying: OneToManySQLToIterable[A, B, HasExtractor, Z]
) extends AnyVal
  with AsyncSQLToIterable[Z] {
  override def future()(implicit
    session: AsyncDBSession,
    cxt: EC = ECGlobal
  ): Future[Iterable[Z]] = {
    session.oneToManyIterable(
      underlying.statement,
      underlying.rawParameters.toSeq: _*
    )(underlying.extractOne)(underlying.extractTo)(underlying.transform)
  }
}

class AsyncOneToManySQLToList[A, B, Z](
  val underlying: OneToManySQLToList[A, B, HasExtractor, Z]
) extends AnyVal
  with AsyncSQLToList[Z] {
  override def future()(implicit
    session: AsyncDBSession,
    cxt: EC = ECGlobal
  ): Future[List[Z]] = {
    val iterable = session.oneToManyIterable(
      underlying.statement,
      underlying.rawParameters.toSeq: _*
    )(underlying.extractOne)(underlying.extractTo)(underlying.transform)
    iterable.map(_.toList)
  }
}
