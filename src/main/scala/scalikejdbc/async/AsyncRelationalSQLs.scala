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
    session.oneToOneTraversable(underlying.statement, underlying.parameters: _*)(underlying.extractOne)(underlying.extractTo)(underlying.transform).map {
      results =>
        results match {
          case Nil => None
          case one :: Nil => Option(one)
          case _ => throw new TooManyRowsException(1, results.size)
        }
    }
  }
}

class AsyncOneToOneSQLToTraversable[A, B, Z](val underlying: OneToOneSQLToTraversable[A, B, HasExtractor, Z])
    extends AnyVal
    with AsyncSQLToTraversable[Z] {
  override def future()(implicit session: AsyncDBSession,
    cxt: EC = ECGlobal): Future[Traversable[Z]] = {
    val traversable = session.oneToOneTraversable(underlying.statement, underlying.parameters: _*)(underlying.extractOne)(underlying.extractTo)(underlying.transform)
    traversable.asInstanceOf[Future[Traversable[Z]]]
  }
}

class AsyncOneToOneSQLToList[A, B, Z](val underlying: OneToOneSQLToList[A, B, HasExtractor, Z])
    extends AnyVal
    with AsyncSQLToList[Z] {
  override def future()(implicit session: AsyncDBSession, cxt: EC = ECGlobal): Future[List[Z]] = {
    val traversable = session.oneToOneTraversable(underlying.statement, underlying.parameters: _*)(underlying.extractOne)(underlying.extractTo)(underlying.transform)
    traversable.map(_.toList)
  }
}
// -------------------
// one-to-many
// -------------------

class AsyncOneToManySQLToOption[A, B, Z](val underlying: OneToManySQLToOption[A, B, HasExtractor, Z])
    extends AnyVal
    with AsyncSQLToOption[Z] {
  override def future()(implicit session: AsyncDBSession, cxt: EC = ECGlobal): Future[Option[Z]] = {
    session.oneToManyTraversable(underlying.statement, underlying.parameters: _*)(underlying.extractOne)(underlying.extractTo)(underlying.transform).map {
      results =>
        results match {
          case Nil => None
          case one :: Nil => Option(one)
          case _ => throw new TooManyRowsException(1, results.size)
        }
    }
  }
}

class AsyncOneToManySQLToTraversable[A, B, Z](val underlying: OneToManySQLToTraversable[A, B, HasExtractor, Z])
    extends AnyVal
    with AsyncSQLToTraversable[Z] {
  override def future()(implicit session: AsyncDBSession,
    cxt: EC = ECGlobal): Future[Traversable[Z]] = {
    session.oneToManyTraversable(underlying.statement, underlying.parameters: _*)(underlying.extractOne)(underlying.extractTo)(underlying.transform)
  }
}

class AsyncOneToManySQLToList[A, B, Z](val underlying: OneToManySQLToList[A, B, HasExtractor, Z])
    extends AnyVal
    with AsyncSQLToList[Z] {
  override def future()(implicit session: AsyncDBSession, cxt: EC = ECGlobal): Future[List[Z]] = {
    val traversable = session.oneToManyTraversable(underlying.statement, underlying.parameters: _*)(underlying.extractOne)(underlying.extractTo)(underlying.transform)
    traversable.map(_.toList)
  }
}

// -------------------
// one-to-manies 2
// -------------------

class AsyncOneToManies2SQLToOption[A, B1, B2, Z](val underlying: OneToManies2SQLToOption[A, B1, B2, HasExtractor, Z])
    extends AnyVal
    with AsyncSQLToOption[Z] {
  override def future()(implicit session: AsyncDBSession, cxt: EC = ECGlobal): Future[Option[Z]] = {
    session.oneToManies2Traversable(underlying.statement, underlying.parameters: _*)(underlying.extractOne)(underlying.extractTo1, underlying.extractTo2)(underlying.transform).map {
      results =>
        results match {
          case Nil => None
          case one :: Nil => Option(one)
          case _ => throw new TooManyRowsException(1, results.size)
        }
    }
  }
}

class AsyncOneToManies2SQLToTraversable[A, B1, B2, Z](val underlying: OneToManies2SQLToTraversable[A, B1, B2, HasExtractor, Z])
    extends AnyVal
    with AsyncSQLToTraversable[Z] {
  override def future()(implicit session: AsyncDBSession, cxt: EC = ECGlobal): Future[Traversable[Z]] = {
    val traversable = session.oneToManies2Traversable(underlying.statement, underlying.parameters: _*)(underlying.extractOne)(underlying.extractTo1, underlying.extractTo2)(underlying.transform)
    traversable
  }
}

class AsyncOneToManies2SQLToList[A, B1, B2, Z](val underlying: OneToManies2SQLToList[A, B1, B2, HasExtractor, Z])
    extends AnyVal
    with AsyncSQLToList[Z] {
  override def future()(implicit session: AsyncDBSession, cxt: EC = ECGlobal): Future[List[Z]] = {
    val traversable = session.oneToManies2Traversable(underlying.statement, underlying.parameters: _*)(underlying.extractOne)(underlying.extractTo1, underlying.extractTo2)(underlying.transform)
    traversable.map(_.toList)
  }
}

// -------------------
// one-to-manies 3
// -------------------

class AsyncOneToManies3SQLToOption[A, B1, B2, B3, Z](val underlying: OneToManies3SQLToOption[A, B1, B2, B3, HasExtractor, Z])
    extends AnyVal
    with AsyncSQLToOption[Z] {
  override def future()(implicit session: AsyncDBSession, cxt: EC = ECGlobal): Future[Option[Z]] = {
    session.oneToManies3Traversable(underlying.statement, underlying.parameters: _*)(underlying.extractOne)(
      underlying.extractTo1, underlying.extractTo2, underlying.extractTo3)(underlying.transform).map {
        results =>
          results match {
            case Nil => None
            case one :: Nil => Option(one)
            case _ => throw new TooManyRowsException(1, results.size)
          }
      }
  }
}

class AsyncOneToManies3SQLToTraversable[A, B1, B2, B3, Z](val underlying: OneToManies3SQLToTraversable[A, B1, B2, B3, HasExtractor, Z])
    extends AnyVal
    with AsyncSQLToTraversable[Z] {
  override def future()(implicit session: AsyncDBSession, cxt: EC = ECGlobal): Future[Traversable[Z]] = {
    session.oneToManies3Traversable(underlying.statement, underlying.parameters: _*)(underlying.extractOne)(
      underlying.extractTo1, underlying.extractTo2, underlying.extractTo3)(underlying.transform)
  }
}

class AsyncOneToManies3SQLToList[A, B1, B2, B3, Z](val underlying: OneToManies3SQLToList[A, B1, B2, B3, HasExtractor, Z])
    extends AnyVal
    with AsyncSQLToList[Z] {
  override def future()(implicit session: AsyncDBSession, cxt: EC = ECGlobal): Future[List[Z]] = {
    val traversable = session.oneToManies3Traversable(underlying.statement, underlying.parameters: _*)(underlying.extractOne)(
      underlying.extractTo1, underlying.extractTo2, underlying.extractTo3)(underlying.transform)
    traversable.map(_.toList)
  }
}

// -------------------
// one-to-manies 4
// -------------------

class AsyncOneToManies4SQLToOption[A, B1, B2, B3, B4, Z](val underlying: OneToManies4SQLToOption[A, B1, B2, B3, B4, HasExtractor, Z])
    extends AnyVal
    with AsyncSQLToOption[Z] {
  override def future()(implicit session: AsyncDBSession, cxt: EC = ECGlobal): Future[Option[Z]] = {
    session.oneToManies4Traversable(underlying.statement, underlying.parameters: _*)(underlying.extractOne)(
      underlying.extractTo1, underlying.extractTo2, underlying.extractTo3, underlying.extractTo4)(underlying.transform).map {
        results =>
          results match {
            case Nil => None
            case one :: Nil => Option(one)
            case _ => throw new TooManyRowsException(1, results.size)
          }
      }
  }
}

class AsyncOneToManies4SQLToTraversable[A, B1, B2, B3, B4, Z](val underlying: OneToManies4SQLToTraversable[A, B1, B2, B3, B4, HasExtractor, Z])
    extends AnyVal
    with AsyncSQLToTraversable[Z] {
  override def future()(implicit session: AsyncDBSession, cxt: EC = ECGlobal): Future[Traversable[Z]] = {
    session.oneToManies4Traversable(underlying.statement, underlying.parameters: _*)(underlying.extractOne)(
      underlying.extractTo1, underlying.extractTo2, underlying.extractTo3, underlying.extractTo4)(underlying.transform)
  }
}

class AsyncOneToManies4SQLToList[A, B1, B2, B3, B4, Z](val underlying: OneToManies4SQLToList[A, B1, B2, B3, B4, HasExtractor, Z])
    extends AnyVal
    with AsyncSQLToList[Z] {
  override def future()(implicit session: AsyncDBSession, cxt: EC = ECGlobal): Future[List[Z]] = {
    val traversable = session.oneToManies4Traversable(underlying.statement, underlying.parameters: _*)(underlying.extractOne)(
      underlying.extractTo1, underlying.extractTo2, underlying.extractTo3, underlying.extractTo4)(underlying.transform)
    traversable.map(_.toList)
  }
}

// -------------------
// one-to-manies 5
// -------------------

class AsyncOneToManies5SQLToOption[A, B1, B2, B3, B4, B5, Z](val underlying: OneToManies5SQLToOption[A, B1, B2, B3, B4, B5, HasExtractor, Z])
    extends AnyVal
    with AsyncSQLToOption[Z] {
  override def future()(implicit session: AsyncDBSession, cxt: EC = ECGlobal): Future[Option[Z]] = {
    session.oneToManies5Traversable(underlying.statement, underlying.parameters: _*)(underlying.extractOne)(
      underlying.extractTo1, underlying.extractTo2, underlying.extractTo3, underlying.extractTo4, underlying.extractTo5)(underlying.transform).map {
        results =>
          results match {
            case Nil => None
            case one :: Nil => Option(one)
            case _ => throw new TooManyRowsException(1, results.size)
          }
      }
  }
}

class AsyncOneToManies5SQLToTraversable[A, B1, B2, B3, B4, B5, Z](val underlying: OneToManies5SQLToTraversable[A, B1, B2, B3, B4, B5, HasExtractor, Z])
    extends AnyVal
    with AsyncSQLToTraversable[Z] {
  override def future()(implicit session: AsyncDBSession, cxt: EC = ECGlobal): Future[Traversable[Z]] = {
    session.oneToManies5Traversable(underlying.statement, underlying.parameters: _*)(underlying.extractOne)(
      underlying.extractTo1, underlying.extractTo2, underlying.extractTo3, underlying.extractTo4, underlying.extractTo5)(underlying.transform)
  }
}

class AsyncOneToManies5SQLToList[A, B1, B2, B3, B4, B5, Z](val underlying: OneToManies5SQLToList[A, B1, B2, B3, B4, B5, HasExtractor, Z])
    extends AnyVal
    with AsyncSQLToList[Z] {
  override def future()(implicit session: AsyncDBSession, cxt: EC = ECGlobal): Future[List[Z]] = {
    val traversable = session.oneToManies5Traversable(underlying.statement, underlying.parameters: _*)(underlying.extractOne)(
      underlying.extractTo1, underlying.extractTo2, underlying.extractTo3, underlying.extractTo4, underlying.extractTo5)(underlying.transform)
    traversable.map(_.toList)
  }
}
