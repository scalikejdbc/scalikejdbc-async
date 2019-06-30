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

class AsyncOneToOneSQLToOption[A, B, Z](val underlying: OneToOneSQLToOption[A, B, HasExtractor, Z])
  extends AnyVal
  with AsyncSQLToOption[Z] {
  override def future()(implicit session: AsyncDBSession, cxt: EC = ECGlobal): Future[Option[Z]] = {
    session.oneToOneIterable(underlying.statement, underlying.rawParameters: _*)(underlying.extractOne)(underlying.extractTo)(underlying.transform).map {
      results =>
        results match {
          case Nil => None
          case _ if results.size == 1 => results.headOption
          case _ => throw new TooManyRowsException(1, results.size)
        }
    }
  }
}

class AsyncOneToOneSQLToIterable[A, B, Z](val underlying: OneToOneSQLToIterable[A, B, HasExtractor, Z])
  extends AnyVal
  with AsyncSQLToIterable[Z] {
  override def future()(implicit
    session: AsyncDBSession,
    cxt: EC = ECGlobal): Future[Iterable[Z]] = {
    val iterable = session.oneToOneIterable(underlying.statement, underlying.rawParameters: _*)(underlying.extractOne)(underlying.extractTo)(underlying.transform)
    iterable.asInstanceOf[Future[Iterable[Z]]]
  }
}

class AsyncOneToOneSQLToList[A, B, Z](val underlying: OneToOneSQLToList[A, B, HasExtractor, Z])
  extends AnyVal
  with AsyncSQLToList[Z] {
  override def future()(implicit session: AsyncDBSession, cxt: EC = ECGlobal): Future[List[Z]] = {
    val iterable = session.oneToOneIterable(underlying.statement, underlying.rawParameters: _*)(underlying.extractOne)(underlying.extractTo)(underlying.transform)
    iterable.map(_.toList)
  }
}
// -------------------
// one-to-many
// -------------------

class AsyncOneToManySQLToOption[A, B, Z](val underlying: OneToManySQLToOption[A, B, HasExtractor, Z])
  extends AnyVal
  with AsyncSQLToOption[Z] {
  override def future()(implicit session: AsyncDBSession, cxt: EC = ECGlobal): Future[Option[Z]] = {
    session.oneToManyIterable(underlying.statement, underlying.rawParameters: _*)(underlying.extractOne)(underlying.extractTo)(underlying.transform).map {
      results =>
        results match {
          case Nil => None
          case _ if results.size == 1 => results.headOption
          case _ => throw new TooManyRowsException(1, results.size)
        }
    }
  }
}

class AsyncOneToManySQLToIterable[A, B, Z](val underlying: OneToManySQLToIterable[A, B, HasExtractor, Z])
  extends AnyVal
  with AsyncSQLToIterable[Z] {
  override def future()(implicit
    session: AsyncDBSession,
    cxt: EC = ECGlobal): Future[Iterable[Z]] = {
    session.oneToManyIterable(underlying.statement, underlying.rawParameters: _*)(underlying.extractOne)(underlying.extractTo)(underlying.transform)
  }
}

class AsyncOneToManySQLToList[A, B, Z](val underlying: OneToManySQLToList[A, B, HasExtractor, Z])
  extends AnyVal
  with AsyncSQLToList[Z] {
  override def future()(implicit session: AsyncDBSession, cxt: EC = ECGlobal): Future[List[Z]] = {
    val iterable = session.oneToManyIterable(underlying.statement, underlying.rawParameters: _*)(underlying.extractOne)(underlying.extractTo)(underlying.transform)
    iterable.map(_.toList)
  }
}

// -------------------
// one-to-manies 2
// -------------------

class AsyncOneToManies2SQLToOption[A, B1, B2, Z](val underlying: OneToManies2SQLToOption[A, B1, B2, HasExtractor, Z])
  extends AnyVal
  with AsyncSQLToOption[Z] {
  override def future()(implicit session: AsyncDBSession, cxt: EC = ECGlobal): Future[Option[Z]] = {
    session.oneToManies2Iterable(underlying.statement, underlying.rawParameters: _*)(underlying.extractOne)(underlying.extractTo1, underlying.extractTo2)(underlying.transform).map {
      results =>
        results match {
          case Nil => None
          case _ if results.size == 1 => results.headOption
          case _ => throw new TooManyRowsException(1, results.size)
        }
    }
  }
}

class AsyncOneToManies2SQLToIterable[A, B1, B2, Z](val underlying: OneToManies2SQLToIterable[A, B1, B2, HasExtractor, Z])
  extends AnyVal
  with AsyncSQLToIterable[Z] {
  override def future()(implicit session: AsyncDBSession, cxt: EC = ECGlobal): Future[Iterable[Z]] = {
    val iterable = session.oneToManies2Iterable(underlying.statement, underlying.rawParameters: _*)(underlying.extractOne)(underlying.extractTo1, underlying.extractTo2)(underlying.transform)
    iterable
  }
}

class AsyncOneToManies2SQLToList[A, B1, B2, Z](val underlying: OneToManies2SQLToList[A, B1, B2, HasExtractor, Z])
  extends AnyVal
  with AsyncSQLToList[Z] {
  override def future()(implicit session: AsyncDBSession, cxt: EC = ECGlobal): Future[List[Z]] = {
    val iterable = session.oneToManies2Iterable(underlying.statement, underlying.rawParameters: _*)(underlying.extractOne)(underlying.extractTo1, underlying.extractTo2)(underlying.transform)
    iterable.map(_.toList)
  }
}

// -------------------
// one-to-manies 3
// -------------------

class AsyncOneToManies3SQLToOption[A, B1, B2, B3, Z](val underlying: OneToManies3SQLToOption[A, B1, B2, B3, HasExtractor, Z])
  extends AnyVal
  with AsyncSQLToOption[Z] {
  override def future()(implicit session: AsyncDBSession, cxt: EC = ECGlobal): Future[Option[Z]] = {
    session.oneToManies3Iterable(underlying.statement, underlying.rawParameters: _*)(underlying.extractOne)(
      underlying.extractTo1, underlying.extractTo2, underlying.extractTo3)(underlying.transform).map {
        results =>
          results match {
            case Nil => None
            case _ if results.size == 1 => results.headOption
            case _ => throw new TooManyRowsException(1, results.size)
          }
      }
  }
}

class AsyncOneToManies3SQLToIterable[A, B1, B2, B3, Z](val underlying: OneToManies3SQLToIterable[A, B1, B2, B3, HasExtractor, Z])
  extends AnyVal
  with AsyncSQLToIterable[Z] {
  override def future()(implicit session: AsyncDBSession, cxt: EC = ECGlobal): Future[Iterable[Z]] = {
    session.oneToManies3Iterable(underlying.statement, underlying.rawParameters: _*)(underlying.extractOne)(
      underlying.extractTo1, underlying.extractTo2, underlying.extractTo3)(underlying.transform)
  }
}

class AsyncOneToManies3SQLToList[A, B1, B2, B3, Z](val underlying: OneToManies3SQLToList[A, B1, B2, B3, HasExtractor, Z])
  extends AnyVal
  with AsyncSQLToList[Z] {
  override def future()(implicit session: AsyncDBSession, cxt: EC = ECGlobal): Future[List[Z]] = {
    val iterable = session.oneToManies3Iterable(underlying.statement, underlying.rawParameters: _*)(underlying.extractOne)(
      underlying.extractTo1, underlying.extractTo2, underlying.extractTo3)(underlying.transform)
    iterable.map(_.toList)
  }
}

// -------------------
// one-to-manies 4
// -------------------

class AsyncOneToManies4SQLToOption[A, B1, B2, B3, B4, Z](val underlying: OneToManies4SQLToOption[A, B1, B2, B3, B4, HasExtractor, Z])
  extends AnyVal
  with AsyncSQLToOption[Z] {
  override def future()(implicit session: AsyncDBSession, cxt: EC = ECGlobal): Future[Option[Z]] = {
    session.oneToManies4Iterable(underlying.statement, underlying.rawParameters: _*)(underlying.extractOne)(
      underlying.extractTo1, underlying.extractTo2, underlying.extractTo3, underlying.extractTo4)(underlying.transform).map {
        results =>
          results match {
            case Nil => None
            case _ if results.size == 1 => results.headOption
            case _ => throw new TooManyRowsException(1, results.size)
          }
      }
  }
}

class AsyncOneToManies4SQLToIterable[A, B1, B2, B3, B4, Z](val underlying: OneToManies4SQLToIterable[A, B1, B2, B3, B4, HasExtractor, Z])
  extends AnyVal
  with AsyncSQLToIterable[Z] {
  override def future()(implicit session: AsyncDBSession, cxt: EC = ECGlobal): Future[Iterable[Z]] = {
    session.oneToManies4Iterable(underlying.statement, underlying.rawParameters: _*)(underlying.extractOne)(
      underlying.extractTo1, underlying.extractTo2, underlying.extractTo3, underlying.extractTo4)(underlying.transform)
  }
}

class AsyncOneToManies4SQLToList[A, B1, B2, B3, B4, Z](val underlying: OneToManies4SQLToList[A, B1, B2, B3, B4, HasExtractor, Z])
  extends AnyVal
  with AsyncSQLToList[Z] {
  override def future()(implicit session: AsyncDBSession, cxt: EC = ECGlobal): Future[List[Z]] = {
    val iterable = session.oneToManies4Iterable(underlying.statement, underlying.rawParameters: _*)(underlying.extractOne)(
      underlying.extractTo1, underlying.extractTo2, underlying.extractTo3, underlying.extractTo4)(underlying.transform)
    iterable.map(_.toList)
  }
}

// -------------------
// one-to-manies 5
// -------------------

class AsyncOneToManies5SQLToOption[A, B1, B2, B3, B4, B5, Z](val underlying: OneToManies5SQLToOption[A, B1, B2, B3, B4, B5, HasExtractor, Z])
  extends AnyVal
  with AsyncSQLToOption[Z] {
  override def future()(implicit session: AsyncDBSession, cxt: EC = ECGlobal): Future[Option[Z]] = {
    session.oneToManies5Iterable(underlying.statement, underlying.rawParameters: _*)(underlying.extractOne)(
      underlying.extractTo1, underlying.extractTo2, underlying.extractTo3, underlying.extractTo4, underlying.extractTo5)(underlying.transform).map {
        results =>
          results match {
            case Nil => None
            case _ if results.size == 1 => results.headOption
            case _ => throw new TooManyRowsException(1, results.size)
          }
      }
  }
}

class AsyncOneToManies5SQLToIterable[A, B1, B2, B3, B4, B5, Z](val underlying: OneToManies5SQLToIterable[A, B1, B2, B3, B4, B5, HasExtractor, Z])
  extends AnyVal
  with AsyncSQLToIterable[Z] {
  override def future()(implicit session: AsyncDBSession, cxt: EC = ECGlobal): Future[Iterable[Z]] = {
    session.oneToManies5Iterable(underlying.statement, underlying.rawParameters: _*)(underlying.extractOne)(
      underlying.extractTo1, underlying.extractTo2, underlying.extractTo3, underlying.extractTo4, underlying.extractTo5)(underlying.transform)
  }
}

class AsyncOneToManies5SQLToList[A, B1, B2, B3, B4, B5, Z](val underlying: OneToManies5SQLToList[A, B1, B2, B3, B4, B5, HasExtractor, Z])
  extends AnyVal
  with AsyncSQLToList[Z] {
  override def future()(implicit session: AsyncDBSession, cxt: EC = ECGlobal): Future[List[Z]] = {
    val iterable = session.oneToManies5Iterable(underlying.statement, underlying.rawParameters: _*)(underlying.extractOne)(
      underlying.extractTo1, underlying.extractTo2, underlying.extractTo3, underlying.extractTo4, underlying.extractTo5)(underlying.transform)
    iterable.map(_.toList)
  }
}

// -------------------
// one-to-manies 6
// -------------------

class AsyncOneToManies6SQLToOption[A, B1, B2, B3, B4, B5, B6, Z](val underlying: OneToManies6SQLToOption[A, B1, B2, B3, B4, B5, B6, HasExtractor, Z])
  extends AnyVal
  with AsyncSQLToOption[Z] {
  override def future()(implicit session: AsyncDBSession, cxt: EC = ECGlobal): Future[Option[Z]] = {
    session.oneToManies6Iterable(underlying.statement, underlying.rawParameters: _*)(underlying.extractOne)(
      underlying.extractTo1, underlying.extractTo2, underlying.extractTo3, underlying.extractTo4, underlying.extractTo5, underlying.extractTo6)(underlying.transform).map {
        results =>
          results match {
            case Nil => None
            case _ if results.size == 1 => results.headOption
            case _ => throw new TooManyRowsException(1, results.size)
          }
      }
  }
}

class AsyncOneToManies6SQLToIterable[A, B1, B2, B3, B4, B5, B6, Z](val underlying: OneToManies6SQLToIterable[A, B1, B2, B3, B4, B5, B6, HasExtractor, Z])
  extends AnyVal
  with AsyncSQLToIterable[Z] {
  override def future()(implicit session: AsyncDBSession, cxt: EC = ECGlobal): Future[Iterable[Z]] = {
    session.oneToManies6Iterable(underlying.statement, underlying.rawParameters: _*)(underlying.extractOne)(
      underlying.extractTo1, underlying.extractTo2, underlying.extractTo3, underlying.extractTo4, underlying.extractTo5, underlying.extractTo6)(underlying.transform)
  }
}

class AsyncOneToManies6SQLToList[A, B1, B2, B3, B4, B5, B6, Z](val underlying: OneToManies6SQLToList[A, B1, B2, B3, B4, B5, B6, HasExtractor, Z])
  extends AnyVal
  with AsyncSQLToList[Z] {
  override def future()(implicit session: AsyncDBSession, cxt: EC = ECGlobal): Future[List[Z]] = {
    val iterable = session.oneToManies6Iterable(underlying.statement, underlying.rawParameters: _*)(underlying.extractOne)(
      underlying.extractTo1, underlying.extractTo2, underlying.extractTo3, underlying.extractTo4, underlying.extractTo5, underlying.extractTo6)(underlying.transform)
    iterable.map(_.toList)
  }
}

// -------------------
// one-to-manies 7
// -------------------

class AsyncOneToManies7SQLToOption[A, B1, B2, B3, B4, B5, B6, B7, Z](val underlying: OneToManies7SQLToOption[A, B1, B2, B3, B4, B5, B6, B7, HasExtractor, Z])
  extends AnyVal
  with AsyncSQLToOption[Z] {
  override def future()(implicit session: AsyncDBSession, cxt: EC = ECGlobal): Future[Option[Z]] = {
    session.oneToManies7Iterable(underlying.statement, underlying.rawParameters: _*)(underlying.extractOne)(
      underlying.extractTo1, underlying.extractTo2, underlying.extractTo3, underlying.extractTo4, underlying.extractTo5, underlying.extractTo6, underlying.extractTo7)(underlying.transform).map {
        results =>
          results match {
            case Nil => None
            case _ if results.size == 1 => results.headOption
            case _ => throw new TooManyRowsException(1, results.size)
          }
      }
  }
}

class AsyncOneToManies7SQLToIterable[A, B1, B2, B3, B4, B5, B6, B7, Z](val underlying: OneToManies7SQLToIterable[A, B1, B2, B3, B4, B5, B6, B7, HasExtractor, Z])
  extends AnyVal
  with AsyncSQLToIterable[Z] {
  override def future()(implicit session: AsyncDBSession, cxt: EC = ECGlobal): Future[Iterable[Z]] = {
    session.oneToManies7Iterable(underlying.statement, underlying.rawParameters: _*)(underlying.extractOne)(
      underlying.extractTo1, underlying.extractTo2, underlying.extractTo3, underlying.extractTo4, underlying.extractTo5, underlying.extractTo6, underlying.extractTo7)(underlying.transform)
  }
}

class AsyncOneToManies7SQLToList[A, B1, B2, B3, B4, B5, B6, B7, Z](val underlying: OneToManies7SQLToList[A, B1, B2, B3, B4, B5, B6, B7, HasExtractor, Z])
  extends AnyVal
  with AsyncSQLToList[Z] {
  override def future()(implicit session: AsyncDBSession, cxt: EC = ECGlobal): Future[List[Z]] = {
    val iterable = session.oneToManies7Iterable(underlying.statement, underlying.rawParameters: _*)(underlying.extractOne)(
      underlying.extractTo1, underlying.extractTo2, underlying.extractTo3, underlying.extractTo4, underlying.extractTo5, underlying.extractTo6, underlying.extractTo7)(underlying.transform)
    iterable.map(_.toList)
  }
}

// -------------------
// one-to-manies 8
// -------------------

class AsyncOneToManies8SQLToOption[A, B1, B2, B3, B4, B5, B6, B7, B8, Z](val underlying: OneToManies8SQLToOption[A, B1, B2, B3, B4, B5, B6, B7, B8, HasExtractor, Z])
  extends AnyVal
  with AsyncSQLToOption[Z] {
  override def future()(implicit session: AsyncDBSession, cxt: EC = ECGlobal): Future[Option[Z]] = {
    session.oneToManies8Iterable(underlying.statement, underlying.rawParameters: _*)(underlying.extractOne)(
      underlying.extractTo1, underlying.extractTo2, underlying.extractTo3, underlying.extractTo4, underlying.extractTo5, underlying.extractTo6, underlying.extractTo7, underlying.extractTo8)(underlying.transform).map {
        results =>
          results match {
            case Nil => None
            case _ if results.size == 1 => results.headOption
            case _ => throw new TooManyRowsException(1, results.size)
          }
      }
  }
}

class AsyncOneToManies8SQLToIterable[A, B1, B2, B3, B4, B5, B6, B7, B8, Z](val underlying: OneToManies8SQLToIterable[A, B1, B2, B3, B4, B5, B6, B7, B8, HasExtractor, Z])
  extends AnyVal
  with AsyncSQLToIterable[Z] {
  override def future()(implicit session: AsyncDBSession, cxt: EC = ECGlobal): Future[Iterable[Z]] = {
    session.oneToManies8Iterable(underlying.statement, underlying.rawParameters: _*)(underlying.extractOne)(
      underlying.extractTo1, underlying.extractTo2, underlying.extractTo3, underlying.extractTo4, underlying.extractTo5, underlying.extractTo6, underlying.extractTo7, underlying.extractTo8)(underlying.transform)
  }
}

class AsyncOneToManies8SQLToList[A, B1, B2, B3, B4, B5, B6, B7, B8, Z](val underlying: OneToManies8SQLToList[A, B1, B2, B3, B4, B5, B6, B7, B8, HasExtractor, Z])
  extends AnyVal
  with AsyncSQLToList[Z] {
  override def future()(implicit session: AsyncDBSession, cxt: EC = ECGlobal): Future[List[Z]] = {
    val iterable = session.oneToManies8Iterable(underlying.statement, underlying.rawParameters: _*)(underlying.extractOne)(
      underlying.extractTo1, underlying.extractTo2, underlying.extractTo3, underlying.extractTo4, underlying.extractTo5, underlying.extractTo6, underlying.extractTo7, underlying.extractTo8)(underlying.transform)
    iterable.map(_.toList)
  }
}

// -------------------
// one-to-manies 9
// -------------------

class AsyncOneToManies9SQLToOption[A, B1, B2, B3, B4, B5, B6, B7, B8, B9, Z](val underlying: OneToManies9SQLToOption[A, B1, B2, B3, B4, B5, B6, B7, B8, B9, HasExtractor, Z])
  extends AnyVal
  with AsyncSQLToOption[Z] {
  override def future()(implicit session: AsyncDBSession, cxt: EC = ECGlobal): Future[Option[Z]] = {
    session.oneToManies9Iterable(underlying.statement, underlying.rawParameters: _*)(underlying.extractOne)(
      underlying.extractTo1, underlying.extractTo2, underlying.extractTo3, underlying.extractTo4, underlying.extractTo5, underlying.extractTo6, underlying.extractTo7, underlying.extractTo8, underlying.extractTo9)(underlying.transform).map {
        results =>
          results match {
            case Nil => None
            case _ if results.size == 1 => results.headOption
            case _ => throw new TooManyRowsException(1, results.size)
          }
      }
  }
}

class AsyncOneToManies9SQLToIterable[A, B1, B2, B3, B4, B5, B6, B7, B8, B9, Z](val underlying: OneToManies9SQLToIterable[A, B1, B2, B3, B4, B5, B6, B7, B8, B9, HasExtractor, Z])
  extends AnyVal
  with AsyncSQLToIterable[Z] {
  override def future()(implicit session: AsyncDBSession, cxt: EC = ECGlobal): Future[Iterable[Z]] = {
    session.oneToManies9Iterable(underlying.statement, underlying.rawParameters: _*)(underlying.extractOne)(
      underlying.extractTo1, underlying.extractTo2, underlying.extractTo3, underlying.extractTo4, underlying.extractTo5, underlying.extractTo6, underlying.extractTo7, underlying.extractTo8, underlying.extractTo9)(underlying.transform)
  }
}

class AsyncOneToManies9SQLToList[A, B1, B2, B3, B4, B5, B6, B7, B8, B9, Z](val underlying: OneToManies9SQLToList[A, B1, B2, B3, B4, B5, B6, B7, B8, B9, HasExtractor, Z])
  extends AnyVal
  with AsyncSQLToList[Z] {
  override def future()(implicit session: AsyncDBSession, cxt: EC = ECGlobal): Future[List[Z]] = {
    val iterable = session.oneToManies9Iterable(underlying.statement, underlying.rawParameters: _*)(underlying.extractOne)(
      underlying.extractTo1, underlying.extractTo2, underlying.extractTo3, underlying.extractTo4, underlying.extractTo5, underlying.extractTo6, underlying.extractTo7, underlying.extractTo8, underlying.extractTo9)(underlying.transform)
    iterable.map(_.toList)
  }
}
