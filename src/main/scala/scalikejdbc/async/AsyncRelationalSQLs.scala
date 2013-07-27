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

class AsyncOneToOneSQLToOption[A, B, Z](sql: OneToOneSQLToOption[A, B, HasExtractor, Z]) extends AsyncSQLToOption(sql) {
  override def future()(implicit session: AsyncDBSession, cxt: EC = ECGlobal): Future[Option[Z]] = {
    session.oneToOneTraversable(sql.statement, sql.parameters: _*)(sql.extractOne)(sql.extractTo)(sql.transform).map {
      results =>
        results match {
          case Nil => None
          case one :: Nil => Option(one)
          case _ => throw new TooManyRowsException(1, results.size)
        }
    }
  }
}

class AsyncOneToOneSQLToTraversable[A, B, Z](sql: OneToOneSQLToTraversable[A, B, HasExtractor, Z]) extends AsyncSQLToTraversable(sql) {
  override def future()(implicit session: AsyncDBSession,
    cxt: EC = ECGlobal): Future[Traversable[Z]] = {
    val traversable = session.oneToOneTraversable(sql.statement, sql.parameters: _*)(sql.extractOne)(sql.extractTo)(sql.transform)
    traversable.asInstanceOf[Future[Traversable[Z]]]
  }
}

class AsyncOneToOneSQLToList[A, B, Z](sql: OneToOneSQLToList[A, B, HasExtractor, Z]) extends AsyncSQLToList(sql) {
  override def future()(implicit session: AsyncDBSession, cxt: EC = ECGlobal): Future[List[Z]] = {
    val traversable = session.oneToOneTraversable(sql.statement, sql.parameters: _*)(sql.extractOne)(sql.extractTo)(sql.transform)
    traversable.map(_.toList)
  }
}
// -------------------
// one-to-many
// -------------------

class AsyncOneToManySQLToOption[A, B, Z](sql: OneToManySQLToOption[A, B, HasExtractor, Z]) extends AsyncSQLToOption(sql) {
  override def future()(implicit session: AsyncDBSession, cxt: EC = ECGlobal): Future[Option[Z]] = {
    session.oneToManyTraversable(sql.statement, sql.parameters: _*)(sql.extractOne)(sql.extractTo)(sql.transform).map {
      results =>
        results match {
          case Nil => None
          case one :: Nil => Option(one)
          case _ => throw new TooManyRowsException(1, results.size)
        }
    }
  }
}

class AsyncOneToManySQLToTraversable[A, B, Z](sql: OneToManySQLToTraversable[A, B, HasExtractor, Z]) extends AsyncSQLToTraversable(sql) {
  override def future()(implicit session: AsyncDBSession,
    cxt: EC = ECGlobal): Future[Traversable[Z]] = {
    session.oneToManyTraversable(sql.statement, sql.parameters: _*)(sql.extractOne)(sql.extractTo)(sql.transform)
  }
}

class AsyncOneToManySQLToList[A, B, Z](sql: OneToManySQLToList[A, B, HasExtractor, Z]) extends AsyncSQLToList(sql) {
  override def future()(implicit session: AsyncDBSession, cxt: EC = ECGlobal): Future[List[Z]] = {
    val traversable = session.oneToManyTraversable(sql.statement, sql.parameters: _*)(sql.extractOne)(sql.extractTo)(sql.transform)
    traversable.map(_.toList)
  }
}

// -------------------
// one-to-manies 2
// -------------------

class AsyncOneToManies2SQLToOption[A, B1, B2, Z](sql: OneToManies2SQLToOption[A, B1, B2, HasExtractor, Z]) extends AsyncSQLToOption(sql) {
  override def future()(implicit session: AsyncDBSession, cxt: EC = ECGlobal): Future[Option[Z]] = {
    session.oneToManies2Traversable(sql.statement, sql.parameters: _*)(sql.extractOne)(sql.extractTo1, sql.extractTo2)(sql.transform).map {
      results =>
        results match {
          case Nil => None
          case one :: Nil => Option(one)
          case _ => throw new TooManyRowsException(1, results.size)
        }
    }
  }
}

class AsyncOneToManies2SQLToTraversable[A, B1, B2, Z](sql: OneToManies2SQLToTraversable[A, B1, B2, HasExtractor, Z]) extends AsyncSQLToTraversable(sql) {
  override def future()(implicit session: AsyncDBSession, cxt: EC = ECGlobal): Future[Traversable[Z]] = {
    val traversable = session.oneToManies2Traversable(sql.statement, sql.parameters: _*)(sql.extractOne)(sql.extractTo1, sql.extractTo2)(sql.transform)
    traversable
  }
}

class AsyncOneToManies2SQLToList[A, B1, B2, Z](sql: OneToManies2SQLToList[A, B1, B2, HasExtractor, Z]) extends AsyncSQLToList(sql) {
  override def future()(implicit session: AsyncDBSession, cxt: EC = ECGlobal): Future[List[Z]] = {
    val traversable = session.oneToManies2Traversable(sql.statement, sql.parameters: _*)(sql.extractOne)(sql.extractTo1, sql.extractTo2)(sql.transform)
    traversable.map(_.toList)
  }
}

// -------------------
// one-to-manies 3
// -------------------

class AsyncOneToManies3SQLToOption[A, B1, B2, B3, Z](sql: OneToManies3SQLToOption[A, B1, B2, B3, HasExtractor, Z]) extends AsyncSQLToOption(sql) {
  override def future()(implicit session: AsyncDBSession, cxt: EC = ECGlobal): Future[Option[Z]] = {
    session.oneToManies3Traversable(sql.statement, sql.parameters: _*)(sql.extractOne)(
      sql.extractTo1, sql.extractTo2, sql.extractTo3)(sql.transform).map {
        results =>
          results match {
            case Nil => None
            case one :: Nil => Option(one)
            case _ => throw new TooManyRowsException(1, results.size)
          }
      }
  }
}

class AsyncOneToManies3SQLToTraversable[A, B1, B2, B3, Z](sql: OneToManies3SQLToTraversable[A, B1, B2, B3, HasExtractor, Z]) extends AsyncSQLToTraversable(sql) {
  override def future()(implicit session: AsyncDBSession, cxt: EC = ECGlobal): Future[Traversable[Z]] = {
    session.oneToManies3Traversable(sql.statement, sql.parameters: _*)(sql.extractOne)(
      sql.extractTo1, sql.extractTo2, sql.extractTo3)(sql.transform)
  }
}

class AsyncOneToManies3SQLToList[A, B1, B2, B3, Z](sql: OneToManies3SQLToList[A, B1, B2, B3, HasExtractor, Z]) extends AsyncSQLToList(sql) {
  override def future()(implicit session: AsyncDBSession, cxt: EC = ECGlobal): Future[List[Z]] = {
    val traversable = session.oneToManies3Traversable(sql.statement, sql.parameters: _*)(sql.extractOne)(
      sql.extractTo1, sql.extractTo2, sql.extractTo3)(sql.transform)
    traversable.map(_.toList)
  }
}

// -------------------
// one-to-manies 4
// -------------------

class AsyncOneToManies4SQLToOption[A, B1, B2, B3, B4, Z](sql: OneToManies4SQLToOption[A, B1, B2, B3, B4, HasExtractor, Z]) extends AsyncSQLToOption(sql) {
  override def future()(implicit session: AsyncDBSession, cxt: EC = ECGlobal): Future[Option[Z]] = {
    session.oneToManies4Traversable(sql.statement, sql.parameters: _*)(sql.extractOne)(
      sql.extractTo1, sql.extractTo2, sql.extractTo3, sql.extractTo4)(sql.transform).map {
        results =>
          results match {
            case Nil => None
            case one :: Nil => Option(one)
            case _ => throw new TooManyRowsException(1, results.size)
          }
      }
  }
}

class AsyncOneToManies4SQLToTraversable[A, B1, B2, B3, B4, Z](sql: OneToManies4SQLToTraversable[A, B1, B2, B3, B4, HasExtractor, Z]) extends AsyncSQLToTraversable(sql) {
  override def future()(implicit session: AsyncDBSession, cxt: EC = ECGlobal): Future[Traversable[Z]] = {
    session.oneToManies4Traversable(sql.statement, sql.parameters: _*)(sql.extractOne)(
      sql.extractTo1, sql.extractTo2, sql.extractTo3, sql.extractTo4)(sql.transform)
  }
}

class AsyncOneToManies4SQLToList[A, B1, B2, B3, B4, Z](sql: OneToManies4SQLToList[A, B1, B2, B3, B4, HasExtractor, Z]) extends AsyncSQLToList(sql) {
  override def future()(implicit session: AsyncDBSession, cxt: EC = ECGlobal): Future[List[Z]] = {
    val traversable = session.oneToManies4Traversable(sql.statement, sql.parameters: _*)(sql.extractOne)(
      sql.extractTo1, sql.extractTo2, sql.extractTo3, sql.extractTo4)(sql.transform)
    traversable.map(_.toList)
  }
}

// -------------------
// one-to-manies 5
// -------------------

class AsyncOneToManies5SQLToOption[A, B1, B2, B3, B4, B5, Z](sql: OneToManies5SQLToOption[A, B1, B2, B3, B4, B5, HasExtractor, Z]) extends AsyncSQLToOption(sql) {
  override def future()(implicit session: AsyncDBSession, cxt: EC = ECGlobal): Future[Option[Z]] = {
    session.oneToManies5Traversable(sql.statement, sql.parameters: _*)(sql.extractOne)(
      sql.extractTo1, sql.extractTo2, sql.extractTo3, sql.extractTo4, sql.extractTo5)(sql.transform).map {
        results =>
          results match {
            case Nil => None
            case one :: Nil => Option(one)
            case _ => throw new TooManyRowsException(1, results.size)
          }
      }
  }
}

class AsyncOneToManies5SQLToTraversable[A, B1, B2, B3, B4, B5, Z](sql: OneToManies5SQLToTraversable[A, B1, B2, B3, B4, B5, HasExtractor, Z]) extends AsyncSQLToTraversable(sql) {
  override def future()(implicit session: AsyncDBSession, cxt: EC = ECGlobal): Future[Traversable[Z]] = {
    session.oneToManies5Traversable(sql.statement, sql.parameters: _*)(sql.extractOne)(
      sql.extractTo1, sql.extractTo2, sql.extractTo3, sql.extractTo4, sql.extractTo5)(sql.transform)
  }
}

class AsyncOneToManies5SQLToList[A, B1, B2, B3, B4, B5, Z](sql: OneToManies5SQLToList[A, B1, B2, B3, B4, B5, HasExtractor, Z]) extends AsyncSQLToList(sql) {
  override def future()(implicit session: AsyncDBSession, cxt: EC = ECGlobal): Future[List[Z]] = {
    val traversable = session.oneToManies5Traversable(sql.statement, sql.parameters: _*)(sql.extractOne)(
      sql.extractTo1, sql.extractTo2, sql.extractTo3, sql.extractTo4, sql.extractTo5)(sql.transform)
    traversable.map(_.toList)
  }
}
