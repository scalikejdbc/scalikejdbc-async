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
    session.oneToOneIterable(underlying.statement, underlying.rawParameters.toSeq: _*)(underlying.extractOne)(underlying.extractTo)(underlying.transform).map {
      case Nil => None
      case results if results.size == 1 => results.headOption
      case results => throw new TooManyRowsException(1, results.size)
    }
  }
}

class AsyncOneToOneSQLToIterable[A, B, Z](val underlying: OneToOneSQLToIterable[A, B, HasExtractor, Z])
  extends AnyVal
  with AsyncSQLToIterable[Z] {
  override def future()(implicit
    session: AsyncDBSession,
    cxt: EC = ECGlobal): Future[Iterable[Z]] = {
    val iterable = session.oneToOneIterable(underlying.statement, underlying.rawParameters.toSeq: _*)(underlying.extractOne)(underlying.extractTo)(underlying.transform)
    iterable.asInstanceOf[Future[Iterable[Z]]]
  }
}

class AsyncOneToOneSQLToList[A, B, Z](val underlying: OneToOneSQLToList[A, B, HasExtractor, Z])
  extends AnyVal
  with AsyncSQLToList[Z] {
  override def future()(implicit session: AsyncDBSession, cxt: EC = ECGlobal): Future[List[Z]] = {
    val iterable = session.oneToOneIterable(underlying.statement, underlying.rawParameters.toSeq: _*)(underlying.extractOne)(underlying.extractTo)(underlying.transform)
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
    session.oneToManyIterable(underlying.statement, underlying.rawParameters.toSeq: _*)(underlying.extractOne)(underlying.extractTo)(underlying.transform).map {
      case Nil => None
      case results if results.size == 1 => results.headOption
      case results => throw new TooManyRowsException(1, results.size)
    }
  }
}

class AsyncOneToManySQLToIterable[A, B, Z](val underlying: OneToManySQLToIterable[A, B, HasExtractor, Z])
  extends AnyVal
  with AsyncSQLToIterable[Z] {
  override def future()(implicit
    session: AsyncDBSession,
    cxt: EC = ECGlobal): Future[Iterable[Z]] = {
    session.oneToManyIterable(underlying.statement, underlying.rawParameters.toSeq: _*)(underlying.extractOne)(underlying.extractTo)(underlying.transform)
  }
}

class AsyncOneToManySQLToList[A, B, Z](val underlying: OneToManySQLToList[A, B, HasExtractor, Z])
  extends AnyVal
  with AsyncSQLToList[Z] {
  override def future()(implicit session: AsyncDBSession, cxt: EC = ECGlobal): Future[List[Z]] = {
    val iterable = session.oneToManyIterable(underlying.statement, underlying.rawParameters.toSeq: _*)(underlying.extractOne)(underlying.extractTo)(underlying.transform)
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
    session.oneToManies2Iterable(underlying.statement, underlying.rawParameters.toSeq: _*)(underlying.extractOne)(underlying.extractTo1, underlying.extractTo2)(underlying.transform).map {
      case Nil => None
      case results if results.size == 1 => results.headOption
      case results => throw new TooManyRowsException(1, results.size)
    }
  }
}

class AsyncOneToManies2SQLToIterable[A, B1, B2, Z](val underlying: OneToManies2SQLToIterable[A, B1, B2, HasExtractor, Z])
  extends AnyVal
  with AsyncSQLToIterable[Z] {
  override def future()(implicit session: AsyncDBSession, cxt: EC = ECGlobal): Future[Iterable[Z]] = {
    val iterable = session.oneToManies2Iterable(underlying.statement, underlying.rawParameters.toSeq: _*)(underlying.extractOne)(underlying.extractTo1, underlying.extractTo2)(underlying.transform)
    iterable
  }
}

class AsyncOneToManies2SQLToList[A, B1, B2, Z](val underlying: OneToManies2SQLToList[A, B1, B2, HasExtractor, Z])
  extends AnyVal
  with AsyncSQLToList[Z] {
  override def future()(implicit session: AsyncDBSession, cxt: EC = ECGlobal): Future[List[Z]] = {
    val iterable = session.oneToManies2Iterable(underlying.statement, underlying.rawParameters.toSeq: _*)(underlying.extractOne)(underlying.extractTo1, underlying.extractTo2)(underlying.transform)
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
    session.oneToManies3Iterable(underlying.statement, underlying.rawParameters.toSeq: _*)(underlying.extractOne)(
      underlying.extractTo1, underlying.extractTo2, underlying.extractTo3)(underlying.transform).map {
        case Nil => None
        case results if results.size == 1 => results.headOption
        case results => throw new TooManyRowsException(1, results.size)
      }
  }
}

class AsyncOneToManies3SQLToIterable[A, B1, B2, B3, Z](val underlying: OneToManies3SQLToIterable[A, B1, B2, B3, HasExtractor, Z])
  extends AnyVal
  with AsyncSQLToIterable[Z] {
  override def future()(implicit session: AsyncDBSession, cxt: EC = ECGlobal): Future[Iterable[Z]] = {
    session.oneToManies3Iterable(underlying.statement, underlying.rawParameters.toSeq: _*)(underlying.extractOne)(
      underlying.extractTo1, underlying.extractTo2, underlying.extractTo3)(underlying.transform)
  }
}

class AsyncOneToManies3SQLToList[A, B1, B2, B3, Z](val underlying: OneToManies3SQLToList[A, B1, B2, B3, HasExtractor, Z])
  extends AnyVal
  with AsyncSQLToList[Z] {
  override def future()(implicit session: AsyncDBSession, cxt: EC = ECGlobal): Future[List[Z]] = {
    val iterable = session.oneToManies3Iterable(underlying.statement, underlying.rawParameters.toSeq: _*)(underlying.extractOne)(
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
    session.oneToManies4Iterable(underlying.statement, underlying.rawParameters.toSeq: _*)(underlying.extractOne)(
      underlying.extractTo1, underlying.extractTo2, underlying.extractTo3, underlying.extractTo4)(underlying.transform).map {
        case Nil => None
        case results if results.size == 1 => results.headOption
        case results => throw new TooManyRowsException(1, results.size)
      }
  }
}

class AsyncOneToManies4SQLToIterable[A, B1, B2, B3, B4, Z](val underlying: OneToManies4SQLToIterable[A, B1, B2, B3, B4, HasExtractor, Z])
  extends AnyVal
  with AsyncSQLToIterable[Z] {
  override def future()(implicit session: AsyncDBSession, cxt: EC = ECGlobal): Future[Iterable[Z]] = {
    session.oneToManies4Iterable(underlying.statement, underlying.rawParameters.toSeq: _*)(underlying.extractOne)(
      underlying.extractTo1, underlying.extractTo2, underlying.extractTo3, underlying.extractTo4)(underlying.transform)
  }
}

class AsyncOneToManies4SQLToList[A, B1, B2, B3, B4, Z](val underlying: OneToManies4SQLToList[A, B1, B2, B3, B4, HasExtractor, Z])
  extends AnyVal
  with AsyncSQLToList[Z] {
  override def future()(implicit session: AsyncDBSession, cxt: EC = ECGlobal): Future[List[Z]] = {
    val iterable = session.oneToManies4Iterable(underlying.statement, underlying.rawParameters.toSeq: _*)(underlying.extractOne)(
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
    session.oneToManies5Iterable(underlying.statement, underlying.rawParameters.toSeq: _*)(underlying.extractOne)(
      underlying.extractTo1, underlying.extractTo2, underlying.extractTo3, underlying.extractTo4, underlying.extractTo5)(underlying.transform).map {
        case Nil => None
        case results if results.size == 1 => results.headOption
        case results => throw new TooManyRowsException(1, results.size)
      }
  }
}

class AsyncOneToManies5SQLToIterable[A, B1, B2, B3, B4, B5, Z](val underlying: OneToManies5SQLToIterable[A, B1, B2, B3, B4, B5, HasExtractor, Z])
  extends AnyVal
  with AsyncSQLToIterable[Z] {
  override def future()(implicit session: AsyncDBSession, cxt: EC = ECGlobal): Future[Iterable[Z]] = {
    session.oneToManies5Iterable(underlying.statement, underlying.rawParameters.toSeq: _*)(underlying.extractOne)(
      underlying.extractTo1, underlying.extractTo2, underlying.extractTo3, underlying.extractTo4, underlying.extractTo5)(underlying.transform)
  }
}

class AsyncOneToManies5SQLToList[A, B1, B2, B3, B4, B5, Z](val underlying: OneToManies5SQLToList[A, B1, B2, B3, B4, B5, HasExtractor, Z])
  extends AnyVal
  with AsyncSQLToList[Z] {
  override def future()(implicit session: AsyncDBSession, cxt: EC = ECGlobal): Future[List[Z]] = {
    val iterable = session.oneToManies5Iterable(underlying.statement, underlying.rawParameters.toSeq: _*)(underlying.extractOne)(
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
    session.oneToManies6Iterable(underlying.statement, underlying.rawParameters.toSeq: _*)(underlying.extractOne)(
      underlying.extractTo1, underlying.extractTo2, underlying.extractTo3, underlying.extractTo4, underlying.extractTo5, underlying.extractTo6)(underlying.transform).map {
        case Nil => None
        case results if results.size == 1 => results.headOption
        case results => throw new TooManyRowsException(1, results.size)
      }
  }
}

class AsyncOneToManies6SQLToIterable[A, B1, B2, B3, B4, B5, B6, Z](val underlying: OneToManies6SQLToIterable[A, B1, B2, B3, B4, B5, B6, HasExtractor, Z])
  extends AnyVal
  with AsyncSQLToIterable[Z] {
  override def future()(implicit session: AsyncDBSession, cxt: EC = ECGlobal): Future[Iterable[Z]] = {
    session.oneToManies6Iterable(underlying.statement, underlying.rawParameters.toSeq: _*)(underlying.extractOne)(
      underlying.extractTo1, underlying.extractTo2, underlying.extractTo3, underlying.extractTo4, underlying.extractTo5, underlying.extractTo6)(underlying.transform)
  }
}

class AsyncOneToManies6SQLToList[A, B1, B2, B3, B4, B5, B6, Z](val underlying: OneToManies6SQLToList[A, B1, B2, B3, B4, B5, B6, HasExtractor, Z])
  extends AnyVal
  with AsyncSQLToList[Z] {
  override def future()(implicit session: AsyncDBSession, cxt: EC = ECGlobal): Future[List[Z]] = {
    val iterable = session.oneToManies6Iterable(underlying.statement, underlying.rawParameters.toSeq: _*)(underlying.extractOne)(
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
    session.oneToManies7Iterable(underlying.statement, underlying.rawParameters.toSeq: _*)(underlying.extractOne)(
      underlying.extractTo1, underlying.extractTo2, underlying.extractTo3, underlying.extractTo4, underlying.extractTo5, underlying.extractTo6, underlying.extractTo7)(underlying.transform).map {
        case Nil => None
        case results if results.size == 1 => results.headOption
        case results => throw new TooManyRowsException(1, results.size)
      }
  }
}

class AsyncOneToManies7SQLToIterable[A, B1, B2, B3, B4, B5, B6, B7, Z](val underlying: OneToManies7SQLToIterable[A, B1, B2, B3, B4, B5, B6, B7, HasExtractor, Z])
  extends AnyVal
  with AsyncSQLToIterable[Z] {
  override def future()(implicit session: AsyncDBSession, cxt: EC = ECGlobal): Future[Iterable[Z]] = {
    session.oneToManies7Iterable(underlying.statement, underlying.rawParameters.toSeq: _*)(underlying.extractOne)(
      underlying.extractTo1, underlying.extractTo2, underlying.extractTo3, underlying.extractTo4, underlying.extractTo5, underlying.extractTo6, underlying.extractTo7)(underlying.transform)
  }
}

class AsyncOneToManies7SQLToList[A, B1, B2, B3, B4, B5, B6, B7, Z](val underlying: OneToManies7SQLToList[A, B1, B2, B3, B4, B5, B6, B7, HasExtractor, Z])
  extends AnyVal
  with AsyncSQLToList[Z] {
  override def future()(implicit session: AsyncDBSession, cxt: EC = ECGlobal): Future[List[Z]] = {
    val iterable = session.oneToManies7Iterable(underlying.statement, underlying.rawParameters.toSeq: _*)(underlying.extractOne)(
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
    session.oneToManies8Iterable(underlying.statement, underlying.rawParameters.toSeq: _*)(underlying.extractOne)(
      underlying.extractTo1, underlying.extractTo2, underlying.extractTo3, underlying.extractTo4, underlying.extractTo5, underlying.extractTo6, underlying.extractTo7, underlying.extractTo8)(underlying.transform).map {
        case Nil => None
        case results if results.size == 1 => results.headOption
        case results => throw new TooManyRowsException(1, results.size)
      }
  }
}

class AsyncOneToManies8SQLToIterable[A, B1, B2, B3, B4, B5, B6, B7, B8, Z](val underlying: OneToManies8SQLToIterable[A, B1, B2, B3, B4, B5, B6, B7, B8, HasExtractor, Z])
  extends AnyVal
  with AsyncSQLToIterable[Z] {
  override def future()(implicit session: AsyncDBSession, cxt: EC = ECGlobal): Future[Iterable[Z]] = {
    session.oneToManies8Iterable(underlying.statement, underlying.rawParameters.toSeq: _*)(underlying.extractOne)(
      underlying.extractTo1, underlying.extractTo2, underlying.extractTo3, underlying.extractTo4, underlying.extractTo5, underlying.extractTo6, underlying.extractTo7, underlying.extractTo8)(underlying.transform)
  }
}

class AsyncOneToManies8SQLToList[A, B1, B2, B3, B4, B5, B6, B7, B8, Z](val underlying: OneToManies8SQLToList[A, B1, B2, B3, B4, B5, B6, B7, B8, HasExtractor, Z])
  extends AnyVal
  with AsyncSQLToList[Z] {
  override def future()(implicit session: AsyncDBSession, cxt: EC = ECGlobal): Future[List[Z]] = {
    val iterable = session.oneToManies8Iterable(underlying.statement, underlying.rawParameters.toSeq: _*)(underlying.extractOne)(
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
    session.oneToManies9Iterable(underlying.statement, underlying.rawParameters.toSeq: _*)(underlying.extractOne)(
      underlying.extractTo1, underlying.extractTo2, underlying.extractTo3, underlying.extractTo4, underlying.extractTo5, underlying.extractTo6, underlying.extractTo7, underlying.extractTo8, underlying.extractTo9)(underlying.transform).map {
        case Nil => None
        case results if results.size == 1 => results.headOption
        case results => throw new TooManyRowsException(1, results.size)
      }
  }
}

class AsyncOneToManies9SQLToIterable[A, B1, B2, B3, B4, B5, B6, B7, B8, B9, Z](val underlying: OneToManies9SQLToIterable[A, B1, B2, B3, B4, B5, B6, B7, B8, B9, HasExtractor, Z])
  extends AnyVal
  with AsyncSQLToIterable[Z] {
  override def future()(implicit session: AsyncDBSession, cxt: EC = ECGlobal): Future[Iterable[Z]] = {
    session.oneToManies9Iterable(underlying.statement, underlying.rawParameters.toSeq: _*)(underlying.extractOne)(
      underlying.extractTo1, underlying.extractTo2, underlying.extractTo3, underlying.extractTo4, underlying.extractTo5, underlying.extractTo6, underlying.extractTo7, underlying.extractTo8, underlying.extractTo9)(underlying.transform)
  }
}

class AsyncOneToManies9SQLToList[A, B1, B2, B3, B4, B5, B6, B7, B8, B9, Z](val underlying: OneToManies9SQLToList[A, B1, B2, B3, B4, B5, B6, B7, B8, B9, HasExtractor, Z])
  extends AnyVal
  with AsyncSQLToList[Z] {
  override def future()(implicit session: AsyncDBSession, cxt: EC = ECGlobal): Future[List[Z]] = {
    val iterable = session.oneToManies9Iterable(underlying.statement, underlying.rawParameters.toSeq: _*)(underlying.extractOne)(
      underlying.extractTo1, underlying.extractTo2, underlying.extractTo3, underlying.extractTo4, underlying.extractTo5, underlying.extractTo6, underlying.extractTo7, underlying.extractTo8, underlying.extractTo9)(underlying.transform)
    iterable.map(_.toList)
  }
}

// -------------------
// one-to-manies 10
// -------------------

class AsyncOneToManies10SQLToOption[A, B1, B2, B3, B4, B5, B6, B7, B8, B9, B10, Z](val underlying: OneToManies10SQLToOption[A, B1, B2, B3, B4, B5, B6, B7, B8, B9, B10, HasExtractor, Z])
  extends AnyVal
  with AsyncSQLToOption[Z] {
  override def future()(implicit session: AsyncDBSession, cxt: EC = ECGlobal): Future[Option[Z]] = {
    session.oneToManies10Iterable(underlying.statement, underlying.rawParameters.toSeq: _*)(underlying.extractOne)(
      underlying.extractTo1, underlying.extractTo2, underlying.extractTo3, underlying.extractTo4, underlying.extractTo5, underlying.extractTo6, underlying.extractTo7, underlying.extractTo8, underlying.extractTo9, underlying.extractTo10)(underlying.transform).map {
        case Nil => None
        case results if results.size == 1 => results.headOption
        case results => throw new TooManyRowsException(1, results.size)
      }
  }
}

class AsyncOneToManies10SQLToIterable[A, B1, B2, B3, B4, B5, B6, B7, B8, B9, B10, Z](val underlying: OneToManies10SQLToIterable[A, B1, B2, B3, B4, B5, B6, B7, B8, B9, B10, HasExtractor, Z])
  extends AnyVal
  with AsyncSQLToIterable[Z] {
  override def future()(implicit session: AsyncDBSession, cxt: EC = ECGlobal): Future[Iterable[Z]] = {
    session.oneToManies10Iterable(underlying.statement, underlying.rawParameters.toSeq: _*)(underlying.extractOne)(
      underlying.extractTo1, underlying.extractTo2, underlying.extractTo3, underlying.extractTo4, underlying.extractTo5, underlying.extractTo6, underlying.extractTo7, underlying.extractTo8, underlying.extractTo9, underlying.extractTo10)(underlying.transform)
  }
}

class AsyncOneToManies10SQLToList[A, B1, B2, B3, B4, B5, B6, B7, B8, B9, B10, Z](val underlying: OneToManies10SQLToList[A, B1, B2, B3, B4, B5, B6, B7, B8, B9, B10, HasExtractor, Z])
  extends AnyVal
  with AsyncSQLToList[Z] {
  override def future()(implicit session: AsyncDBSession, cxt: EC = ECGlobal): Future[List[Z]] = {
    val iterable = session.oneToManies10Iterable(underlying.statement, underlying.rawParameters.toSeq: _*)(underlying.extractOne)(
      underlying.extractTo1, underlying.extractTo2, underlying.extractTo3, underlying.extractTo4, underlying.extractTo5, underlying.extractTo6, underlying.extractTo7, underlying.extractTo8, underlying.extractTo9, underlying.extractTo10)(underlying.transform)
    iterable.map(_.toList)
  }
}

// -------------------
// one-to-manies 11
// -------------------

class AsyncOneToManies11SQLToOption[A, B1, B2, B3, B4, B5, B6, B7, B8, B9, B10, B11, Z](val underlying: OneToManies11SQLToOption[A, B1, B2, B3, B4, B5, B6, B7, B8, B9, B10, B11, HasExtractor, Z])
  extends AnyVal
  with AsyncSQLToOption[Z] {
  override def future()(implicit session: AsyncDBSession, cxt: EC = ECGlobal): Future[Option[Z]] = {
    session.oneToManies11Iterable(underlying.statement, underlying.rawParameters.toSeq: _*)(underlying.extractOne)(
      underlying.extractTo1, underlying.extractTo2, underlying.extractTo3, underlying.extractTo4, underlying.extractTo5, underlying.extractTo6, underlying.extractTo7, underlying.extractTo8, underlying.extractTo9, underlying.extractTo10, underlying.extractTo11)(underlying.transform).map {
        case Nil => None
        case results if results.size == 1 => results.headOption
        case results => throw new TooManyRowsException(1, results.size)
      }
  }
}

class AsyncOneToManies11SQLToIterable[A, B1, B2, B3, B4, B5, B6, B7, B8, B9, B10, B11, Z](val underlying: OneToManies11SQLToIterable[A, B1, B2, B3, B4, B5, B6, B7, B8, B9, B10, B11, HasExtractor, Z])
  extends AnyVal
  with AsyncSQLToIterable[Z] {
  override def future()(implicit session: AsyncDBSession, cxt: EC = ECGlobal): Future[Iterable[Z]] = {
    session.oneToManies11Iterable(underlying.statement, underlying.rawParameters.toSeq: _*)(underlying.extractOne)(
      underlying.extractTo1, underlying.extractTo2, underlying.extractTo3, underlying.extractTo4, underlying.extractTo5, underlying.extractTo6, underlying.extractTo7, underlying.extractTo8, underlying.extractTo9, underlying.extractTo10, underlying.extractTo11)(underlying.transform)
  }
}

class AsyncOneToManies11SQLToList[A, B1, B2, B3, B4, B5, B6, B7, B8, B9, B10, B11, Z](val underlying: OneToManies11SQLToList[A, B1, B2, B3, B4, B5, B6, B7, B8, B9, B10, B11, HasExtractor, Z])
  extends AnyVal
  with AsyncSQLToList[Z] {
  override def future()(implicit session: AsyncDBSession, cxt: EC = ECGlobal): Future[List[Z]] = {
    val iterable = session.oneToManies11Iterable(underlying.statement, underlying.rawParameters.toSeq: _*)(underlying.extractOne)(
      underlying.extractTo1, underlying.extractTo2, underlying.extractTo3, underlying.extractTo4, underlying.extractTo5, underlying.extractTo6, underlying.extractTo7, underlying.extractTo8, underlying.extractTo9, underlying.extractTo10, underlying.extractTo11)(underlying.transform)
    iterable.map(_.toList)
  }
}

// -------------------
// one-to-manies 12
// -------------------

class AsyncOneToManies12SQLToOption[A, B1, B2, B3, B4, B5, B6, B7, B8, B9, B10, B11, B12, Z](val underlying: OneToManies12SQLToOption[A, B1, B2, B3, B4, B5, B6, B7, B8, B9, B10, B11, B12, HasExtractor, Z])
  extends AnyVal
  with AsyncSQLToOption[Z] {
  override def future()(implicit session: AsyncDBSession, cxt: EC = ECGlobal): Future[Option[Z]] = {
    session.oneToManies12Iterable(underlying.statement, underlying.rawParameters.toSeq: _*)(underlying.extractOne)(
      underlying.extractTo1, underlying.extractTo2, underlying.extractTo3, underlying.extractTo4, underlying.extractTo5, underlying.extractTo6, underlying.extractTo7, underlying.extractTo8, underlying.extractTo9, underlying.extractTo10, underlying.extractTo11, underlying.extractTo12)(underlying.transform).map {
        case Nil => None
        case results if results.size == 1 => results.headOption
        case results => throw new TooManyRowsException(1, results.size)
      }
  }
}

class AsyncOneToManies12SQLToIterable[A, B1, B2, B3, B4, B5, B6, B7, B8, B9, B10, B11, B12, Z](val underlying: OneToManies12SQLToIterable[A, B1, B2, B3, B4, B5, B6, B7, B8, B9, B10, B11, B12, HasExtractor, Z])
  extends AnyVal
  with AsyncSQLToIterable[Z] {
  override def future()(implicit session: AsyncDBSession, cxt: EC = ECGlobal): Future[Iterable[Z]] = {
    session.oneToManies12Iterable(underlying.statement, underlying.rawParameters.toSeq: _*)(underlying.extractOne)(
      underlying.extractTo1, underlying.extractTo2, underlying.extractTo3, underlying.extractTo4, underlying.extractTo5, underlying.extractTo6, underlying.extractTo7, underlying.extractTo8, underlying.extractTo9, underlying.extractTo10, underlying.extractTo11, underlying.extractTo12)(underlying.transform)
  }
}

class AsyncOneToManies12SQLToList[A, B1, B2, B3, B4, B5, B6, B7, B8, B9, B10, B11, B12, Z](val underlying: OneToManies12SQLToList[A, B1, B2, B3, B4, B5, B6, B7, B8, B9, B10, B11, B12, HasExtractor, Z])
  extends AnyVal
  with AsyncSQLToList[Z] {
  override def future()(implicit session: AsyncDBSession, cxt: EC = ECGlobal): Future[List[Z]] = {
    val iterable = session.oneToManies12Iterable(underlying.statement, underlying.rawParameters.toSeq: _*)(underlying.extractOne)(
      underlying.extractTo1, underlying.extractTo2, underlying.extractTo3, underlying.extractTo4, underlying.extractTo5, underlying.extractTo6, underlying.extractTo7, underlying.extractTo8, underlying.extractTo9, underlying.extractTo10, underlying.extractTo11, underlying.extractTo12)(underlying.transform)
    iterable.map(_.toList)
  }
}

// -------------------
// one-to-manies 13
// -------------------

class AsyncOneToManies13SQLToOption[A, B1, B2, B3, B4, B5, B6, B7, B8, B9, B10, B11, B12, B13, Z](val underlying: OneToManies13SQLToOption[A, B1, B2, B3, B4, B5, B6, B7, B8, B9, B10, B11, B12, B13, HasExtractor, Z])
  extends AnyVal
  with AsyncSQLToOption[Z] {
  override def future()(implicit session: AsyncDBSession, cxt: EC = ECGlobal): Future[Option[Z]] = {
    session.oneToManies13Iterable(underlying.statement, underlying.rawParameters.toSeq: _*)(underlying.extractOne)(
      underlying.extractTo1, underlying.extractTo2, underlying.extractTo3, underlying.extractTo4, underlying.extractTo5, underlying.extractTo6, underlying.extractTo7, underlying.extractTo8, underlying.extractTo9, underlying.extractTo10, underlying.extractTo11, underlying.extractTo12, underlying.extractTo13)(underlying.transform).map {
        case Nil => None
        case results if results.size == 1 => results.headOption
        case results => throw new TooManyRowsException(1, results.size)
      }
  }
}

class AsyncOneToManies13SQLToIterable[A, B1, B2, B3, B4, B5, B6, B7, B8, B9, B10, B11, B12, B13, Z](val underlying: OneToManies13SQLToIterable[A, B1, B2, B3, B4, B5, B6, B7, B8, B9, B10, B11, B12, B13, HasExtractor, Z])
  extends AnyVal
  with AsyncSQLToIterable[Z] {
  override def future()(implicit session: AsyncDBSession, cxt: EC = ECGlobal): Future[Iterable[Z]] = {
    session.oneToManies13Iterable(underlying.statement, underlying.rawParameters.toSeq: _*)(underlying.extractOne)(
      underlying.extractTo1, underlying.extractTo2, underlying.extractTo3, underlying.extractTo4, underlying.extractTo5, underlying.extractTo6, underlying.extractTo7, underlying.extractTo8, underlying.extractTo9, underlying.extractTo10, underlying.extractTo11, underlying.extractTo12, underlying.extractTo13)(underlying.transform)
  }
}

class AsyncOneToManies13SQLToList[A, B1, B2, B3, B4, B5, B6, B7, B8, B9, B10, B11, B12, B13, Z](val underlying: OneToManies13SQLToList[A, B1, B2, B3, B4, B5, B6, B7, B8, B9, B10, B11, B12, B13, HasExtractor, Z])
  extends AnyVal
  with AsyncSQLToList[Z] {
  override def future()(implicit session: AsyncDBSession, cxt: EC = ECGlobal): Future[List[Z]] = {
    val iterable = session.oneToManies13Iterable(underlying.statement, underlying.rawParameters.toSeq: _*)(underlying.extractOne)(
      underlying.extractTo1, underlying.extractTo2, underlying.extractTo3, underlying.extractTo4, underlying.extractTo5, underlying.extractTo6, underlying.extractTo7, underlying.extractTo8, underlying.extractTo9, underlying.extractTo10, underlying.extractTo11, underlying.extractTo12, underlying.extractTo13)(underlying.transform)
    iterable.map(_.toList)
  }
}

// -------------------
// one-to-manies 14
// -------------------

class AsyncOneToManies14SQLToOption[A, B1, B2, B3, B4, B5, B6, B7, B8, B9, B10, B11, B12, B13, B14, Z](val underlying: OneToManies14SQLToOption[A, B1, B2, B3, B4, B5, B6, B7, B8, B9, B10, B11, B12, B13, B14, HasExtractor, Z])
  extends AnyVal
  with AsyncSQLToOption[Z] {
  override def future()(implicit session: AsyncDBSession, cxt: EC = ECGlobal): Future[Option[Z]] = {
    session.oneToManies14Iterable(underlying.statement, underlying.rawParameters.toSeq: _*)(underlying.extractOne)(
      underlying.extractTo1, underlying.extractTo2, underlying.extractTo3, underlying.extractTo4, underlying.extractTo5, underlying.extractTo6, underlying.extractTo7, underlying.extractTo8, underlying.extractTo9, underlying.extractTo10, underlying.extractTo11, underlying.extractTo12, underlying.extractTo13, underlying.extractTo14)(underlying.transform).map {
        case Nil => None
        case results if results.size == 1 => results.headOption
        case results => throw new TooManyRowsException(1, results.size)
      }
  }
}

class AsyncOneToManies14SQLToIterable[A, B1, B2, B3, B4, B5, B6, B7, B8, B9, B10, B11, B12, B13, B14, Z](val underlying: OneToManies14SQLToIterable[A, B1, B2, B3, B4, B5, B6, B7, B8, B9, B10, B11, B12, B13, B14, HasExtractor, Z])
  extends AnyVal
  with AsyncSQLToIterable[Z] {
  override def future()(implicit session: AsyncDBSession, cxt: EC = ECGlobal): Future[Iterable[Z]] = {
    session.oneToManies14Iterable(underlying.statement, underlying.rawParameters.toSeq: _*)(underlying.extractOne)(
      underlying.extractTo1, underlying.extractTo2, underlying.extractTo3, underlying.extractTo4, underlying.extractTo5, underlying.extractTo6, underlying.extractTo7, underlying.extractTo8, underlying.extractTo9, underlying.extractTo10, underlying.extractTo11, underlying.extractTo12, underlying.extractTo13, underlying.extractTo14)(underlying.transform)
  }
}

class AsyncOneToManies14SQLToList[A, B1, B2, B3, B4, B5, B6, B7, B8, B9, B10, B11, B12, B13, B14, Z](val underlying: OneToManies14SQLToList[A, B1, B2, B3, B4, B5, B6, B7, B8, B9, B10, B11, B12, B13, B14, HasExtractor, Z])
  extends AnyVal
  with AsyncSQLToList[Z] {
  override def future()(implicit session: AsyncDBSession, cxt: EC = ECGlobal): Future[List[Z]] = {
    val iterable = session.oneToManies14Iterable(underlying.statement, underlying.rawParameters.toSeq: _*)(underlying.extractOne)(
      underlying.extractTo1, underlying.extractTo2, underlying.extractTo3, underlying.extractTo4, underlying.extractTo5, underlying.extractTo6, underlying.extractTo7, underlying.extractTo8, underlying.extractTo9, underlying.extractTo10, underlying.extractTo11, underlying.extractTo12, underlying.extractTo13, underlying.extractTo14)(underlying.transform)
    iterable.map(_.toList)
  }
}

// -------------------
// one-to-manies 15
// -------------------

class AsyncOneToManies15SQLToOption[A, B1, B2, B3, B4, B5, B6, B7, B8, B9, B10, B11, B12, B13, B14, B15, Z](val underlying: OneToManies15SQLToOption[A, B1, B2, B3, B4, B5, B6, B7, B8, B9, B10, B11, B12, B13, B14, B15, HasExtractor, Z])
  extends AnyVal
  with AsyncSQLToOption[Z] {
  override def future()(implicit session: AsyncDBSession, cxt: EC = ECGlobal): Future[Option[Z]] = {
    session.oneToManies15Iterable(underlying.statement, underlying.rawParameters.toSeq: _*)(underlying.extractOne)(
      underlying.extractTo1, underlying.extractTo2, underlying.extractTo3, underlying.extractTo4, underlying.extractTo5, underlying.extractTo6, underlying.extractTo7, underlying.extractTo8, underlying.extractTo9, underlying.extractTo10, underlying.extractTo11, underlying.extractTo12, underlying.extractTo13, underlying.extractTo14, underlying.extractTo15)(underlying.transform).map {
        case Nil => None
        case results if results.size == 1 => results.headOption
        case results => throw new TooManyRowsException(1, results.size)
      }
  }
}

class AsyncOneToManies15SQLToIterable[A, B1, B2, B3, B4, B5, B6, B7, B8, B9, B10, B11, B12, B13, B14, B15, Z](val underlying: OneToManies15SQLToIterable[A, B1, B2, B3, B4, B5, B6, B7, B8, B9, B10, B11, B12, B13, B14, B15, HasExtractor, Z])
  extends AnyVal
  with AsyncSQLToIterable[Z] {
  override def future()(implicit session: AsyncDBSession, cxt: EC = ECGlobal): Future[Iterable[Z]] = {
    session.oneToManies15Iterable(underlying.statement, underlying.rawParameters.toSeq: _*)(underlying.extractOne)(
      underlying.extractTo1, underlying.extractTo2, underlying.extractTo3, underlying.extractTo4, underlying.extractTo5, underlying.extractTo6, underlying.extractTo7, underlying.extractTo8, underlying.extractTo9, underlying.extractTo10, underlying.extractTo11, underlying.extractTo12, underlying.extractTo13, underlying.extractTo14, underlying.extractTo15)(underlying.transform)
  }
}

class AsyncOneToManies15SQLToList[A, B1, B2, B3, B4, B5, B6, B7, B8, B9, B10, B11, B12, B13, B14, B15, Z](val underlying: OneToManies15SQLToList[A, B1, B2, B3, B4, B5, B6, B7, B8, B9, B10, B11, B12, B13, B14, B15, HasExtractor, Z])
  extends AnyVal
  with AsyncSQLToList[Z] {
  override def future()(implicit session: AsyncDBSession, cxt: EC = ECGlobal): Future[List[Z]] = {
    val iterable = session.oneToManies15Iterable(underlying.statement, underlying.rawParameters.toSeq: _*)(underlying.extractOne)(
      underlying.extractTo1, underlying.extractTo2, underlying.extractTo3, underlying.extractTo4, underlying.extractTo5, underlying.extractTo6, underlying.extractTo7, underlying.extractTo8, underlying.extractTo9, underlying.extractTo10, underlying.extractTo11, underlying.extractTo12, underlying.extractTo13, underlying.extractTo14, underlying.extractTo15)(underlying.transform)
    iterable.map(_.toList)
  }
}

// -------------------
// one-to-manies 16
// -------------------

class AsyncOneToManies16SQLToOption[A, B1, B2, B3, B4, B5, B6, B7, B8, B9, B10, B11, B12, B13, B14, B15, B16, Z](val underlying: OneToManies16SQLToOption[A, B1, B2, B3, B4, B5, B6, B7, B8, B9, B10, B11, B12, B13, B14, B15, B16, HasExtractor, Z])
  extends AnyVal
  with AsyncSQLToOption[Z] {
  override def future()(implicit session: AsyncDBSession, cxt: EC = ECGlobal): Future[Option[Z]] = {
    session.oneToManies16Iterable(underlying.statement, underlying.rawParameters.toSeq: _*)(underlying.extractOne)(
      underlying.extractTo1, underlying.extractTo2, underlying.extractTo3, underlying.extractTo4, underlying.extractTo5, underlying.extractTo6, underlying.extractTo7, underlying.extractTo8, underlying.extractTo9, underlying.extractTo10, underlying.extractTo11, underlying.extractTo12, underlying.extractTo13, underlying.extractTo14, underlying.extractTo15, underlying.extractTo16)(underlying.transform).map {
        case Nil => None
        case results if results.size == 1 => results.headOption
        case results => throw new TooManyRowsException(1, results.size)
      }
  }
}

class AsyncOneToManies16SQLToIterable[A, B1, B2, B3, B4, B5, B6, B7, B8, B9, B10, B11, B12, B13, B14, B15, B16, Z](val underlying: OneToManies16SQLToIterable[A, B1, B2, B3, B4, B5, B6, B7, B8, B9, B10, B11, B12, B13, B14, B15, B16, HasExtractor, Z])
  extends AnyVal
  with AsyncSQLToIterable[Z] {
  override def future()(implicit session: AsyncDBSession, cxt: EC = ECGlobal): Future[Iterable[Z]] = {
    session.oneToManies16Iterable(underlying.statement, underlying.rawParameters.toSeq: _*)(underlying.extractOne)(
      underlying.extractTo1, underlying.extractTo2, underlying.extractTo3, underlying.extractTo4, underlying.extractTo5, underlying.extractTo6, underlying.extractTo7, underlying.extractTo8, underlying.extractTo9, underlying.extractTo10, underlying.extractTo11, underlying.extractTo12, underlying.extractTo13, underlying.extractTo14, underlying.extractTo15, underlying.extractTo16)(underlying.transform)
  }
}

class AsyncOneToManies16SQLToList[A, B1, B2, B3, B4, B5, B6, B7, B8, B9, B10, B11, B12, B13, B14, B15, B16, Z](val underlying: OneToManies16SQLToList[A, B1, B2, B3, B4, B5, B6, B7, B8, B9, B10, B11, B12, B13, B14, B15, B16, HasExtractor, Z])
  extends AnyVal
  with AsyncSQLToList[Z] {
  override def future()(implicit session: AsyncDBSession, cxt: EC = ECGlobal): Future[List[Z]] = {
    val iterable = session.oneToManies16Iterable(underlying.statement, underlying.rawParameters.toSeq: _*)(underlying.extractOne)(
      underlying.extractTo1, underlying.extractTo2, underlying.extractTo3, underlying.extractTo4, underlying.extractTo5, underlying.extractTo6, underlying.extractTo7, underlying.extractTo8, underlying.extractTo9, underlying.extractTo10, underlying.extractTo11, underlying.extractTo12, underlying.extractTo13, underlying.extractTo14, underlying.extractTo15, underlying.extractTo16)(underlying.transform)
    iterable.map(_.toList)
  }
}

// -------------------
// one-to-manies 17
// -------------------

class AsyncOneToManies17SQLToOption[A, B1, B2, B3, B4, B5, B6, B7, B8, B9, B10, B11, B12, B13, B14, B15, B16, B17, Z](val underlying: OneToManies17SQLToOption[A, B1, B2, B3, B4, B5, B6, B7, B8, B9, B10, B11, B12, B13, B14, B15, B16, B17, HasExtractor, Z])
  extends AnyVal
  with AsyncSQLToOption[Z] {
  override def future()(implicit session: AsyncDBSession, cxt: EC = ECGlobal): Future[Option[Z]] = {
    session.oneToManies17Iterable(underlying.statement, underlying.rawParameters.toSeq: _*)(underlying.extractOne)(
      underlying.extractTo1, underlying.extractTo2, underlying.extractTo3, underlying.extractTo4, underlying.extractTo5, underlying.extractTo6, underlying.extractTo7, underlying.extractTo8, underlying.extractTo9, underlying.extractTo10, underlying.extractTo11, underlying.extractTo12, underlying.extractTo13, underlying.extractTo14, underlying.extractTo15, underlying.extractTo16, underlying.extractTo17)(underlying.transform).map {
        case Nil => None
        case results if results.size == 1 => results.headOption
        case results => throw new TooManyRowsException(1, results.size)
      }
  }
}

class AsyncOneToManies17SQLToIterable[A, B1, B2, B3, B4, B5, B6, B7, B8, B9, B10, B11, B12, B13, B14, B15, B16, B17, Z](val underlying: OneToManies17SQLToIterable[A, B1, B2, B3, B4, B5, B6, B7, B8, B9, B10, B11, B12, B13, B14, B15, B16, B17, HasExtractor, Z])
  extends AnyVal
  with AsyncSQLToIterable[Z] {
  override def future()(implicit session: AsyncDBSession, cxt: EC = ECGlobal): Future[Iterable[Z]] = {
    session.oneToManies17Iterable(underlying.statement, underlying.rawParameters.toSeq: _*)(underlying.extractOne)(
      underlying.extractTo1, underlying.extractTo2, underlying.extractTo3, underlying.extractTo4, underlying.extractTo5, underlying.extractTo6, underlying.extractTo7, underlying.extractTo8, underlying.extractTo9, underlying.extractTo10, underlying.extractTo11, underlying.extractTo12, underlying.extractTo13, underlying.extractTo14, underlying.extractTo15, underlying.extractTo16, underlying.extractTo17)(underlying.transform)
  }
}

class AsyncOneToManies17SQLToList[A, B1, B2, B3, B4, B5, B6, B7, B8, B9, B10, B11, B12, B13, B14, B15, B16, B17, Z](val underlying: OneToManies17SQLToList[A, B1, B2, B3, B4, B5, B6, B7, B8, B9, B10, B11, B12, B13, B14, B15, B16, B17, HasExtractor, Z])
  extends AnyVal
  with AsyncSQLToList[Z] {
  override def future()(implicit session: AsyncDBSession, cxt: EC = ECGlobal): Future[List[Z]] = {
    val iterable = session.oneToManies17Iterable(underlying.statement, underlying.rawParameters.toSeq: _*)(underlying.extractOne)(
      underlying.extractTo1, underlying.extractTo2, underlying.extractTo3, underlying.extractTo4, underlying.extractTo5, underlying.extractTo6, underlying.extractTo7, underlying.extractTo8, underlying.extractTo9, underlying.extractTo10, underlying.extractTo11, underlying.extractTo12, underlying.extractTo13, underlying.extractTo14, underlying.extractTo15, underlying.extractTo16, underlying.extractTo17)(underlying.transform)
    iterable.map(_.toList)
  }
}

// -------------------
// one-to-manies 18
// -------------------

class AsyncOneToManies18SQLToOption[A, B1, B2, B3, B4, B5, B6, B7, B8, B9, B10, B11, B12, B13, B14, B15, B16, B17, B18, Z](val underlying: OneToManies18SQLToOption[A, B1, B2, B3, B4, B5, B6, B7, B8, B9, B10, B11, B12, B13, B14, B15, B16, B17, B18, HasExtractor, Z])
  extends AnyVal
  with AsyncSQLToOption[Z] {
  override def future()(implicit session: AsyncDBSession, cxt: EC = ECGlobal): Future[Option[Z]] = {
    session.oneToManies18Iterable(underlying.statement, underlying.rawParameters.toSeq: _*)(underlying.extractOne)(
      underlying.extractTo1, underlying.extractTo2, underlying.extractTo3, underlying.extractTo4, underlying.extractTo5, underlying.extractTo6, underlying.extractTo7, underlying.extractTo8, underlying.extractTo9, underlying.extractTo10, underlying.extractTo11, underlying.extractTo12, underlying.extractTo13, underlying.extractTo14, underlying.extractTo15, underlying.extractTo16, underlying.extractTo17, underlying.extractTo18)(underlying.transform).map {
        case Nil => None
        case results if results.size == 1 => results.headOption
        case results => throw new TooManyRowsException(1, results.size)
      }
  }
}

class AsyncOneToManies18SQLToIterable[A, B1, B2, B3, B4, B5, B6, B7, B8, B9, B10, B11, B12, B13, B14, B15, B16, B17, B18, Z](val underlying: OneToManies18SQLToIterable[A, B1, B2, B3, B4, B5, B6, B7, B8, B9, B10, B11, B12, B13, B14, B15, B16, B17, B18, HasExtractor, Z])
  extends AnyVal
  with AsyncSQLToIterable[Z] {
  override def future()(implicit session: AsyncDBSession, cxt: EC = ECGlobal): Future[Iterable[Z]] = {
    session.oneToManies18Iterable(underlying.statement, underlying.rawParameters.toSeq: _*)(underlying.extractOne)(
      underlying.extractTo1, underlying.extractTo2, underlying.extractTo3, underlying.extractTo4, underlying.extractTo5, underlying.extractTo6, underlying.extractTo7, underlying.extractTo8, underlying.extractTo9, underlying.extractTo10, underlying.extractTo11, underlying.extractTo12, underlying.extractTo13, underlying.extractTo14, underlying.extractTo15, underlying.extractTo16, underlying.extractTo17, underlying.extractTo18)(underlying.transform)
  }
}

class AsyncOneToManies18SQLToList[A, B1, B2, B3, B4, B5, B6, B7, B8, B9, B10, B11, B12, B13, B14, B15, B16, B17, B18, Z](val underlying: OneToManies18SQLToList[A, B1, B2, B3, B4, B5, B6, B7, B8, B9, B10, B11, B12, B13, B14, B15, B16, B17, B18, HasExtractor, Z])
  extends AnyVal
  with AsyncSQLToList[Z] {
  override def future()(implicit session: AsyncDBSession, cxt: EC = ECGlobal): Future[List[Z]] = {
    val iterable = session.oneToManies18Iterable(underlying.statement, underlying.rawParameters.toSeq: _*)(underlying.extractOne)(
      underlying.extractTo1, underlying.extractTo2, underlying.extractTo3, underlying.extractTo4, underlying.extractTo5, underlying.extractTo6, underlying.extractTo7, underlying.extractTo8, underlying.extractTo9, underlying.extractTo10, underlying.extractTo11, underlying.extractTo12, underlying.extractTo13, underlying.extractTo14, underlying.extractTo15, underlying.extractTo16, underlying.extractTo17, underlying.extractTo18)(underlying.transform)
    iterable.map(_.toList)
  }
}

// -------------------
// one-to-manies 19
// -------------------

class AsyncOneToManies19SQLToOption[A, B1, B2, B3, B4, B5, B6, B7, B8, B9, B10, B11, B12, B13, B14, B15, B16, B17, B18, B19, Z](val underlying: OneToManies19SQLToOption[A, B1, B2, B3, B4, B5, B6, B7, B8, B9, B10, B11, B12, B13, B14, B15, B16, B17, B18, B19, HasExtractor, Z])
  extends AnyVal
  with AsyncSQLToOption[Z] {
  override def future()(implicit session: AsyncDBSession, cxt: EC = ECGlobal): Future[Option[Z]] = {
    session.oneToManies19Iterable(underlying.statement, underlying.rawParameters.toSeq: _*)(underlying.extractOne)(
      underlying.extractTo1, underlying.extractTo2, underlying.extractTo3, underlying.extractTo4, underlying.extractTo5, underlying.extractTo6, underlying.extractTo7, underlying.extractTo8, underlying.extractTo9, underlying.extractTo10, underlying.extractTo11, underlying.extractTo12, underlying.extractTo13, underlying.extractTo14, underlying.extractTo15, underlying.extractTo16, underlying.extractTo17, underlying.extractTo18, underlying.extractTo19)(underlying.transform).map {
        case Nil => None
        case results if results.size == 1 => results.headOption
        case results => throw new TooManyRowsException(1, results.size)
      }
  }
}

class AsyncOneToManies19SQLToIterable[A, B1, B2, B3, B4, B5, B6, B7, B8, B9, B10, B11, B12, B13, B14, B15, B16, B17, B18, B19, Z](val underlying: OneToManies19SQLToIterable[A, B1, B2, B3, B4, B5, B6, B7, B8, B9, B10, B11, B12, B13, B14, B15, B16, B17, B18, B19, HasExtractor, Z])
  extends AnyVal
  with AsyncSQLToIterable[Z] {
  override def future()(implicit session: AsyncDBSession, cxt: EC = ECGlobal): Future[Iterable[Z]] = {
    session.oneToManies19Iterable(underlying.statement, underlying.rawParameters.toSeq: _*)(underlying.extractOne)(
      underlying.extractTo1, underlying.extractTo2, underlying.extractTo3, underlying.extractTo4, underlying.extractTo5, underlying.extractTo6, underlying.extractTo7, underlying.extractTo8, underlying.extractTo9, underlying.extractTo10, underlying.extractTo11, underlying.extractTo12, underlying.extractTo13, underlying.extractTo14, underlying.extractTo15, underlying.extractTo16, underlying.extractTo17, underlying.extractTo18, underlying.extractTo19)(underlying.transform)
  }
}

class AsyncOneToManies19SQLToList[A, B1, B2, B3, B4, B5, B6, B7, B8, B9, B10, B11, B12, B13, B14, B15, B16, B17, B18, B19, Z](val underlying: OneToManies19SQLToList[A, B1, B2, B3, B4, B5, B6, B7, B8, B9, B10, B11, B12, B13, B14, B15, B16, B17, B18, B19, HasExtractor, Z])
  extends AnyVal
  with AsyncSQLToList[Z] {
  override def future()(implicit session: AsyncDBSession, cxt: EC = ECGlobal): Future[List[Z]] = {
    val iterable = session.oneToManies19Iterable(underlying.statement, underlying.rawParameters.toSeq: _*)(underlying.extractOne)(
      underlying.extractTo1, underlying.extractTo2, underlying.extractTo3, underlying.extractTo4, underlying.extractTo5, underlying.extractTo6, underlying.extractTo7, underlying.extractTo8, underlying.extractTo9, underlying.extractTo10, underlying.extractTo11, underlying.extractTo12, underlying.extractTo13, underlying.extractTo14, underlying.extractTo15, underlying.extractTo16, underlying.extractTo17, underlying.extractTo18, underlying.extractTo19)(underlying.transform)
    iterable.map(_.toList)
  }
}

// -------------------
// one-to-manies 20
// -------------------

class AsyncOneToManies20SQLToOption[A, B1, B2, B3, B4, B5, B6, B7, B8, B9, B10, B11, B12, B13, B14, B15, B16, B17, B18, B19, B20, Z](val underlying: OneToManies20SQLToOption[A, B1, B2, B3, B4, B5, B6, B7, B8, B9, B10, B11, B12, B13, B14, B15, B16, B17, B18, B19, B20, HasExtractor, Z])
  extends AnyVal
  with AsyncSQLToOption[Z] {
  override def future()(implicit session: AsyncDBSession, cxt: EC = ECGlobal): Future[Option[Z]] = {
    session.oneToManies20Iterable(underlying.statement, underlying.rawParameters.toSeq: _*)(underlying.extractOne)(
      underlying.extractTo1, underlying.extractTo2, underlying.extractTo3, underlying.extractTo4, underlying.extractTo5, underlying.extractTo6, underlying.extractTo7, underlying.extractTo8, underlying.extractTo9, underlying.extractTo10, underlying.extractTo11, underlying.extractTo12, underlying.extractTo13, underlying.extractTo14, underlying.extractTo15, underlying.extractTo16, underlying.extractTo17, underlying.extractTo18, underlying.extractTo19, underlying.extractTo20)(underlying.transform).map {
        case Nil => None
        case results if results.size == 1 => results.headOption
        case results => throw new TooManyRowsException(1, results.size)
      }
  }
}

class AsyncOneToManies20SQLToIterable[A, B1, B2, B3, B4, B5, B6, B7, B8, B9, B10, B11, B12, B13, B14, B15, B16, B17, B18, B19, B20, Z](val underlying: OneToManies20SQLToIterable[A, B1, B2, B3, B4, B5, B6, B7, B8, B9, B10, B11, B12, B13, B14, B15, B16, B17, B18, B19, B20, HasExtractor, Z])
  extends AnyVal
  with AsyncSQLToIterable[Z] {
  override def future()(implicit session: AsyncDBSession, cxt: EC = ECGlobal): Future[Iterable[Z]] = {
    session.oneToManies20Iterable(underlying.statement, underlying.rawParameters.toSeq: _*)(underlying.extractOne)(
      underlying.extractTo1, underlying.extractTo2, underlying.extractTo3, underlying.extractTo4, underlying.extractTo5, underlying.extractTo6, underlying.extractTo7, underlying.extractTo8, underlying.extractTo9, underlying.extractTo10, underlying.extractTo11, underlying.extractTo12, underlying.extractTo13, underlying.extractTo14, underlying.extractTo15, underlying.extractTo16, underlying.extractTo17, underlying.extractTo18, underlying.extractTo19, underlying.extractTo20)(underlying.transform)
  }
}

class AsyncOneToManies20SQLToList[A, B1, B2, B3, B4, B5, B6, B7, B8, B9, B10, B11, B12, B13, B14, B15, B16, B17, B18, B19, B20, Z](val underlying: OneToManies20SQLToList[A, B1, B2, B3, B4, B5, B6, B7, B8, B9, B10, B11, B12, B13, B14, B15, B16, B17, B18, B19, B20, HasExtractor, Z])
  extends AnyVal
  with AsyncSQLToList[Z] {
  override def future()(implicit session: AsyncDBSession, cxt: EC = ECGlobal): Future[List[Z]] = {
    val iterable = session.oneToManies20Iterable(underlying.statement, underlying.rawParameters.toSeq: _*)(underlying.extractOne)(
      underlying.extractTo1, underlying.extractTo2, underlying.extractTo3, underlying.extractTo4, underlying.extractTo5, underlying.extractTo6, underlying.extractTo7, underlying.extractTo8, underlying.extractTo9, underlying.extractTo10, underlying.extractTo11, underlying.extractTo12, underlying.extractTo13, underlying.extractTo14, underlying.extractTo15, underlying.extractTo16, underlying.extractTo17, underlying.extractTo18, underlying.extractTo19, underlying.extractTo20)(underlying.transform)
    iterable.map(_.toList)
  }
}

// -------------------
// one-to-manies 21
// -------------------

class AsyncOneToManies21SQLToOption[A, B1, B2, B3, B4, B5, B6, B7, B8, B9, B10, B11, B12, B13, B14, B15, B16, B17, B18, B19, B20, B21, Z](val underlying: OneToManies21SQLToOption[A, B1, B2, B3, B4, B5, B6, B7, B8, B9, B10, B11, B12, B13, B14, B15, B16, B17, B18, B19, B20, B21, HasExtractor, Z])
  extends AnyVal
  with AsyncSQLToOption[Z] {
  override def future()(implicit session: AsyncDBSession, cxt: EC = ECGlobal): Future[Option[Z]] = {
    session.oneToManies21Iterable(underlying.statement, underlying.rawParameters.toSeq: _*)(underlying.extractOne)(
      underlying.extractTo1, underlying.extractTo2, underlying.extractTo3, underlying.extractTo4, underlying.extractTo5, underlying.extractTo6, underlying.extractTo7, underlying.extractTo8, underlying.extractTo9, underlying.extractTo10, underlying.extractTo11, underlying.extractTo12, underlying.extractTo13, underlying.extractTo14, underlying.extractTo15, underlying.extractTo16, underlying.extractTo17, underlying.extractTo18, underlying.extractTo19, underlying.extractTo20, underlying.extractTo21)(underlying.transform).map {
        case Nil => None
        case results if results.size == 1 => results.headOption
        case results => throw new TooManyRowsException(1, results.size)
      }
  }
}

class AsyncOneToManies21SQLToIterable[A, B1, B2, B3, B4, B5, B6, B7, B8, B9, B10, B11, B12, B13, B14, B15, B16, B17, B18, B19, B20, B21, Z](val underlying: OneToManies21SQLToIterable[A, B1, B2, B3, B4, B5, B6, B7, B8, B9, B10, B11, B12, B13, B14, B15, B16, B17, B18, B19, B20, B21, HasExtractor, Z])
  extends AnyVal
  with AsyncSQLToIterable[Z] {
  override def future()(implicit session: AsyncDBSession, cxt: EC = ECGlobal): Future[Iterable[Z]] = {
    session.oneToManies21Iterable(underlying.statement, underlying.rawParameters.toSeq: _*)(underlying.extractOne)(
      underlying.extractTo1, underlying.extractTo2, underlying.extractTo3, underlying.extractTo4, underlying.extractTo5, underlying.extractTo6, underlying.extractTo7, underlying.extractTo8, underlying.extractTo9, underlying.extractTo10, underlying.extractTo11, underlying.extractTo12, underlying.extractTo13, underlying.extractTo14, underlying.extractTo15, underlying.extractTo16, underlying.extractTo17, underlying.extractTo18, underlying.extractTo19, underlying.extractTo20, underlying.extractTo21)(underlying.transform)
  }
}

class AsyncOneToManies21SQLToList[A, B1, B2, B3, B4, B5, B6, B7, B8, B9, B10, B11, B12, B13, B14, B15, B16, B17, B18, B19, B20, B21, Z](val underlying: OneToManies21SQLToList[A, B1, B2, B3, B4, B5, B6, B7, B8, B9, B10, B11, B12, B13, B14, B15, B16, B17, B18, B19, B20, B21, HasExtractor, Z])
  extends AnyVal
  with AsyncSQLToList[Z] {
  override def future()(implicit session: AsyncDBSession, cxt: EC = ECGlobal): Future[List[Z]] = {
    val iterable = session.oneToManies21Iterable(underlying.statement, underlying.rawParameters.toSeq: _*)(underlying.extractOne)(
      underlying.extractTo1, underlying.extractTo2, underlying.extractTo3, underlying.extractTo4, underlying.extractTo5, underlying.extractTo6, underlying.extractTo7, underlying.extractTo8, underlying.extractTo9, underlying.extractTo10, underlying.extractTo11, underlying.extractTo12, underlying.extractTo13, underlying.extractTo14, underlying.extractTo15, underlying.extractTo16, underlying.extractTo17, underlying.extractTo18, underlying.extractTo19, underlying.extractTo20, underlying.extractTo21)(underlying.transform)
    iterable.map(_.toList)
  }
}
