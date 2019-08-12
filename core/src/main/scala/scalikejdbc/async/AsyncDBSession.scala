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

import scala.concurrent._
import scalikejdbc._
import scalikejdbc.GlobalSettings._
import scala.collection.mutable.LinkedHashMap
import scala.util.{ Failure, Success }
import scalikejdbc.async.ShortenedNames._
import scalikejdbc.async.internal.MockPreparedStatement

/**
 * Asynchronous DB Session
 */
trait AsyncDBSession extends LogSupport {

  val connection: AsyncConnection

  def execute(statement: String, parameters: Any*)(implicit cxt: EC = ECGlobal): Future[Boolean] = {
    val _parameters = ensureAndNormalizeParameters(parameters)
    withListeners(statement, _parameters) {
      queryLogging(statement, _parameters)
      connection.sendPreparedStatement(statement, _parameters: _*).map { result =>
        result.rowsAffected.exists(_ > 0)
      }
    }
  }

  def update(statement: String, parameters: Any*)(implicit cxt: EC = ECGlobal): Future[Int] = {
    val _parameters = ensureAndNormalizeParameters(parameters)
    withListeners(statement, _parameters) {
      queryLogging(statement, _parameters)
      val statementResultF =
        if (connection.isShared) {
          // create local transaction if current session is not transactional
          connection.toNonSharedConnection().flatMap { conn =>
            AsyncTx.inTransaction(TxAsyncDBSession(conn), { tx: TxAsyncDBSession =>
              tx.connection.sendPreparedStatement(statement, _parameters: _*)
            })
          }
        } else {
          connection.sendPreparedStatement(statement, _parameters: _*)
        }
      // Process statement result
      statementResultF.map { result =>
        result.rowsAffected.map(_.toInt).getOrElse(0)
      }
    }
  }

  def updateAndReturnGeneratedKey(statement: String, parameters: Any*)(implicit cxt: EC = ECGlobal): Future[Long] = {
    def readGeneratedKey(result: AsyncQueryResult): Future[Long] = {
      result.generatedKey.map(_.getOrElse {
        throw new IllegalArgumentException(ErrorMessage.FAILED_TO_RETRIEVE_GENERATED_KEY + " SQL: '" + statement + "'")
      })
    }
    val _parameters = ensureAndNormalizeParameters(parameters)
    withListeners(statement, _parameters) {
      queryLogging(statement, _parameters)
      if (connection.isShared) {
        // create local transaction if current session is not transactional
        connection.toNonSharedConnection().flatMap { conn =>
          AsyncTx.inTransaction(TxAsyncDBSession(conn), { tx: TxAsyncDBSession =>
            tx.connection.sendPreparedStatement(statement, _parameters: _*).flatMap(readGeneratedKey)
          })
        }
      } else {
        connection.sendPreparedStatement(statement, _parameters: _*).flatMap(readGeneratedKey)
      }
    }
  }

  @deprecated("will be removed. use iterable", "0.12.0")
  def traversable[A](statement: String, parameters: Any*)(extractor: WrappedResultSet => A)(implicit cxt: EC = ECGlobal): Future[Iterable[A]] =
    iterable[A](statement, parameters: _*)(extractor)

  def iterable[A](statement: String, parameters: Any*)(extractor: WrappedResultSet => A)(implicit cxt: EC = ECGlobal): Future[Iterable[A]] = {
    val _parameters = ensureAndNormalizeParameters(parameters)
    withListeners(statement, _parameters) {
      queryLogging(statement, _parameters)
      connection.sendPreparedStatement(statement, _parameters: _*).map { result =>
        result.rows.map { ars =>
          new AsyncResultSetIterator(ars).map(extractor).toList
        }.getOrElse(Nil)
      }
    }
  }

  def single[A](statement: String, parameters: Any*)(extractor: WrappedResultSet => A)(
    implicit
    cxt: EC = ECGlobal): Future[Option[A]] = {
    val _parameters = ensureAndNormalizeParameters(parameters)
    iterable(statement, _parameters: _*)(extractor).map {
      case Nil => None
      case one :: Nil => Option(one)
      case results => throw TooManyRowsException(1, results.size)
    }
  }

  def list[A](statement: String, parameters: Any*)(extractor: WrappedResultSet => A)(implicit cxt: EC = ECGlobal): Future[List[A]] = {
    val _parameters = ensureAndNormalizeParameters(parameters)
    iterable[A](statement, _parameters: _*)(extractor).map(_.toList)
  }

  @deprecated("will be removed. use oneToOneIterable", "0.12.0")
  def oneToOneTraversable[A, B, Z](statement: String, parameters: Any*)(extractOne: WrappedResultSet => A)(extractTo: WrappedResultSet => Option[B])(transform: (A, B) => Z)(
    implicit
    cxt: EC = ECGlobal): Future[Iterable[Z]] =
    oneToOneIterable[A, B, Z](statement, parameters: _*)(extractOne)(extractTo)(transform)

  def oneToOneIterable[A, B, Z](statement: String, parameters: Any*)(extractOne: WrappedResultSet => A)(extractTo: WrappedResultSet => Option[B])(transform: (A, B) => Z)(
    implicit
    cxt: EC = ECGlobal): Future[Iterable[Z]] = {
    val _parameters = ensureAndNormalizeParameters(parameters)
    withListeners(statement, _parameters) {
      queryLogging(statement, _parameters)

      def processResultSet(oneToOne: LinkedHashMap[A, Option[B]], rs: WrappedResultSet): LinkedHashMap[A, Option[B]] = {
        val o = extractOne(rs)
        oneToOne.keys.find(_ == o) match {
          case Some(_) => throw IllegalRelationshipException(ErrorMessage.INVALID_ONE_TO_ONE_RELATION)
          case _ => oneToOne += (o -> extractTo(rs))
        }
      }
      connection.sendPreparedStatement(statement, _parameters: _*).map { result =>
        result.rows.map { ars =>
          new AsyncResultSetIterator(ars).foldLeft(LinkedHashMap[A, Option[B]]())(processResultSet).map {
            case (one, Some(to)) => transform(one, to)
            case (one, None) => one.asInstanceOf[Z]
          }
        }.getOrElse(Nil)
      }
    }
  }

  @deprecated("will be removed. use oneToManyIterable", "0.12.0")
  def oneToManyTraversable[A, B, Z](statement: String, parameters: Any*)(
    extractOne: WrappedResultSet => A)(
    extractTo: WrappedResultSet => Option[B])(
    transform: (A, Seq[B]) => Z)(
    implicit
    cxt: EC = ECGlobal): Future[Iterable[Z]] =
    oneToManyIterable[A, B, Z](statement, parameters: _*)(extractOne)(extractTo)(transform)

  def oneToManyIterable[A, B, Z](statement: String, parameters: Any*)(
    extractOne: WrappedResultSet => A)(
    extractTo: WrappedResultSet => Option[B])(
    transform: (A, Seq[B]) => Z)(
    implicit
    cxt: EC = ECGlobal): Future[Iterable[Z]] = {
    val _parameters = ensureAndNormalizeParameters(parameters)
    withListeners(statement, _parameters) {
      queryLogging(statement, _parameters)

      def processResultSet(oneToMany: LinkedHashMap[A, Seq[B]], rs: WrappedResultSet): LinkedHashMap[A, Seq[B]] = {
        val o = extractOne(rs)
        oneToMany.keys.find(_ == o).map { _ =>
          extractTo(rs).map(many => oneToMany += (o -> (oneToMany.apply(o) :+ many))).getOrElse(oneToMany)
        }.getOrElse {
          oneToMany += (o -> extractTo(rs).map(many => Vector(many)).getOrElse(Nil))
        }
      }
      connection.sendPreparedStatement(statement, _parameters: _*).map { result =>
        result.rows.map { ars =>
          new AsyncResultSetIterator(ars).foldLeft(LinkedHashMap[A, Seq[B]]())(processResultSet).map {
            case (one, to) => transform(one, to)
          }
        }.getOrElse(Nil)
      }
    }
  }

  @deprecated("will be removed. use oneToManies2Iterable", "0.12.0")
  def oneToManies2Traversable[A, B1, B2, Z](statement: String, parameters: Any*)(
    extractOne: WrappedResultSet => A)(
    extractTo1: WrappedResultSet => Option[B1],
    extractTo2: WrappedResultSet => Option[B2])(
    transform: (A, Seq[B1], Seq[B2]) => Z)(
    implicit
    cxt: EC = ECGlobal): Future[Iterable[Z]] =
    oneToManies2Iterable[A, B1, B2, Z](statement, parameters: _*)(extractOne)(extractTo1, extractTo2)(transform)

  def oneToManies2Iterable[A, B1, B2, Z](statement: String, parameters: Any*)(
    extractOne: WrappedResultSet => A)(
    extractTo1: WrappedResultSet => Option[B1],
    extractTo2: WrappedResultSet => Option[B2])(
    transform: (A, Seq[B1], Seq[B2]) => Z)(
    implicit
    cxt: EC = ECGlobal): Future[Iterable[Z]] = {
    val _parameters = ensureAndNormalizeParameters(parameters)
    withListeners(statement, _parameters) {
      queryLogging(statement, _parameters)

      def processResultSet(result: LinkedHashMap[A, (Seq[B1], Seq[B2])], rs: WrappedResultSet): LinkedHashMap[A, (Seq[B1], Seq[B2])] = {
        val o = extractOne(rs)
        val (to1, to2) = (extractTo1(rs), extractTo2(rs))
        result.keys.find(_ == o).map { _ =>
          to1.orElse(to2).map { _ =>
            val (ts1, ts2) = result.apply(o)
            result += (o -> (
              to1.map(t => if (ts1.contains(t)) ts1 else ts1 :+ t).getOrElse(ts1),
              to2.map(t => if (ts2.contains(t)) ts2 else ts2 :+ t).getOrElse(ts2)))
          }.getOrElse(result)
        }.getOrElse {
          result += (o -> (to1.map(t => Vector(t)).getOrElse(Vector.empty), to2.map(t => Vector(t)).getOrElse(Vector.empty)))
        }
      }
      connection.sendPreparedStatement(statement, _parameters: _*).map { result =>
        result.rows.map { ars =>
          new AsyncResultSetIterator(ars).foldLeft(LinkedHashMap[A, (Seq[B1], Seq[B2])]())(processResultSet).map {
            case (one, (t1, t2)) => transform(one, t1, t2)
          }
        }.getOrElse(Nil)
      }
    }
  }

  @deprecated("will be removed. use oneToManies3Iterable", "0.12.0")
  def oneToManies3Traversable[A, B1, B2, B3, Z](statement: String, parameters: Any*)(
    extractOne: WrappedResultSet => A)(
    extractTo1: WrappedResultSet => Option[B1],
    extractTo2: WrappedResultSet => Option[B2],
    extractTo3: WrappedResultSet => Option[B3])(
    transform: (A, Seq[B1], Seq[B2], Seq[B3]) => Z)(
    implicit
    cxt: EC = ECGlobal): Future[Iterable[Z]] =
    oneToManies3Iterable[A, B1, B2, B3, Z](statement, parameters: _*)(extractOne)(extractTo1, extractTo2, extractTo3)(transform)

  def oneToManies3Iterable[A, B1, B2, B3, Z](statement: String, parameters: Any*)(
    extractOne: WrappedResultSet => A)(
    extractTo1: WrappedResultSet => Option[B1],
    extractTo2: WrappedResultSet => Option[B2],
    extractTo3: WrappedResultSet => Option[B3])(
    transform: (A, Seq[B1], Seq[B2], Seq[B3]) => Z)(
    implicit
    cxt: EC = ECGlobal): Future[Iterable[Z]] = {
    val _parameters = ensureAndNormalizeParameters(parameters)
    withListeners(statement, _parameters) {
      queryLogging(statement, _parameters)

      def processResultSet(result: LinkedHashMap[A, (Seq[B1], Seq[B2], Seq[B3])], rs: WrappedResultSet): LinkedHashMap[A, (Seq[B1], Seq[B2], Seq[B3])] = {
        val o = extractOne(rs)
        val (to1, to2, to3) = (extractTo1(rs), extractTo2(rs), extractTo3(rs))
        result.keys.find(_ == o).map { _ =>
          to1.orElse(to2).orElse(to3).map { _ =>
            val (ts1, ts2, ts3) = result.apply(o)
            result += (o -> (
              to1.map(t => if (ts1.contains(t)) ts1 else ts1 :+ t).getOrElse(ts1),
              to2.map(t => if (ts2.contains(t)) ts2 else ts2 :+ t).getOrElse(ts2),
              to3.map(t => if (ts3.contains(t)) ts3 else ts3 :+ t).getOrElse(ts3)))
          }.getOrElse(result)
        }.getOrElse {
          result += (
            o -> (
              to1.map(t => Vector(t)).getOrElse(Vector.empty),
              to2.map(t => Vector(t)).getOrElse(Vector.empty),
              to3.map(t => Vector(t)).getOrElse(Vector.empty)))
        }
      }
      connection.sendPreparedStatement(statement, _parameters: _*).map { result =>
        result.rows.map { ars =>
          new AsyncResultSetIterator(ars).foldLeft(LinkedHashMap[A, (Seq[B1], Seq[B2], Seq[B3])]())(processResultSet).map {
            case (one, (t1, t2, t3)) => transform(one, t1, t2, t3)
          }
        }.getOrElse(Nil)
      }
    }
  }

  @deprecated("will be removed. use oneToManies4Iterable", "0.12.0")
  def oneToManies4Traversable[A, B1, B2, B3, B4, Z](statement: String, parameters: Any*)(
    extractOne: WrappedResultSet => A)(
    extractTo1: WrappedResultSet => Option[B1],
    extractTo2: WrappedResultSet => Option[B2],
    extractTo3: WrappedResultSet => Option[B3],
    extractTo4: WrappedResultSet => Option[B4])(
    transform: (A, Seq[B1], Seq[B2], Seq[B3], Seq[B4]) => Z)(
    implicit
    cxt: EC = ECGlobal): Future[Iterable[Z]] =
    oneToManies4Iterable[A, B1, B2, B3, B4, Z](statement, parameters: _*)(extractOne)(extractTo1, extractTo2, extractTo3, extractTo4)(transform)

  def oneToManies4Iterable[A, B1, B2, B3, B4, Z](statement: String, parameters: Any*)(
    extractOne: WrappedResultSet => A)(
    extractTo1: WrappedResultSet => Option[B1],
    extractTo2: WrappedResultSet => Option[B2],
    extractTo3: WrappedResultSet => Option[B3],
    extractTo4: WrappedResultSet => Option[B4])(
    transform: (A, Seq[B1], Seq[B2], Seq[B3], Seq[B4]) => Z)(
    implicit
    cxt: EC = ECGlobal): Future[Iterable[Z]] = {
    val _parameters = ensureAndNormalizeParameters(parameters)
    withListeners(statement, _parameters) {
      queryLogging(statement, _parameters)

      def processResultSet(result: LinkedHashMap[A, (Seq[B1], Seq[B2], Seq[B3], Seq[B4])], rs: WrappedResultSet): LinkedHashMap[A, (Seq[B1], Seq[B2], Seq[B3], Seq[B4])] = {
        val o = extractOne(rs)
        val (to1, to2, to3, to4) = (extractTo1(rs), extractTo2(rs), extractTo3(rs), extractTo4(rs))
        result.keys.find(_ == o).map { _ =>
          to1.orElse(to2).orElse(to3).orElse(to4).map { _ =>
            val (ts1, ts2, ts3, ts4) = result.apply(o)
            result += (o -> (
              to1.map(t => if (ts1.contains(t)) ts1 else ts1 :+ t).getOrElse(ts1),
              to2.map(t => if (ts2.contains(t)) ts2 else ts2 :+ t).getOrElse(ts2),
              to3.map(t => if (ts3.contains(t)) ts3 else ts3 :+ t).getOrElse(ts3),
              to4.map(t => if (ts4.contains(t)) ts4 else ts4 :+ t).getOrElse(ts4)))
          }.getOrElse(result)
        }.getOrElse {
          result += (
            o -> (
              to1.map(t => Vector(t)).getOrElse(Vector.empty),
              to2.map(t => Vector(t)).getOrElse(Vector.empty),
              to3.map(t => Vector(t)).getOrElse(Vector.empty),
              to4.map(t => Vector(t)).getOrElse(Vector.empty)))
        }
      }
      connection.sendPreparedStatement(statement, _parameters: _*).map { result =>
        result.rows.map { ars =>
          new AsyncResultSetIterator(ars).foldLeft(
            LinkedHashMap[A, (Seq[B1], Seq[B2], Seq[B3], Seq[B4])]())(processResultSet).map {
              case (one, (t1, t2, t3, t4)) => transform(one, t1, t2, t3, t4)
            }
        }.getOrElse(Nil)
      }
    }
  }

  @deprecated("will be removed. use oneToManies5Iterable", "0.12.0")
  def oneToManies5Traversable[A, B1, B2, B3, B4, B5, Z](
    statement: String,
    parameters: Any*)(
    extractOne: WrappedResultSet => A)(
    extractTo1: WrappedResultSet => Option[B1],
    extractTo2: WrappedResultSet => Option[B2],
    extractTo3: WrappedResultSet => Option[B3],
    extractTo4: WrappedResultSet => Option[B4],
    extractTo5: WrappedResultSet => Option[B5])(
    transform: (A, Seq[B1], Seq[B2], Seq[B3], Seq[B4], Seq[B5]) => Z)(
    implicit
    cxt: EC = ECGlobal): Future[Iterable[Z]] =
    oneToManies5Iterable[A, B1, B2, B3, B4, B5, Z](statement, parameters: _*)(extractOne)(
      extractTo1,
      extractTo2,
      extractTo3,
      extractTo4,
      extractTo5)(transform)

  def oneToManies5Iterable[A, B1, B2, B3, B4, B5, Z](
    statement: String,
    parameters: Any*)(
    extractOne: WrappedResultSet => A)(
    extractTo1: WrappedResultSet => Option[B1],
    extractTo2: WrappedResultSet => Option[B2],
    extractTo3: WrappedResultSet => Option[B3],
    extractTo4: WrappedResultSet => Option[B4],
    extractTo5: WrappedResultSet => Option[B5])(
    transform: (A, Seq[B1], Seq[B2], Seq[B3], Seq[B4], Seq[B5]) => Z)(
    implicit
    cxt: EC = ECGlobal): Future[Iterable[Z]] = {
    val _parameters = ensureAndNormalizeParameters(parameters)
    withListeners(statement, _parameters) {
      queryLogging(statement, _parameters)

      def processResultSet(
        result: LinkedHashMap[A, (Seq[B1], Seq[B2], Seq[B3], Seq[B4], Seq[B5])],
        rs: WrappedResultSet): LinkedHashMap[A, (Seq[B1], Seq[B2], Seq[B3], Seq[B4], Seq[B5])] = {
        val o = extractOne(rs)
        val (to1, to2, to3, to4, to5) = (extractTo1(rs), extractTo2(rs), extractTo3(rs), extractTo4(rs), extractTo5(rs))
        result.keys.find(_ == o).map { _ =>
          to1.orElse(to2).orElse(to3).orElse(to4).orElse(to5).map { _ =>
            val (ts1, ts2, ts3, ts4, ts5) = result.apply(o)
            result += (o -> (
              to1.map(t => if (ts1.contains(t)) ts1 else ts1 :+ t).getOrElse(ts1),
              to2.map(t => if (ts2.contains(t)) ts2 else ts2 :+ t).getOrElse(ts2),
              to3.map(t => if (ts3.contains(t)) ts3 else ts3 :+ t).getOrElse(ts3),
              to4.map(t => if (ts4.contains(t)) ts4 else ts4 :+ t).getOrElse(ts4),
              to5.map(t => if (ts5.contains(t)) ts5 else ts5 :+ t).getOrElse(ts5)))
          }.getOrElse(result)
        }.getOrElse {
          result += (
            o -> (
              to1.map(t => Vector(t)).getOrElse(Vector.empty),
              to2.map(t => Vector(t)).getOrElse(Vector.empty),
              to3.map(t => Vector(t)).getOrElse(Vector.empty),
              to4.map(t => Vector(t)).getOrElse(Vector.empty),
              to5.map(t => Vector(t)).getOrElse(Vector.empty)))
        }
      }
      connection.sendPreparedStatement(statement, _parameters: _*).map { result =>
        result.rows.map { ars =>
          new AsyncResultSetIterator(ars).foldLeft(
            LinkedHashMap[A, (Seq[B1], Seq[B2], Seq[B3], Seq[B4], Seq[B5])]())(processResultSet).map {
              case (one, (t1, t2, t3, t4, t5)) => transform(one, t1, t2, t3, t4, t5)
            }
        }.getOrElse(Nil)
      }
    }
  }

  @deprecated("will be removed. use oneToManies6Iterable", "0.12.0")
  def oneToManies6Traversable[A, B1, B2, B3, B4, B5, B6, Z](
    statement: String,
    parameters: Any*)(
    extractOne: WrappedResultSet => A)(
    extractTo1: WrappedResultSet => Option[B1],
    extractTo2: WrappedResultSet => Option[B2],
    extractTo3: WrappedResultSet => Option[B3],
    extractTo4: WrappedResultSet => Option[B4],
    extractTo5: WrappedResultSet => Option[B5],
    extractTo6: WrappedResultSet => Option[B6])(
    transform: (A, Seq[B1], Seq[B2], Seq[B3], Seq[B4], Seq[B5], Seq[B6]) => Z)(
    implicit
    cxt: EC = ECGlobal): Future[Iterable[Z]] =
    oneToManies6Iterable[A, B1, B2, B3, B4, B5, B6, Z](statement, parameters: _*)(extractOne)(
      extractTo1,
      extractTo2,
      extractTo3,
      extractTo4,
      extractTo5,
      extractTo6)(transform)

  def oneToManies6Iterable[A, B1, B2, B3, B4, B5, B6, Z](
    statement: String,
    parameters: Any*)(
    extractOne: WrappedResultSet => A)(
    extractTo1: WrappedResultSet => Option[B1],
    extractTo2: WrappedResultSet => Option[B2],
    extractTo3: WrappedResultSet => Option[B3],
    extractTo4: WrappedResultSet => Option[B4],
    extractTo5: WrappedResultSet => Option[B5],
    extractTo6: WrappedResultSet => Option[B6])(
    transform: (A, Seq[B1], Seq[B2], Seq[B3], Seq[B4], Seq[B5], Seq[B6]) => Z)(
    implicit
    cxt: EC = ECGlobal): Future[Iterable[Z]] = {
    val _parameters = ensureAndNormalizeParameters(parameters)
    withListeners(statement, _parameters) {
      queryLogging(statement, _parameters)

      def processResultSet(
        result: LinkedHashMap[A, (Seq[B1], Seq[B2], Seq[B3], Seq[B4], Seq[B5], Seq[B6])],
        rs: WrappedResultSet): LinkedHashMap[A, (Seq[B1], Seq[B2], Seq[B3], Seq[B4], Seq[B5], Seq[B6])] = {
        val o = extractOne(rs)
        val (to1, to2, to3, to4, to5, to6) = (extractTo1(rs), extractTo2(rs), extractTo3(rs), extractTo4(rs), extractTo5(rs), extractTo6(rs))
        result.keys.find(_ == o).map { _ =>
          to1.orElse(to2).orElse(to3).orElse(to4).orElse(to5).orElse(to6).map { _ =>
            val (ts1, ts2, ts3, ts4, ts5, ts6) = result.apply(o)
            result += (o -> (
              to1.map(t => if (ts1.contains(t)) ts1 else ts1 :+ t).getOrElse(ts1),
              to2.map(t => if (ts2.contains(t)) ts2 else ts2 :+ t).getOrElse(ts2),
              to3.map(t => if (ts3.contains(t)) ts3 else ts3 :+ t).getOrElse(ts3),
              to4.map(t => if (ts4.contains(t)) ts4 else ts4 :+ t).getOrElse(ts4),
              to5.map(t => if (ts5.contains(t)) ts5 else ts5 :+ t).getOrElse(ts5),
              to6.map(t => if (ts6.contains(t)) ts6 else ts6 :+ t).getOrElse(ts6)))
          }.getOrElse(result)
        }.getOrElse {
          result += (
            o -> (
              to1.map(t => Vector(t)).getOrElse(Vector.empty),
              to2.map(t => Vector(t)).getOrElse(Vector.empty),
              to3.map(t => Vector(t)).getOrElse(Vector.empty),
              to4.map(t => Vector(t)).getOrElse(Vector.empty),
              to5.map(t => Vector(t)).getOrElse(Vector.empty),
              to6.map(t => Vector(t)).getOrElse(Vector.empty)))
        }
      }
      connection.sendPreparedStatement(statement, _parameters: _*).map { result =>
        result.rows.map { ars =>
          new AsyncResultSetIterator(ars).foldLeft(
            LinkedHashMap[A, (Seq[B1], Seq[B2], Seq[B3], Seq[B4], Seq[B5], Seq[B6])]())(processResultSet).map {
              case (one, (t1, t2, t3, t4, t5, t6)) => transform(one, t1, t2, t3, t4, t5, t6)
            }
        }.getOrElse(Nil)
      }
    }
  }

  @deprecated("will be removed. use oneToManies7Iterable", "0.12.0")
  def oneToManies7Traversable[A, B1, B2, B3, B4, B5, B6, B7, Z](
    statement: String,
    parameters: Any*)(
    extractOne: WrappedResultSet => A)(
    extractTo1: WrappedResultSet => Option[B1],
    extractTo2: WrappedResultSet => Option[B2],
    extractTo3: WrappedResultSet => Option[B3],
    extractTo4: WrappedResultSet => Option[B4],
    extractTo5: WrappedResultSet => Option[B5],
    extractTo6: WrappedResultSet => Option[B6],
    extractTo7: WrappedResultSet => Option[B7])(
    transform: (A, Seq[B1], Seq[B2], Seq[B3], Seq[B4], Seq[B5], Seq[B6], Seq[B7]) => Z)(
    implicit
    cxt: EC = ECGlobal): Future[Iterable[Z]] =
    oneToManies7Iterable[A, B1, B2, B3, B4, B5, B6, B7, Z](statement, parameters: _*)(extractOne)(
      extractTo1,
      extractTo2,
      extractTo3,
      extractTo4,
      extractTo5,
      extractTo6,
      extractTo7)(transform)

  def oneToManies7Iterable[A, B1, B2, B3, B4, B5, B6, B7, Z](
    statement: String,
    parameters: Any*)(
    extractOne: WrappedResultSet => A)(
    extractTo1: WrappedResultSet => Option[B1],
    extractTo2: WrappedResultSet => Option[B2],
    extractTo3: WrappedResultSet => Option[B3],
    extractTo4: WrappedResultSet => Option[B4],
    extractTo5: WrappedResultSet => Option[B5],
    extractTo6: WrappedResultSet => Option[B6],
    extractTo7: WrappedResultSet => Option[B7])(
    transform: (A, Seq[B1], Seq[B2], Seq[B3], Seq[B4], Seq[B5], Seq[B6], Seq[B7]) => Z)(
    implicit
    cxt: EC = ECGlobal): Future[Iterable[Z]] = {
    val _parameters = ensureAndNormalizeParameters(parameters)
    withListeners(statement, _parameters) {
      queryLogging(statement, _parameters)

      def processResultSet(
        result: LinkedHashMap[A, (Seq[B1], Seq[B2], Seq[B3], Seq[B4], Seq[B5], Seq[B6], Seq[B7])],
        rs: WrappedResultSet): LinkedHashMap[A, (Seq[B1], Seq[B2], Seq[B3], Seq[B4], Seq[B5], Seq[B6], Seq[B7])] = {
        val o = extractOne(rs)
        val (to1, to2, to3, to4, to5, to6, to7) = (
          extractTo1(rs),
          extractTo2(rs),
          extractTo3(rs),
          extractTo4(rs),
          extractTo5(rs),
          extractTo6(rs),
          extractTo7(rs))
        result.keys.find(_ == o).map { _ =>
          to1.orElse(to2).orElse(to3).orElse(to4).orElse(to5).orElse(to6).orElse(to7).map { _ =>
            val (ts1, ts2, ts3, ts4, ts5, ts6, ts7) = result.apply(o)
            result += (o -> (
              to1.map(t => if (ts1.contains(t)) ts1 else ts1 :+ t).getOrElse(ts1),
              to2.map(t => if (ts2.contains(t)) ts2 else ts2 :+ t).getOrElse(ts2),
              to3.map(t => if (ts3.contains(t)) ts3 else ts3 :+ t).getOrElse(ts3),
              to4.map(t => if (ts4.contains(t)) ts4 else ts4 :+ t).getOrElse(ts4),
              to5.map(t => if (ts5.contains(t)) ts5 else ts5 :+ t).getOrElse(ts5),
              to6.map(t => if (ts6.contains(t)) ts6 else ts6 :+ t).getOrElse(ts6),
              to7.map(t => if (ts7.contains(t)) ts7 else ts7 :+ t).getOrElse(ts7)))
          }.getOrElse(result)
        }.getOrElse {
          result += (
            o -> (
              to1.map(t => Vector(t)).getOrElse(Vector.empty),
              to2.map(t => Vector(t)).getOrElse(Vector.empty),
              to3.map(t => Vector(t)).getOrElse(Vector.empty),
              to4.map(t => Vector(t)).getOrElse(Vector.empty),
              to5.map(t => Vector(t)).getOrElse(Vector.empty),
              to6.map(t => Vector(t)).getOrElse(Vector.empty),
              to7.map(t => Vector(t)).getOrElse(Vector.empty)))
        }
      }
      connection.sendPreparedStatement(statement, _parameters: _*).map { result =>
        result.rows.map { ars =>
          new AsyncResultSetIterator(ars).foldLeft(
            LinkedHashMap[A, (Seq[B1], Seq[B2], Seq[B3], Seq[B4], Seq[B5], Seq[B6], Seq[B7])]())(processResultSet).map {
              case (one, (t1, t2, t3, t4, t5, t6, t7)) => transform(one, t1, t2, t3, t4, t5, t6, t7)
            }
        }.getOrElse(Nil)
      }
    }
  }

  @deprecated("will be removed. use oneToManies8Iterable", "0.12.0")
  def oneToManies8Traversable[A, B1, B2, B3, B4, B5, B6, B7, B8, Z](
    statement: String,
    parameters: Any*)(
    extractOne: WrappedResultSet => A)(
    extractTo1: WrappedResultSet => Option[B1],
    extractTo2: WrappedResultSet => Option[B2],
    extractTo3: WrappedResultSet => Option[B3],
    extractTo4: WrappedResultSet => Option[B4],
    extractTo5: WrappedResultSet => Option[B5],
    extractTo6: WrappedResultSet => Option[B6],
    extractTo7: WrappedResultSet => Option[B7],
    extractTo8: WrappedResultSet => Option[B8])(
    transform: (A, Seq[B1], Seq[B2], Seq[B3], Seq[B4], Seq[B5], Seq[B6], Seq[B7], Seq[B8]) => Z)(
    implicit
    cxt: EC = ECGlobal): Future[Iterable[Z]] =
    oneToManies8Iterable[A, B1, B2, B3, B4, B5, B6, B7, B8, Z](statement, parameters: _*)(extractOne)(
      extractTo1,
      extractTo2,
      extractTo3,
      extractTo4,
      extractTo5,
      extractTo6,
      extractTo7,
      extractTo8)(transform)

  def oneToManies8Iterable[A, B1, B2, B3, B4, B5, B6, B7, B8, Z](
    statement: String,
    parameters: Any*)(
    extractOne: WrappedResultSet => A)(
    extractTo1: WrappedResultSet => Option[B1],
    extractTo2: WrappedResultSet => Option[B2],
    extractTo3: WrappedResultSet => Option[B3],
    extractTo4: WrappedResultSet => Option[B4],
    extractTo5: WrappedResultSet => Option[B5],
    extractTo6: WrappedResultSet => Option[B6],
    extractTo7: WrappedResultSet => Option[B7],
    extractTo8: WrappedResultSet => Option[B8])(
    transform: (A, Seq[B1], Seq[B2], Seq[B3], Seq[B4], Seq[B5], Seq[B6], Seq[B7], Seq[B8]) => Z)(
    implicit
    cxt: EC = ECGlobal): Future[Iterable[Z]] = {
    val _parameters = ensureAndNormalizeParameters(parameters)
    withListeners(statement, _parameters) {
      queryLogging(statement, _parameters)

      def processResultSet(
        result: LinkedHashMap[A, (Seq[B1], Seq[B2], Seq[B3], Seq[B4], Seq[B5], Seq[B6], Seq[B7], Seq[B8])],
        rs: WrappedResultSet): LinkedHashMap[A, (Seq[B1], Seq[B2], Seq[B3], Seq[B4], Seq[B5], Seq[B6], Seq[B7], Seq[B8])] = {
        val o = extractOne(rs)
        val (to1, to2, to3, to4, to5, to6, to7, to8) = (
          extractTo1(rs),
          extractTo2(rs),
          extractTo3(rs),
          extractTo4(rs),
          extractTo5(rs),
          extractTo6(rs),
          extractTo7(rs),
          extractTo8(rs))
        result.keys.find(_ == o).map { _ =>
          to1.orElse(to2).orElse(to3).orElse(to4).orElse(to5).orElse(to6).orElse(to7).orElse(to8).map { _ =>
            val (ts1, ts2, ts3, ts4, ts5, ts6, ts7, ts8) = result.apply(o)
            result += (o -> (
              to1.map(t => if (ts1.contains(t)) ts1 else ts1 :+ t).getOrElse(ts1),
              to2.map(t => if (ts2.contains(t)) ts2 else ts2 :+ t).getOrElse(ts2),
              to3.map(t => if (ts3.contains(t)) ts3 else ts3 :+ t).getOrElse(ts3),
              to4.map(t => if (ts4.contains(t)) ts4 else ts4 :+ t).getOrElse(ts4),
              to5.map(t => if (ts5.contains(t)) ts5 else ts5 :+ t).getOrElse(ts5),
              to6.map(t => if (ts6.contains(t)) ts6 else ts6 :+ t).getOrElse(ts6),
              to7.map(t => if (ts7.contains(t)) ts7 else ts7 :+ t).getOrElse(ts7),
              to8.map(t => if (ts8.contains(t)) ts8 else ts8 :+ t).getOrElse(ts8)))
          }.getOrElse(result)
        }.getOrElse {
          result += (
            o -> (
              to1.map(t => Vector(t)).getOrElse(Vector.empty),
              to2.map(t => Vector(t)).getOrElse(Vector.empty),
              to3.map(t => Vector(t)).getOrElse(Vector.empty),
              to4.map(t => Vector(t)).getOrElse(Vector.empty),
              to5.map(t => Vector(t)).getOrElse(Vector.empty),
              to6.map(t => Vector(t)).getOrElse(Vector.empty),
              to7.map(t => Vector(t)).getOrElse(Vector.empty),
              to8.map(t => Vector(t)).getOrElse(Vector.empty)))
        }
      }
      connection.sendPreparedStatement(statement, _parameters: _*).map { result =>
        result.rows.map { ars =>
          new AsyncResultSetIterator(ars).foldLeft(
            LinkedHashMap[A, (Seq[B1], Seq[B2], Seq[B3], Seq[B4], Seq[B5], Seq[B6], Seq[B7], Seq[B8])]())(processResultSet).map {
              case (one, (t1, t2, t3, t4, t5, t6, t7, t8)) => transform(one, t1, t2, t3, t4, t5, t6, t7, t8)
            }
        }.getOrElse(Nil)
      }
    }
  }

  @deprecated("will be removed. use oneToManies9Iterable", "0.12.0")
  def oneToManies9Traversable[A, B1, B2, B3, B4, B5, B6, B7, B8, B9, Z](
    statement: String,
    parameters: Any*)(
    extractOne: WrappedResultSet => A)(
    extractTo1: WrappedResultSet => Option[B1],
    extractTo2: WrappedResultSet => Option[B2],
    extractTo3: WrappedResultSet => Option[B3],
    extractTo4: WrappedResultSet => Option[B4],
    extractTo5: WrappedResultSet => Option[B5],
    extractTo6: WrappedResultSet => Option[B6],
    extractTo7: WrappedResultSet => Option[B7],
    extractTo8: WrappedResultSet => Option[B8],
    extractTo9: WrappedResultSet => Option[B9])(
    transform: (A, Seq[B1], Seq[B2], Seq[B3], Seq[B4], Seq[B5], Seq[B6], Seq[B7], Seq[B8], Seq[B9]) => Z)(
    implicit
    cxt: EC = ECGlobal): Future[Iterable[Z]] =
    oneToManies9Iterable[A, B1, B2, B3, B4, B5, B6, B7, B8, B9, Z](statement, parameters: _*)(extractOne)(
      extractTo1,
      extractTo2,
      extractTo3,
      extractTo4,
      extractTo5,
      extractTo6,
      extractTo7,
      extractTo8,
      extractTo9)(transform)

  def oneToManies9Iterable[A, B1, B2, B3, B4, B5, B6, B7, B8, B9, Z](
    statement: String,
    parameters: Any*)(
    extractOne: WrappedResultSet => A)(
    extractTo1: WrappedResultSet => Option[B1],
    extractTo2: WrappedResultSet => Option[B2],
    extractTo3: WrappedResultSet => Option[B3],
    extractTo4: WrappedResultSet => Option[B4],
    extractTo5: WrappedResultSet => Option[B5],
    extractTo6: WrappedResultSet => Option[B6],
    extractTo7: WrappedResultSet => Option[B7],
    extractTo8: WrappedResultSet => Option[B8],
    extractTo9: WrappedResultSet => Option[B9])(
    transform: (A, Seq[B1], Seq[B2], Seq[B3], Seq[B4], Seq[B5], Seq[B6], Seq[B7], Seq[B8], Seq[B9]) => Z)(
    implicit
    cxt: EC = ECGlobal): Future[Iterable[Z]] = {
    val _parameters = ensureAndNormalizeParameters(parameters)
    withListeners(statement, _parameters) {
      queryLogging(statement, _parameters)

      def processResultSet(
        result: LinkedHashMap[A, (Seq[B1], Seq[B2], Seq[B3], Seq[B4], Seq[B5], Seq[B6], Seq[B7], Seq[B8], Seq[B9])],
        rs: WrappedResultSet): LinkedHashMap[A, (Seq[B1], Seq[B2], Seq[B3], Seq[B4], Seq[B5], Seq[B6], Seq[B7], Seq[B8], Seq[B9])] = {
        val o = extractOne(rs)
        val (to1, to2, to3, to4, to5, to6, to7, to8, to9) = (
          extractTo1(rs),
          extractTo2(rs),
          extractTo3(rs),
          extractTo4(rs),
          extractTo5(rs),
          extractTo6(rs),
          extractTo7(rs),
          extractTo8(rs),
          extractTo9(rs))
        result.keys.find(_ == o).map { _ =>
          to1.orElse(to2).orElse(to3).orElse(to4).orElse(to5).orElse(to6).orElse(to7).orElse(to8).orElse(to9).map { _ =>
            val (ts1, ts2, ts3, ts4, ts5, ts6, ts7, ts8, ts9) = result.apply(o)
            result += (o -> (
              to1.map(t => if (ts1.contains(t)) ts1 else ts1 :+ t).getOrElse(ts1),
              to2.map(t => if (ts2.contains(t)) ts2 else ts2 :+ t).getOrElse(ts2),
              to3.map(t => if (ts3.contains(t)) ts3 else ts3 :+ t).getOrElse(ts3),
              to4.map(t => if (ts4.contains(t)) ts4 else ts4 :+ t).getOrElse(ts4),
              to5.map(t => if (ts5.contains(t)) ts5 else ts5 :+ t).getOrElse(ts5),
              to6.map(t => if (ts6.contains(t)) ts6 else ts6 :+ t).getOrElse(ts6),
              to7.map(t => if (ts7.contains(t)) ts7 else ts7 :+ t).getOrElse(ts7),
              to8.map(t => if (ts8.contains(t)) ts8 else ts8 :+ t).getOrElse(ts8),
              to9.map(t => if (ts9.contains(t)) ts9 else ts9 :+ t).getOrElse(ts9)))
          }.getOrElse(result)
        }.getOrElse {
          result += (
            o -> (
              to1.map(t => Vector(t)).getOrElse(Vector.empty),
              to2.map(t => Vector(t)).getOrElse(Vector.empty),
              to3.map(t => Vector(t)).getOrElse(Vector.empty),
              to4.map(t => Vector(t)).getOrElse(Vector.empty),
              to5.map(t => Vector(t)).getOrElse(Vector.empty),
              to6.map(t => Vector(t)).getOrElse(Vector.empty),
              to7.map(t => Vector(t)).getOrElse(Vector.empty),
              to8.map(t => Vector(t)).getOrElse(Vector.empty),
              to9.map(t => Vector(t)).getOrElse(Vector.empty)))
        }
      }
      connection.sendPreparedStatement(statement, _parameters: _*).map { result =>
        result.rows.map { ars =>
          new AsyncResultSetIterator(ars).foldLeft(
            LinkedHashMap[A, (Seq[B1], Seq[B2], Seq[B3], Seq[B4], Seq[B5], Seq[B6], Seq[B7], Seq[B8], Seq[B9])]())(processResultSet).map {
              case (one, (t1, t2, t3, t4, t5, t6, t7, t8, t9)) => transform(one, t1, t2, t3, t4, t5, t6, t7, t8, t9)
            }
        }.getOrElse(Nil)
      }
    }
  }

  @deprecated("will be removed. use oneToManies10Iterable", "0.12.0")
  def oneToManies10Traversable[A, B1, B2, B3, B4, B5, B6, B7, B8, B9, B10, Z](
    statement: String,
    parameters: Any*)(
    extractOne: WrappedResultSet => A)(
    extractTo1: WrappedResultSet => Option[B1],
    extractTo2: WrappedResultSet => Option[B2],
    extractTo3: WrappedResultSet => Option[B3],
    extractTo4: WrappedResultSet => Option[B4],
    extractTo5: WrappedResultSet => Option[B5],
    extractTo6: WrappedResultSet => Option[B6],
    extractTo7: WrappedResultSet => Option[B7],
    extractTo8: WrappedResultSet => Option[B8],
    extractTo9: WrappedResultSet => Option[B9],
    extractTo10: WrappedResultSet => Option[B10])(
    transform: (A, Seq[B1], Seq[B2], Seq[B3], Seq[B4], Seq[B5], Seq[B6], Seq[B7], Seq[B8], Seq[B9], Seq[B10]) => Z)(
    implicit
    cxt: EC = ECGlobal): Future[Iterable[Z]] =
    oneToManies10Iterable[A, B1, B2, B3, B4, B5, B6, B7, B8, B9, B10, Z](statement, parameters: _*)(extractOne)(
      extractTo1,
      extractTo2,
      extractTo3,
      extractTo4,
      extractTo5,
      extractTo6,
      extractTo7,
      extractTo8,
      extractTo9,
      extractTo10)(transform)
  def oneToManies10Iterable[A, B1, B2, B3, B4, B5, B6, B7, B8, B9, B10, Z](
    statement: String,
    parameters: Any*)(
    extractOne: WrappedResultSet => A)(
    extractTo1: WrappedResultSet => Option[B1],
    extractTo2: WrappedResultSet => Option[B2],
    extractTo3: WrappedResultSet => Option[B3],
    extractTo4: WrappedResultSet => Option[B4],
    extractTo5: WrappedResultSet => Option[B5],
    extractTo6: WrappedResultSet => Option[B6],
    extractTo7: WrappedResultSet => Option[B7],
    extractTo8: WrappedResultSet => Option[B8],
    extractTo9: WrappedResultSet => Option[B9],
    extractTo10: WrappedResultSet => Option[B10])(
    transform: (A, Seq[B1], Seq[B2], Seq[B3], Seq[B4], Seq[B5], Seq[B6], Seq[B7], Seq[B8], Seq[B9], Seq[B10]) => Z)(
    implicit
    cxt: EC = ECGlobal): Future[Iterable[Z]] = {
    val _parameters = ensureAndNormalizeParameters(parameters)
    withListeners(statement, _parameters) {
      queryLogging(statement, _parameters)

      def processResultSet(
        result: LinkedHashMap[A, (Seq[B1], Seq[B2], Seq[B3], Seq[B4], Seq[B5], Seq[B6], Seq[B7], Seq[B8], Seq[B9], Seq[B10])],
        rs: WrappedResultSet): LinkedHashMap[A, (Seq[B1], Seq[B2], Seq[B3], Seq[B4], Seq[B5], Seq[B6], Seq[B7], Seq[B8], Seq[B9], Seq[B10])] = {
        val o = extractOne(rs)
        val (to1, to2, to3, to4, to5, to6, to7, to8, to9, to10) = (
          extractTo1(rs),
          extractTo2(rs),
          extractTo3(rs),
          extractTo4(rs),
          extractTo5(rs),
          extractTo6(rs),
          extractTo7(rs),
          extractTo8(rs),
          extractTo9(rs),
          extractTo10(rs))
        result.keys.find(_ == o).map { _ =>
          to1.orElse(to2).orElse(to3).orElse(to4).orElse(to5).orElse(to6).orElse(to7).orElse(to8).orElse(to9).orElse(to10).map { _ =>
            val (ts1, ts2, ts3, ts4, ts5, ts6, ts7, ts8, ts9, ts10) = result.apply(o)
            result += (o -> (
              to1.map(t => if (ts1.contains(t)) ts1 else ts1 :+ t).getOrElse(ts1),
              to2.map(t => if (ts2.contains(t)) ts2 else ts2 :+ t).getOrElse(ts2),
              to3.map(t => if (ts3.contains(t)) ts3 else ts3 :+ t).getOrElse(ts3),
              to4.map(t => if (ts4.contains(t)) ts4 else ts4 :+ t).getOrElse(ts4),
              to5.map(t => if (ts5.contains(t)) ts5 else ts5 :+ t).getOrElse(ts5),
              to6.map(t => if (ts6.contains(t)) ts6 else ts6 :+ t).getOrElse(ts6),
              to7.map(t => if (ts7.contains(t)) ts7 else ts7 :+ t).getOrElse(ts7),
              to8.map(t => if (ts8.contains(t)) ts8 else ts8 :+ t).getOrElse(ts8),
              to9.map(t => if (ts9.contains(t)) ts9 else ts9 :+ t).getOrElse(ts9),
              to10.map(t => if (ts10.contains(t)) ts10 else ts10 :+ t).getOrElse(ts10)))
          }.getOrElse(result)
        }.getOrElse {
          result += (
            o -> (
              to1.map(t => Vector(t)).getOrElse(Vector()),
              to2.map(t => Vector(t)).getOrElse(Vector()),
              to3.map(t => Vector(t)).getOrElse(Vector()),
              to4.map(t => Vector(t)).getOrElse(Vector()),
              to5.map(t => Vector(t)).getOrElse(Vector()),
              to6.map(t => Vector(t)).getOrElse(Vector()),
              to7.map(t => Vector(t)).getOrElse(Vector()),
              to8.map(t => Vector(t)).getOrElse(Vector()),
              to9.map(t => Vector(t)).getOrElse(Vector()),
              to10.map(t => Vector(t)).getOrElse(Vector())))
        }
      }

      connection.sendPreparedStatement(statement, _parameters: _*).map { result =>
        result.rows.map { ars =>
          new AsyncResultSetIterator(ars).foldLeft(
            LinkedHashMap[A, (Seq[B1], Seq[B2], Seq[B3], Seq[B4], Seq[B5], Seq[B6], Seq[B7], Seq[B8], Seq[B9], Seq[B10])]())(processResultSet).map {
              case (one, (t1, t2, t3, t4, t5, t6, t7, t8, t9, t10)) => transform(one, t1, t2, t3, t4, t5, t6, t7, t8, t9, t10)
            }
        }.getOrElse(Nil)
      }
    }
  }

  @deprecated("will be removed. use oneToManies11Iterable", "0.12.0")
  def oneToManies11Traversable[A, B1, B2, B3, B4, B5, B6, B7, B8, B9, B10, B11, Z](
    statement: String,
    parameters: Any*)(
    extractOne: WrappedResultSet => A)(
    extractTo1: WrappedResultSet => Option[B1],
    extractTo2: WrappedResultSet => Option[B2],
    extractTo3: WrappedResultSet => Option[B3],
    extractTo4: WrappedResultSet => Option[B4],
    extractTo5: WrappedResultSet => Option[B5],
    extractTo6: WrappedResultSet => Option[B6],
    extractTo7: WrappedResultSet => Option[B7],
    extractTo8: WrappedResultSet => Option[B8],
    extractTo9: WrappedResultSet => Option[B9],
    extractTo10: WrappedResultSet => Option[B10],
    extractTo11: WrappedResultSet => Option[B11])(
    transform: (A, Seq[B1], Seq[B2], Seq[B3], Seq[B4], Seq[B5], Seq[B6], Seq[B7], Seq[B8], Seq[B9], Seq[B10], Seq[B11]) => Z)(
    implicit
    cxt: EC = ECGlobal): Future[Iterable[Z]] =
    oneToManies11Iterable[A, B1, B2, B3, B4, B5, B6, B7, B8, B9, B10, B11, Z](statement, parameters: _*)(extractOne)(
      extractTo1,
      extractTo2,
      extractTo3,
      extractTo4,
      extractTo5,
      extractTo6,
      extractTo7,
      extractTo8,
      extractTo9,
      extractTo10,
      extractTo11)(transform)
  def oneToManies11Iterable[A, B1, B2, B3, B4, B5, B6, B7, B8, B9, B10, B11, Z](
    statement: String,
    parameters: Any*)(
    extractOne: WrappedResultSet => A)(
    extractTo1: WrappedResultSet => Option[B1],
    extractTo2: WrappedResultSet => Option[B2],
    extractTo3: WrappedResultSet => Option[B3],
    extractTo4: WrappedResultSet => Option[B4],
    extractTo5: WrappedResultSet => Option[B5],
    extractTo6: WrappedResultSet => Option[B6],
    extractTo7: WrappedResultSet => Option[B7],
    extractTo8: WrappedResultSet => Option[B8],
    extractTo9: WrappedResultSet => Option[B9],
    extractTo10: WrappedResultSet => Option[B10],
    extractTo11: WrappedResultSet => Option[B11])(
    transform: (A, Seq[B1], Seq[B2], Seq[B3], Seq[B4], Seq[B5], Seq[B6], Seq[B7], Seq[B8], Seq[B9], Seq[B10], Seq[B11]) => Z)(
    implicit
    cxt: EC = ECGlobal): Future[Iterable[Z]] = {
    val _parameters = ensureAndNormalizeParameters(parameters)
    withListeners(statement, _parameters) {
      queryLogging(statement, _parameters)

      def processResultSet(
        result: LinkedHashMap[A, (Seq[B1], Seq[B2], Seq[B3], Seq[B4], Seq[B5], Seq[B6], Seq[B7], Seq[B8], Seq[B9], Seq[B10], Seq[B11])],
        rs: WrappedResultSet): LinkedHashMap[A, (Seq[B1], Seq[B2], Seq[B3], Seq[B4], Seq[B5], Seq[B6], Seq[B7], Seq[B8], Seq[B9], Seq[B10], Seq[B11])] = {
        val o = extractOne(rs)
        val (to1, to2, to3, to4, to5, to6, to7, to8, to9, to10, to11) = (
          extractTo1(rs),
          extractTo2(rs),
          extractTo3(rs),
          extractTo4(rs),
          extractTo5(rs),
          extractTo6(rs),
          extractTo7(rs),
          extractTo8(rs),
          extractTo9(rs),
          extractTo10(rs),
          extractTo11(rs))
        result.keys.find(_ == o).map { _ =>
          to1.orElse(to2).orElse(to3).orElse(to4).orElse(to5).orElse(to6).orElse(to7).orElse(to8).orElse(to9).orElse(to10).orElse(to11).map { _ =>
            val (ts1, ts2, ts3, ts4, ts5, ts6, ts7, ts8, ts9, ts10, ts11) = result.apply(o)
            result += (o -> (
              to1.map(t => if (ts1.contains(t)) ts1 else ts1 :+ t).getOrElse(ts1),
              to2.map(t => if (ts2.contains(t)) ts2 else ts2 :+ t).getOrElse(ts2),
              to3.map(t => if (ts3.contains(t)) ts3 else ts3 :+ t).getOrElse(ts3),
              to4.map(t => if (ts4.contains(t)) ts4 else ts4 :+ t).getOrElse(ts4),
              to5.map(t => if (ts5.contains(t)) ts5 else ts5 :+ t).getOrElse(ts5),
              to6.map(t => if (ts6.contains(t)) ts6 else ts6 :+ t).getOrElse(ts6),
              to7.map(t => if (ts7.contains(t)) ts7 else ts7 :+ t).getOrElse(ts7),
              to8.map(t => if (ts8.contains(t)) ts8 else ts8 :+ t).getOrElse(ts8),
              to9.map(t => if (ts9.contains(t)) ts9 else ts9 :+ t).getOrElse(ts9),
              to10.map(t => if (ts10.contains(t)) ts10 else ts10 :+ t).getOrElse(ts10),
              to11.map(t => if (ts11.contains(t)) ts11 else ts11 :+ t).getOrElse(ts11)))
          }.getOrElse(result)
        }.getOrElse {
          result += (
            o -> (
              to1.map(t => Vector(t)).getOrElse(Vector()),
              to2.map(t => Vector(t)).getOrElse(Vector()),
              to3.map(t => Vector(t)).getOrElse(Vector()),
              to4.map(t => Vector(t)).getOrElse(Vector()),
              to5.map(t => Vector(t)).getOrElse(Vector()),
              to6.map(t => Vector(t)).getOrElse(Vector()),
              to7.map(t => Vector(t)).getOrElse(Vector()),
              to8.map(t => Vector(t)).getOrElse(Vector()),
              to9.map(t => Vector(t)).getOrElse(Vector()),
              to10.map(t => Vector(t)).getOrElse(Vector()),
              to11.map(t => Vector(t)).getOrElse(Vector())))
        }
      }

      connection.sendPreparedStatement(statement, _parameters: _*).map { result =>
        result.rows.map { ars =>
          new AsyncResultSetIterator(ars).foldLeft(
            LinkedHashMap[A, (Seq[B1], Seq[B2], Seq[B3], Seq[B4], Seq[B5], Seq[B6], Seq[B7], Seq[B8], Seq[B9], Seq[B10], Seq[B11])]())(processResultSet).map {
              case (one, (t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11)) => transform(one, t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11)
            }
        }.getOrElse(Nil)
      }
    }
  }

  @deprecated("will be removed. use oneToManies12Iterable", "0.12.0")
  def oneToManies12Traversable[A, B1, B2, B3, B4, B5, B6, B7, B8, B9, B10, B11, B12, Z](
    statement: String,
    parameters: Any*)(
    extractOne: WrappedResultSet => A)(
    extractTo1: WrappedResultSet => Option[B1],
    extractTo2: WrappedResultSet => Option[B2],
    extractTo3: WrappedResultSet => Option[B3],
    extractTo4: WrappedResultSet => Option[B4],
    extractTo5: WrappedResultSet => Option[B5],
    extractTo6: WrappedResultSet => Option[B6],
    extractTo7: WrappedResultSet => Option[B7],
    extractTo8: WrappedResultSet => Option[B8],
    extractTo9: WrappedResultSet => Option[B9],
    extractTo10: WrappedResultSet => Option[B10],
    extractTo11: WrappedResultSet => Option[B11],
    extractTo12: WrappedResultSet => Option[B12])(
    transform: (A, Seq[B1], Seq[B2], Seq[B3], Seq[B4], Seq[B5], Seq[B6], Seq[B7], Seq[B8], Seq[B9], Seq[B10], Seq[B11], Seq[B12]) => Z)(
    implicit
    cxt: EC = ECGlobal): Future[Iterable[Z]] =
    oneToManies12Iterable[A, B1, B2, B3, B4, B5, B6, B7, B8, B9, B10, B11, B12, Z](statement, parameters: _*)(extractOne)(
      extractTo1,
      extractTo2,
      extractTo3,
      extractTo4,
      extractTo5,
      extractTo6,
      extractTo7,
      extractTo8,
      extractTo9,
      extractTo10,
      extractTo11,
      extractTo12)(transform)
  def oneToManies12Iterable[A, B1, B2, B3, B4, B5, B6, B7, B8, B9, B10, B11, B12, Z](
    statement: String,
    parameters: Any*)(
    extractOne: WrappedResultSet => A)(
    extractTo1: WrappedResultSet => Option[B1],
    extractTo2: WrappedResultSet => Option[B2],
    extractTo3: WrappedResultSet => Option[B3],
    extractTo4: WrappedResultSet => Option[B4],
    extractTo5: WrappedResultSet => Option[B5],
    extractTo6: WrappedResultSet => Option[B6],
    extractTo7: WrappedResultSet => Option[B7],
    extractTo8: WrappedResultSet => Option[B8],
    extractTo9: WrappedResultSet => Option[B9],
    extractTo10: WrappedResultSet => Option[B10],
    extractTo11: WrappedResultSet => Option[B11],
    extractTo12: WrappedResultSet => Option[B12])(
    transform: (A, Seq[B1], Seq[B2], Seq[B3], Seq[B4], Seq[B5], Seq[B6], Seq[B7], Seq[B8], Seq[B9], Seq[B10], Seq[B11], Seq[B12]) => Z)(
    implicit
    cxt: EC = ECGlobal): Future[Iterable[Z]] = {
    val _parameters = ensureAndNormalizeParameters(parameters)
    withListeners(statement, _parameters) {
      queryLogging(statement, _parameters)

      def processResultSet(
        result: LinkedHashMap[A, (Seq[B1], Seq[B2], Seq[B3], Seq[B4], Seq[B5], Seq[B6], Seq[B7], Seq[B8], Seq[B9], Seq[B10], Seq[B11], Seq[B12])],
        rs: WrappedResultSet): LinkedHashMap[A, (Seq[B1], Seq[B2], Seq[B3], Seq[B4], Seq[B5], Seq[B6], Seq[B7], Seq[B8], Seq[B9], Seq[B10], Seq[B11], Seq[B12])] = {
        val o = extractOne(rs)
        val (to1, to2, to3, to4, to5, to6, to7, to8, to9, to10, to11, to12) = (
          extractTo1(rs),
          extractTo2(rs),
          extractTo3(rs),
          extractTo4(rs),
          extractTo5(rs),
          extractTo6(rs),
          extractTo7(rs),
          extractTo8(rs),
          extractTo9(rs),
          extractTo10(rs),
          extractTo11(rs),
          extractTo12(rs))
        result.keys.find(_ == o).map { _ =>
          to1.orElse(to2).orElse(to3).orElse(to4).orElse(to5).orElse(to6).orElse(to7).orElse(to8).orElse(to9).orElse(to10).orElse(to11).orElse(to12).map { _ =>
            val (ts1, ts2, ts3, ts4, ts5, ts6, ts7, ts8, ts9, ts10, ts11, ts12) = result.apply(o)
            result += (o -> (
              to1.map(t => if (ts1.contains(t)) ts1 else ts1 :+ t).getOrElse(ts1),
              to2.map(t => if (ts2.contains(t)) ts2 else ts2 :+ t).getOrElse(ts2),
              to3.map(t => if (ts3.contains(t)) ts3 else ts3 :+ t).getOrElse(ts3),
              to4.map(t => if (ts4.contains(t)) ts4 else ts4 :+ t).getOrElse(ts4),
              to5.map(t => if (ts5.contains(t)) ts5 else ts5 :+ t).getOrElse(ts5),
              to6.map(t => if (ts6.contains(t)) ts6 else ts6 :+ t).getOrElse(ts6),
              to7.map(t => if (ts7.contains(t)) ts7 else ts7 :+ t).getOrElse(ts7),
              to8.map(t => if (ts8.contains(t)) ts8 else ts8 :+ t).getOrElse(ts8),
              to9.map(t => if (ts9.contains(t)) ts9 else ts9 :+ t).getOrElse(ts9),
              to10.map(t => if (ts10.contains(t)) ts10 else ts10 :+ t).getOrElse(ts10),
              to11.map(t => if (ts11.contains(t)) ts11 else ts11 :+ t).getOrElse(ts11),
              to12.map(t => if (ts12.contains(t)) ts12 else ts12 :+ t).getOrElse(ts12)))
          }.getOrElse(result)
        }.getOrElse {
          result += (
            o -> (
              to1.map(t => Vector(t)).getOrElse(Vector()),
              to2.map(t => Vector(t)).getOrElse(Vector()),
              to3.map(t => Vector(t)).getOrElse(Vector()),
              to4.map(t => Vector(t)).getOrElse(Vector()),
              to5.map(t => Vector(t)).getOrElse(Vector()),
              to6.map(t => Vector(t)).getOrElse(Vector()),
              to7.map(t => Vector(t)).getOrElse(Vector()),
              to8.map(t => Vector(t)).getOrElse(Vector()),
              to9.map(t => Vector(t)).getOrElse(Vector()),
              to10.map(t => Vector(t)).getOrElse(Vector()),
              to11.map(t => Vector(t)).getOrElse(Vector()),
              to12.map(t => Vector(t)).getOrElse(Vector())))
        }
      }

      connection.sendPreparedStatement(statement, _parameters: _*).map { result =>
        result.rows.map { ars =>
          new AsyncResultSetIterator(ars).foldLeft(
            LinkedHashMap[A, (Seq[B1], Seq[B2], Seq[B3], Seq[B4], Seq[B5], Seq[B6], Seq[B7], Seq[B8], Seq[B9], Seq[B10], Seq[B11], Seq[B12])]())(processResultSet).map {
              case (one, (t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11, t12)) => transform(one, t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11, t12)
            }
        }.getOrElse(Nil)
      }
    }
  }

  @deprecated("will be removed. use oneToManies13Iterable", "0.12.0")
  def oneToManies13Traversable[A, B1, B2, B3, B4, B5, B6, B7, B8, B9, B10, B11, B12, B13, Z](
    statement: String,
    parameters: Any*)(
    extractOne: WrappedResultSet => A)(
    extractTo1: WrappedResultSet => Option[B1],
    extractTo2: WrappedResultSet => Option[B2],
    extractTo3: WrappedResultSet => Option[B3],
    extractTo4: WrappedResultSet => Option[B4],
    extractTo5: WrappedResultSet => Option[B5],
    extractTo6: WrappedResultSet => Option[B6],
    extractTo7: WrappedResultSet => Option[B7],
    extractTo8: WrappedResultSet => Option[B8],
    extractTo9: WrappedResultSet => Option[B9],
    extractTo10: WrappedResultSet => Option[B10],
    extractTo11: WrappedResultSet => Option[B11],
    extractTo12: WrappedResultSet => Option[B12],
    extractTo13: WrappedResultSet => Option[B13])(
    transform: (A, Seq[B1], Seq[B2], Seq[B3], Seq[B4], Seq[B5], Seq[B6], Seq[B7], Seq[B8], Seq[B9], Seq[B10], Seq[B11], Seq[B12], Seq[B13]) => Z)(
    implicit
    cxt: EC = ECGlobal): Future[Iterable[Z]] =
    oneToManies13Iterable[A, B1, B2, B3, B4, B5, B6, B7, B8, B9, B10, B11, B12, B13, Z](statement, parameters: _*)(extractOne)(
      extractTo1,
      extractTo2,
      extractTo3,
      extractTo4,
      extractTo5,
      extractTo6,
      extractTo7,
      extractTo8,
      extractTo9,
      extractTo10,
      extractTo11,
      extractTo12,
      extractTo13)(transform)
  def oneToManies13Iterable[A, B1, B2, B3, B4, B5, B6, B7, B8, B9, B10, B11, B12, B13, Z](
    statement: String,
    parameters: Any*)(
    extractOne: WrappedResultSet => A)(
    extractTo1: WrappedResultSet => Option[B1],
    extractTo2: WrappedResultSet => Option[B2],
    extractTo3: WrappedResultSet => Option[B3],
    extractTo4: WrappedResultSet => Option[B4],
    extractTo5: WrappedResultSet => Option[B5],
    extractTo6: WrappedResultSet => Option[B6],
    extractTo7: WrappedResultSet => Option[B7],
    extractTo8: WrappedResultSet => Option[B8],
    extractTo9: WrappedResultSet => Option[B9],
    extractTo10: WrappedResultSet => Option[B10],
    extractTo11: WrappedResultSet => Option[B11],
    extractTo12: WrappedResultSet => Option[B12],
    extractTo13: WrappedResultSet => Option[B13])(
    transform: (A, Seq[B1], Seq[B2], Seq[B3], Seq[B4], Seq[B5], Seq[B6], Seq[B7], Seq[B8], Seq[B9], Seq[B10], Seq[B11], Seq[B12], Seq[B13]) => Z)(
    implicit
    cxt: EC = ECGlobal): Future[Iterable[Z]] = {
    val _parameters = ensureAndNormalizeParameters(parameters)
    withListeners(statement, _parameters) {
      queryLogging(statement, _parameters)

      def processResultSet(
        result: LinkedHashMap[A, (Seq[B1], Seq[B2], Seq[B3], Seq[B4], Seq[B5], Seq[B6], Seq[B7], Seq[B8], Seq[B9], Seq[B10], Seq[B11], Seq[B12], Seq[B13])],
        rs: WrappedResultSet): LinkedHashMap[A, (Seq[B1], Seq[B2], Seq[B3], Seq[B4], Seq[B5], Seq[B6], Seq[B7], Seq[B8], Seq[B9], Seq[B10], Seq[B11], Seq[B12], Seq[B13])] = {
        val o = extractOne(rs)
        val (to1, to2, to3, to4, to5, to6, to7, to8, to9, to10, to11, to12, to13) = (
          extractTo1(rs),
          extractTo2(rs),
          extractTo3(rs),
          extractTo4(rs),
          extractTo5(rs),
          extractTo6(rs),
          extractTo7(rs),
          extractTo8(rs),
          extractTo9(rs),
          extractTo10(rs),
          extractTo11(rs),
          extractTo12(rs),
          extractTo13(rs))
        result.keys.find(_ == o).map { _ =>
          to1.orElse(to2).orElse(to3).orElse(to4).orElse(to5).orElse(to6).orElse(to7).orElse(to8).orElse(to9).orElse(to10).orElse(to11).orElse(to12).orElse(to13).map { _ =>
            val (ts1, ts2, ts3, ts4, ts5, ts6, ts7, ts8, ts9, ts10, ts11, ts12, ts13) = result.apply(o)
            result += (o -> (
              to1.map(t => if (ts1.contains(t)) ts1 else ts1 :+ t).getOrElse(ts1),
              to2.map(t => if (ts2.contains(t)) ts2 else ts2 :+ t).getOrElse(ts2),
              to3.map(t => if (ts3.contains(t)) ts3 else ts3 :+ t).getOrElse(ts3),
              to4.map(t => if (ts4.contains(t)) ts4 else ts4 :+ t).getOrElse(ts4),
              to5.map(t => if (ts5.contains(t)) ts5 else ts5 :+ t).getOrElse(ts5),
              to6.map(t => if (ts6.contains(t)) ts6 else ts6 :+ t).getOrElse(ts6),
              to7.map(t => if (ts7.contains(t)) ts7 else ts7 :+ t).getOrElse(ts7),
              to8.map(t => if (ts8.contains(t)) ts8 else ts8 :+ t).getOrElse(ts8),
              to9.map(t => if (ts9.contains(t)) ts9 else ts9 :+ t).getOrElse(ts9),
              to10.map(t => if (ts10.contains(t)) ts10 else ts10 :+ t).getOrElse(ts10),
              to11.map(t => if (ts11.contains(t)) ts11 else ts11 :+ t).getOrElse(ts11),
              to12.map(t => if (ts12.contains(t)) ts12 else ts12 :+ t).getOrElse(ts12),
              to13.map(t => if (ts13.contains(t)) ts13 else ts13 :+ t).getOrElse(ts13)))
          }.getOrElse(result)
        }.getOrElse {
          result += (
            o -> (
              to1.map(t => Vector(t)).getOrElse(Vector()),
              to2.map(t => Vector(t)).getOrElse(Vector()),
              to3.map(t => Vector(t)).getOrElse(Vector()),
              to4.map(t => Vector(t)).getOrElse(Vector()),
              to5.map(t => Vector(t)).getOrElse(Vector()),
              to6.map(t => Vector(t)).getOrElse(Vector()),
              to7.map(t => Vector(t)).getOrElse(Vector()),
              to8.map(t => Vector(t)).getOrElse(Vector()),
              to9.map(t => Vector(t)).getOrElse(Vector()),
              to10.map(t => Vector(t)).getOrElse(Vector()),
              to11.map(t => Vector(t)).getOrElse(Vector()),
              to12.map(t => Vector(t)).getOrElse(Vector()),
              to13.map(t => Vector(t)).getOrElse(Vector())))
        }
      }

      connection.sendPreparedStatement(statement, _parameters: _*).map { result =>
        result.rows.map { ars =>
          new AsyncResultSetIterator(ars).foldLeft(
            LinkedHashMap[A, (Seq[B1], Seq[B2], Seq[B3], Seq[B4], Seq[B5], Seq[B6], Seq[B7], Seq[B8], Seq[B9], Seq[B10], Seq[B11], Seq[B12], Seq[B13])]())(processResultSet).map {
              case (one, (t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11, t12, t13)) => transform(one, t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11, t12, t13)
            }
        }.getOrElse(Nil)
      }
    }
  }

  @deprecated("will be removed. use oneToManies14Iterable", "0.12.0")
  def oneToManies14Traversable[A, B1, B2, B3, B4, B5, B6, B7, B8, B9, B10, B11, B12, B13, B14, Z](
    statement: String,
    parameters: Any*)(
    extractOne: WrappedResultSet => A)(
    extractTo1: WrappedResultSet => Option[B1],
    extractTo2: WrappedResultSet => Option[B2],
    extractTo3: WrappedResultSet => Option[B3],
    extractTo4: WrappedResultSet => Option[B4],
    extractTo5: WrappedResultSet => Option[B5],
    extractTo6: WrappedResultSet => Option[B6],
    extractTo7: WrappedResultSet => Option[B7],
    extractTo8: WrappedResultSet => Option[B8],
    extractTo9: WrappedResultSet => Option[B9],
    extractTo10: WrappedResultSet => Option[B10],
    extractTo11: WrappedResultSet => Option[B11],
    extractTo12: WrappedResultSet => Option[B12],
    extractTo13: WrappedResultSet => Option[B13],
    extractTo14: WrappedResultSet => Option[B14])(
    transform: (A, Seq[B1], Seq[B2], Seq[B3], Seq[B4], Seq[B5], Seq[B6], Seq[B7], Seq[B8], Seq[B9], Seq[B10], Seq[B11], Seq[B12], Seq[B13], Seq[B14]) => Z)(
    implicit
    cxt: EC = ECGlobal): Future[Iterable[Z]] =
    oneToManies14Iterable[A, B1, B2, B3, B4, B5, B6, B7, B8, B9, B10, B11, B12, B13, B14, Z](statement, parameters: _*)(extractOne)(
      extractTo1,
      extractTo2,
      extractTo3,
      extractTo4,
      extractTo5,
      extractTo6,
      extractTo7,
      extractTo8,
      extractTo9,
      extractTo10,
      extractTo11,
      extractTo12,
      extractTo13,
      extractTo14)(transform)
  def oneToManies14Iterable[A, B1, B2, B3, B4, B5, B6, B7, B8, B9, B10, B11, B12, B13, B14, Z](
    statement: String,
    parameters: Any*)(
    extractOne: WrappedResultSet => A)(
    extractTo1: WrappedResultSet => Option[B1],
    extractTo2: WrappedResultSet => Option[B2],
    extractTo3: WrappedResultSet => Option[B3],
    extractTo4: WrappedResultSet => Option[B4],
    extractTo5: WrappedResultSet => Option[B5],
    extractTo6: WrappedResultSet => Option[B6],
    extractTo7: WrappedResultSet => Option[B7],
    extractTo8: WrappedResultSet => Option[B8],
    extractTo9: WrappedResultSet => Option[B9],
    extractTo10: WrappedResultSet => Option[B10],
    extractTo11: WrappedResultSet => Option[B11],
    extractTo12: WrappedResultSet => Option[B12],
    extractTo13: WrappedResultSet => Option[B13],
    extractTo14: WrappedResultSet => Option[B14])(
    transform: (A, Seq[B1], Seq[B2], Seq[B3], Seq[B4], Seq[B5], Seq[B6], Seq[B7], Seq[B8], Seq[B9], Seq[B10], Seq[B11], Seq[B12], Seq[B13], Seq[B14]) => Z)(
    implicit
    cxt: EC = ECGlobal): Future[Iterable[Z]] = {
    val _parameters = ensureAndNormalizeParameters(parameters)
    withListeners(statement, _parameters) {
      queryLogging(statement, _parameters)

      def processResultSet(
        result: LinkedHashMap[A, (Seq[B1], Seq[B2], Seq[B3], Seq[B4], Seq[B5], Seq[B6], Seq[B7], Seq[B8], Seq[B9], Seq[B10], Seq[B11], Seq[B12], Seq[B13], Seq[B14])],
        rs: WrappedResultSet): LinkedHashMap[A, (Seq[B1], Seq[B2], Seq[B3], Seq[B4], Seq[B5], Seq[B6], Seq[B7], Seq[B8], Seq[B9], Seq[B10], Seq[B11], Seq[B12], Seq[B13], Seq[B14])] = {
        val o = extractOne(rs)
        val (to1, to2, to3, to4, to5, to6, to7, to8, to9, to10, to11, to12, to13, to14) = (
          extractTo1(rs),
          extractTo2(rs),
          extractTo3(rs),
          extractTo4(rs),
          extractTo5(rs),
          extractTo6(rs),
          extractTo7(rs),
          extractTo8(rs),
          extractTo9(rs),
          extractTo10(rs),
          extractTo11(rs),
          extractTo12(rs),
          extractTo13(rs),
          extractTo14(rs))
        result.keys.find(_ == o).map { _ =>
          to1.orElse(to2).orElse(to3).orElse(to4).orElse(to5).orElse(to6).orElse(to7).orElse(to8).orElse(to9).orElse(to10).orElse(to11).orElse(to12).orElse(to13).orElse(to14).map { _ =>
            val (ts1, ts2, ts3, ts4, ts5, ts6, ts7, ts8, ts9, ts10, ts11, ts12, ts13, ts14) = result.apply(o)
            result += (o -> (
              to1.map(t => if (ts1.contains(t)) ts1 else ts1 :+ t).getOrElse(ts1),
              to2.map(t => if (ts2.contains(t)) ts2 else ts2 :+ t).getOrElse(ts2),
              to3.map(t => if (ts3.contains(t)) ts3 else ts3 :+ t).getOrElse(ts3),
              to4.map(t => if (ts4.contains(t)) ts4 else ts4 :+ t).getOrElse(ts4),
              to5.map(t => if (ts5.contains(t)) ts5 else ts5 :+ t).getOrElse(ts5),
              to6.map(t => if (ts6.contains(t)) ts6 else ts6 :+ t).getOrElse(ts6),
              to7.map(t => if (ts7.contains(t)) ts7 else ts7 :+ t).getOrElse(ts7),
              to8.map(t => if (ts8.contains(t)) ts8 else ts8 :+ t).getOrElse(ts8),
              to9.map(t => if (ts9.contains(t)) ts9 else ts9 :+ t).getOrElse(ts9),
              to10.map(t => if (ts10.contains(t)) ts10 else ts10 :+ t).getOrElse(ts10),
              to11.map(t => if (ts11.contains(t)) ts11 else ts11 :+ t).getOrElse(ts11),
              to12.map(t => if (ts12.contains(t)) ts12 else ts12 :+ t).getOrElse(ts12),
              to13.map(t => if (ts13.contains(t)) ts13 else ts13 :+ t).getOrElse(ts13),
              to14.map(t => if (ts14.contains(t)) ts14 else ts14 :+ t).getOrElse(ts14)))
          }.getOrElse(result)
        }.getOrElse {
          result += (
            o -> (
              to1.map(t => Vector(t)).getOrElse(Vector()),
              to2.map(t => Vector(t)).getOrElse(Vector()),
              to3.map(t => Vector(t)).getOrElse(Vector()),
              to4.map(t => Vector(t)).getOrElse(Vector()),
              to5.map(t => Vector(t)).getOrElse(Vector()),
              to6.map(t => Vector(t)).getOrElse(Vector()),
              to7.map(t => Vector(t)).getOrElse(Vector()),
              to8.map(t => Vector(t)).getOrElse(Vector()),
              to9.map(t => Vector(t)).getOrElse(Vector()),
              to10.map(t => Vector(t)).getOrElse(Vector()),
              to11.map(t => Vector(t)).getOrElse(Vector()),
              to12.map(t => Vector(t)).getOrElse(Vector()),
              to13.map(t => Vector(t)).getOrElse(Vector()),
              to14.map(t => Vector(t)).getOrElse(Vector())))
        }
      }

      connection.sendPreparedStatement(statement, _parameters: _*).map { result =>
        result.rows.map { ars =>
          new AsyncResultSetIterator(ars).foldLeft(
            LinkedHashMap[A, (Seq[B1], Seq[B2], Seq[B3], Seq[B4], Seq[B5], Seq[B6], Seq[B7], Seq[B8], Seq[B9], Seq[B10], Seq[B11], Seq[B12], Seq[B13], Seq[B14])]())(processResultSet).map {
              case (one, (t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11, t12, t13, t14)) => transform(one, t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11, t12, t13, t14)
            }
        }.getOrElse(Nil)
      }
    }
  }

  @deprecated("will be removed. use oneToManies15Iterable", "0.12.0")
  def oneToManies15Traversable[A, B1, B2, B3, B4, B5, B6, B7, B8, B9, B10, B11, B12, B13, B14, B15, Z](
    statement: String,
    parameters: Any*)(
    extractOne: WrappedResultSet => A)(
    extractTo1: WrappedResultSet => Option[B1],
    extractTo2: WrappedResultSet => Option[B2],
    extractTo3: WrappedResultSet => Option[B3],
    extractTo4: WrappedResultSet => Option[B4],
    extractTo5: WrappedResultSet => Option[B5],
    extractTo6: WrappedResultSet => Option[B6],
    extractTo7: WrappedResultSet => Option[B7],
    extractTo8: WrappedResultSet => Option[B8],
    extractTo9: WrappedResultSet => Option[B9],
    extractTo10: WrappedResultSet => Option[B10],
    extractTo11: WrappedResultSet => Option[B11],
    extractTo12: WrappedResultSet => Option[B12],
    extractTo13: WrappedResultSet => Option[B13],
    extractTo14: WrappedResultSet => Option[B14],
    extractTo15: WrappedResultSet => Option[B15])(
    transform: (A, Seq[B1], Seq[B2], Seq[B3], Seq[B4], Seq[B5], Seq[B6], Seq[B7], Seq[B8], Seq[B9], Seq[B10], Seq[B11], Seq[B12], Seq[B13], Seq[B14], Seq[B15]) => Z)(
    implicit
    cxt: EC = ECGlobal): Future[Iterable[Z]] =
    oneToManies15Iterable[A, B1, B2, B3, B4, B5, B6, B7, B8, B9, B10, B11, B12, B13, B14, B15, Z](statement, parameters: _*)(extractOne)(
      extractTo1,
      extractTo2,
      extractTo3,
      extractTo4,
      extractTo5,
      extractTo6,
      extractTo7,
      extractTo8,
      extractTo9,
      extractTo10,
      extractTo11,
      extractTo12,
      extractTo13,
      extractTo14,
      extractTo15)(transform)
  def oneToManies15Iterable[A, B1, B2, B3, B4, B5, B6, B7, B8, B9, B10, B11, B12, B13, B14, B15, Z](
    statement: String,
    parameters: Any*)(
    extractOne: WrappedResultSet => A)(
    extractTo1: WrappedResultSet => Option[B1],
    extractTo2: WrappedResultSet => Option[B2],
    extractTo3: WrappedResultSet => Option[B3],
    extractTo4: WrappedResultSet => Option[B4],
    extractTo5: WrappedResultSet => Option[B5],
    extractTo6: WrappedResultSet => Option[B6],
    extractTo7: WrappedResultSet => Option[B7],
    extractTo8: WrappedResultSet => Option[B8],
    extractTo9: WrappedResultSet => Option[B9],
    extractTo10: WrappedResultSet => Option[B10],
    extractTo11: WrappedResultSet => Option[B11],
    extractTo12: WrappedResultSet => Option[B12],
    extractTo13: WrappedResultSet => Option[B13],
    extractTo14: WrappedResultSet => Option[B14],
    extractTo15: WrappedResultSet => Option[B15])(
    transform: (A, Seq[B1], Seq[B2], Seq[B3], Seq[B4], Seq[B5], Seq[B6], Seq[B7], Seq[B8], Seq[B9], Seq[B10], Seq[B11], Seq[B12], Seq[B13], Seq[B14], Seq[B15]) => Z)(
    implicit
    cxt: EC = ECGlobal): Future[Iterable[Z]] = {
    val _parameters = ensureAndNormalizeParameters(parameters)
    withListeners(statement, _parameters) {
      queryLogging(statement, _parameters)

      def processResultSet(
        result: LinkedHashMap[A, (Seq[B1], Seq[B2], Seq[B3], Seq[B4], Seq[B5], Seq[B6], Seq[B7], Seq[B8], Seq[B9], Seq[B10], Seq[B11], Seq[B12], Seq[B13], Seq[B14], Seq[B15])],
        rs: WrappedResultSet): LinkedHashMap[A, (Seq[B1], Seq[B2], Seq[B3], Seq[B4], Seq[B5], Seq[B6], Seq[B7], Seq[B8], Seq[B9], Seq[B10], Seq[B11], Seq[B12], Seq[B13], Seq[B14], Seq[B15])] = {
        val o = extractOne(rs)
        val (to1, to2, to3, to4, to5, to6, to7, to8, to9, to10, to11, to12, to13, to14, to15) = (
          extractTo1(rs),
          extractTo2(rs),
          extractTo3(rs),
          extractTo4(rs),
          extractTo5(rs),
          extractTo6(rs),
          extractTo7(rs),
          extractTo8(rs),
          extractTo9(rs),
          extractTo10(rs),
          extractTo11(rs),
          extractTo12(rs),
          extractTo13(rs),
          extractTo14(rs),
          extractTo15(rs))
        result.keys.find(_ == o).map { _ =>
          to1.orElse(to2).orElse(to3).orElse(to4).orElse(to5).orElse(to6).orElse(to7).orElse(to8).orElse(to9).orElse(to10).orElse(to11).orElse(to12).orElse(to13).orElse(to14).orElse(to15).map { _ =>
            val (ts1, ts2, ts3, ts4, ts5, ts6, ts7, ts8, ts9, ts10, ts11, ts12, ts13, ts14, ts15) = result.apply(o)
            result += (o -> (
              to1.map(t => if (ts1.contains(t)) ts1 else ts1 :+ t).getOrElse(ts1),
              to2.map(t => if (ts2.contains(t)) ts2 else ts2 :+ t).getOrElse(ts2),
              to3.map(t => if (ts3.contains(t)) ts3 else ts3 :+ t).getOrElse(ts3),
              to4.map(t => if (ts4.contains(t)) ts4 else ts4 :+ t).getOrElse(ts4),
              to5.map(t => if (ts5.contains(t)) ts5 else ts5 :+ t).getOrElse(ts5),
              to6.map(t => if (ts6.contains(t)) ts6 else ts6 :+ t).getOrElse(ts6),
              to7.map(t => if (ts7.contains(t)) ts7 else ts7 :+ t).getOrElse(ts7),
              to8.map(t => if (ts8.contains(t)) ts8 else ts8 :+ t).getOrElse(ts8),
              to9.map(t => if (ts9.contains(t)) ts9 else ts9 :+ t).getOrElse(ts9),
              to10.map(t => if (ts10.contains(t)) ts10 else ts10 :+ t).getOrElse(ts10),
              to11.map(t => if (ts11.contains(t)) ts11 else ts11 :+ t).getOrElse(ts11),
              to12.map(t => if (ts12.contains(t)) ts12 else ts12 :+ t).getOrElse(ts12),
              to13.map(t => if (ts13.contains(t)) ts13 else ts13 :+ t).getOrElse(ts13),
              to14.map(t => if (ts14.contains(t)) ts14 else ts14 :+ t).getOrElse(ts14),
              to15.map(t => if (ts15.contains(t)) ts15 else ts15 :+ t).getOrElse(ts15)))
          }.getOrElse(result)
        }.getOrElse {
          result += (
            o -> (
              to1.map(t => Vector(t)).getOrElse(Vector()),
              to2.map(t => Vector(t)).getOrElse(Vector()),
              to3.map(t => Vector(t)).getOrElse(Vector()),
              to4.map(t => Vector(t)).getOrElse(Vector()),
              to5.map(t => Vector(t)).getOrElse(Vector()),
              to6.map(t => Vector(t)).getOrElse(Vector()),
              to7.map(t => Vector(t)).getOrElse(Vector()),
              to8.map(t => Vector(t)).getOrElse(Vector()),
              to9.map(t => Vector(t)).getOrElse(Vector()),
              to10.map(t => Vector(t)).getOrElse(Vector()),
              to11.map(t => Vector(t)).getOrElse(Vector()),
              to12.map(t => Vector(t)).getOrElse(Vector()),
              to13.map(t => Vector(t)).getOrElse(Vector()),
              to14.map(t => Vector(t)).getOrElse(Vector()),
              to15.map(t => Vector(t)).getOrElse(Vector())))
        }
      }

      connection.sendPreparedStatement(statement, _parameters: _*).map { result =>
        result.rows.map { ars =>
          new AsyncResultSetIterator(ars).foldLeft(
            LinkedHashMap[A, (Seq[B1], Seq[B2], Seq[B3], Seq[B4], Seq[B5], Seq[B6], Seq[B7], Seq[B8], Seq[B9], Seq[B10], Seq[B11], Seq[B12], Seq[B13], Seq[B14], Seq[B15])]())(processResultSet).map {
              case (one, (t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11, t12, t13, t14, t15)) => transform(one, t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11, t12, t13, t14, t15)
            }
        }.getOrElse(Nil)
      }
    }
  }

  @deprecated("will be removed. use oneToManies16Iterable", "0.12.0")
  def oneToManies16Traversable[A, B1, B2, B3, B4, B5, B6, B7, B8, B9, B10, B11, B12, B13, B14, B15, B16, Z](
    statement: String,
    parameters: Any*)(
    extractOne: WrappedResultSet => A)(
    extractTo1: WrappedResultSet => Option[B1],
    extractTo2: WrappedResultSet => Option[B2],
    extractTo3: WrappedResultSet => Option[B3],
    extractTo4: WrappedResultSet => Option[B4],
    extractTo5: WrappedResultSet => Option[B5],
    extractTo6: WrappedResultSet => Option[B6],
    extractTo7: WrappedResultSet => Option[B7],
    extractTo8: WrappedResultSet => Option[B8],
    extractTo9: WrappedResultSet => Option[B9],
    extractTo10: WrappedResultSet => Option[B10],
    extractTo11: WrappedResultSet => Option[B11],
    extractTo12: WrappedResultSet => Option[B12],
    extractTo13: WrappedResultSet => Option[B13],
    extractTo14: WrappedResultSet => Option[B14],
    extractTo15: WrappedResultSet => Option[B15],
    extractTo16: WrappedResultSet => Option[B16])(
    transform: (A, Seq[B1], Seq[B2], Seq[B3], Seq[B4], Seq[B5], Seq[B6], Seq[B7], Seq[B8], Seq[B9], Seq[B10], Seq[B11], Seq[B12], Seq[B13], Seq[B14], Seq[B15], Seq[B16]) => Z)(
    implicit
    cxt: EC = ECGlobal): Future[Iterable[Z]] =
    oneToManies16Iterable[A, B1, B2, B3, B4, B5, B6, B7, B8, B9, B10, B11, B12, B13, B14, B15, B16, Z](statement, parameters: _*)(extractOne)(
      extractTo1,
      extractTo2,
      extractTo3,
      extractTo4,
      extractTo5,
      extractTo6,
      extractTo7,
      extractTo8,
      extractTo9,
      extractTo10,
      extractTo11,
      extractTo12,
      extractTo13,
      extractTo14,
      extractTo15,
      extractTo16)(transform)
  def oneToManies16Iterable[A, B1, B2, B3, B4, B5, B6, B7, B8, B9, B10, B11, B12, B13, B14, B15, B16, Z](
    statement: String,
    parameters: Any*)(
    extractOne: WrappedResultSet => A)(
    extractTo1: WrappedResultSet => Option[B1],
    extractTo2: WrappedResultSet => Option[B2],
    extractTo3: WrappedResultSet => Option[B3],
    extractTo4: WrappedResultSet => Option[B4],
    extractTo5: WrappedResultSet => Option[B5],
    extractTo6: WrappedResultSet => Option[B6],
    extractTo7: WrappedResultSet => Option[B7],
    extractTo8: WrappedResultSet => Option[B8],
    extractTo9: WrappedResultSet => Option[B9],
    extractTo10: WrappedResultSet => Option[B10],
    extractTo11: WrappedResultSet => Option[B11],
    extractTo12: WrappedResultSet => Option[B12],
    extractTo13: WrappedResultSet => Option[B13],
    extractTo14: WrappedResultSet => Option[B14],
    extractTo15: WrappedResultSet => Option[B15],
    extractTo16: WrappedResultSet => Option[B16])(
    transform: (A, Seq[B1], Seq[B2], Seq[B3], Seq[B4], Seq[B5], Seq[B6], Seq[B7], Seq[B8], Seq[B9], Seq[B10], Seq[B11], Seq[B12], Seq[B13], Seq[B14], Seq[B15], Seq[B16]) => Z)(
    implicit
    cxt: EC = ECGlobal): Future[Iterable[Z]] = {
    val _parameters = ensureAndNormalizeParameters(parameters)
    withListeners(statement, _parameters) {
      queryLogging(statement, _parameters)

      def processResultSet(
        result: LinkedHashMap[A, (Seq[B1], Seq[B2], Seq[B3], Seq[B4], Seq[B5], Seq[B6], Seq[B7], Seq[B8], Seq[B9], Seq[B10], Seq[B11], Seq[B12], Seq[B13], Seq[B14], Seq[B15], Seq[B16])],
        rs: WrappedResultSet): LinkedHashMap[A, (Seq[B1], Seq[B2], Seq[B3], Seq[B4], Seq[B5], Seq[B6], Seq[B7], Seq[B8], Seq[B9], Seq[B10], Seq[B11], Seq[B12], Seq[B13], Seq[B14], Seq[B15], Seq[B16])] = {
        val o = extractOne(rs)
        val (to1, to2, to3, to4, to5, to6, to7, to8, to9, to10, to11, to12, to13, to14, to15, to16) = (
          extractTo1(rs),
          extractTo2(rs),
          extractTo3(rs),
          extractTo4(rs),
          extractTo5(rs),
          extractTo6(rs),
          extractTo7(rs),
          extractTo8(rs),
          extractTo9(rs),
          extractTo10(rs),
          extractTo11(rs),
          extractTo12(rs),
          extractTo13(rs),
          extractTo14(rs),
          extractTo15(rs),
          extractTo16(rs))
        result.keys.find(_ == o).map { _ =>
          to1.orElse(to2).orElse(to3).orElse(to4).orElse(to5).orElse(to6).orElse(to7).orElse(to8).orElse(to9).orElse(to10).orElse(to11).orElse(to12).orElse(to13).orElse(to14).orElse(to15).orElse(to16).map { _ =>
            val (ts1, ts2, ts3, ts4, ts5, ts6, ts7, ts8, ts9, ts10, ts11, ts12, ts13, ts14, ts15, ts16) = result.apply(o)
            result += (o -> (
              to1.map(t => if (ts1.contains(t)) ts1 else ts1 :+ t).getOrElse(ts1),
              to2.map(t => if (ts2.contains(t)) ts2 else ts2 :+ t).getOrElse(ts2),
              to3.map(t => if (ts3.contains(t)) ts3 else ts3 :+ t).getOrElse(ts3),
              to4.map(t => if (ts4.contains(t)) ts4 else ts4 :+ t).getOrElse(ts4),
              to5.map(t => if (ts5.contains(t)) ts5 else ts5 :+ t).getOrElse(ts5),
              to6.map(t => if (ts6.contains(t)) ts6 else ts6 :+ t).getOrElse(ts6),
              to7.map(t => if (ts7.contains(t)) ts7 else ts7 :+ t).getOrElse(ts7),
              to8.map(t => if (ts8.contains(t)) ts8 else ts8 :+ t).getOrElse(ts8),
              to9.map(t => if (ts9.contains(t)) ts9 else ts9 :+ t).getOrElse(ts9),
              to10.map(t => if (ts10.contains(t)) ts10 else ts10 :+ t).getOrElse(ts10),
              to11.map(t => if (ts11.contains(t)) ts11 else ts11 :+ t).getOrElse(ts11),
              to12.map(t => if (ts12.contains(t)) ts12 else ts12 :+ t).getOrElse(ts12),
              to13.map(t => if (ts13.contains(t)) ts13 else ts13 :+ t).getOrElse(ts13),
              to14.map(t => if (ts14.contains(t)) ts14 else ts14 :+ t).getOrElse(ts14),
              to15.map(t => if (ts15.contains(t)) ts15 else ts15 :+ t).getOrElse(ts15),
              to16.map(t => if (ts16.contains(t)) ts16 else ts16 :+ t).getOrElse(ts16)))
          }.getOrElse(result)
        }.getOrElse {
          result += (
            o -> (
              to1.map(t => Vector(t)).getOrElse(Vector()),
              to2.map(t => Vector(t)).getOrElse(Vector()),
              to3.map(t => Vector(t)).getOrElse(Vector()),
              to4.map(t => Vector(t)).getOrElse(Vector()),
              to5.map(t => Vector(t)).getOrElse(Vector()),
              to6.map(t => Vector(t)).getOrElse(Vector()),
              to7.map(t => Vector(t)).getOrElse(Vector()),
              to8.map(t => Vector(t)).getOrElse(Vector()),
              to9.map(t => Vector(t)).getOrElse(Vector()),
              to10.map(t => Vector(t)).getOrElse(Vector()),
              to11.map(t => Vector(t)).getOrElse(Vector()),
              to12.map(t => Vector(t)).getOrElse(Vector()),
              to13.map(t => Vector(t)).getOrElse(Vector()),
              to14.map(t => Vector(t)).getOrElse(Vector()),
              to15.map(t => Vector(t)).getOrElse(Vector()),
              to16.map(t => Vector(t)).getOrElse(Vector())))
        }
      }

      connection.sendPreparedStatement(statement, _parameters: _*).map { result =>
        result.rows.map { ars =>
          new AsyncResultSetIterator(ars).foldLeft(
            LinkedHashMap[A, (Seq[B1], Seq[B2], Seq[B3], Seq[B4], Seq[B5], Seq[B6], Seq[B7], Seq[B8], Seq[B9], Seq[B10], Seq[B11], Seq[B12], Seq[B13], Seq[B14], Seq[B15], Seq[B16])]())(processResultSet).map {
              case (one, (t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11, t12, t13, t14, t15, t16)) => transform(one, t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11, t12, t13, t14, t15, t16)
            }
        }.getOrElse(Nil)
      }
    }
  }

  @deprecated("will be removed. use oneToManies17Iterable", "0.12.0")
  def oneToManies17Traversable[A, B1, B2, B3, B4, B5, B6, B7, B8, B9, B10, B11, B12, B13, B14, B15, B16, B17, Z](
    statement: String,
    parameters: Any*)(
    extractOne: WrappedResultSet => A)(
    extractTo1: WrappedResultSet => Option[B1],
    extractTo2: WrappedResultSet => Option[B2],
    extractTo3: WrappedResultSet => Option[B3],
    extractTo4: WrappedResultSet => Option[B4],
    extractTo5: WrappedResultSet => Option[B5],
    extractTo6: WrappedResultSet => Option[B6],
    extractTo7: WrappedResultSet => Option[B7],
    extractTo8: WrappedResultSet => Option[B8],
    extractTo9: WrappedResultSet => Option[B9],
    extractTo10: WrappedResultSet => Option[B10],
    extractTo11: WrappedResultSet => Option[B11],
    extractTo12: WrappedResultSet => Option[B12],
    extractTo13: WrappedResultSet => Option[B13],
    extractTo14: WrappedResultSet => Option[B14],
    extractTo15: WrappedResultSet => Option[B15],
    extractTo16: WrappedResultSet => Option[B16],
    extractTo17: WrappedResultSet => Option[B17])(
    transform: (A, Seq[B1], Seq[B2], Seq[B3], Seq[B4], Seq[B5], Seq[B6], Seq[B7], Seq[B8], Seq[B9], Seq[B10], Seq[B11], Seq[B12], Seq[B13], Seq[B14], Seq[B15], Seq[B16], Seq[B17]) => Z)(
    implicit
    cxt: EC = ECGlobal): Future[Iterable[Z]] =
    oneToManies17Iterable[A, B1, B2, B3, B4, B5, B6, B7, B8, B9, B10, B11, B12, B13, B14, B15, B16, B17, Z](statement, parameters: _*)(extractOne)(
      extractTo1,
      extractTo2,
      extractTo3,
      extractTo4,
      extractTo5,
      extractTo6,
      extractTo7,
      extractTo8,
      extractTo9,
      extractTo10,
      extractTo11,
      extractTo12,
      extractTo13,
      extractTo14,
      extractTo15,
      extractTo16,
      extractTo17)(transform)
  def oneToManies17Iterable[A, B1, B2, B3, B4, B5, B6, B7, B8, B9, B10, B11, B12, B13, B14, B15, B16, B17, Z](
    statement: String,
    parameters: Any*)(
    extractOne: WrappedResultSet => A)(
    extractTo1: WrappedResultSet => Option[B1],
    extractTo2: WrappedResultSet => Option[B2],
    extractTo3: WrappedResultSet => Option[B3],
    extractTo4: WrappedResultSet => Option[B4],
    extractTo5: WrappedResultSet => Option[B5],
    extractTo6: WrappedResultSet => Option[B6],
    extractTo7: WrappedResultSet => Option[B7],
    extractTo8: WrappedResultSet => Option[B8],
    extractTo9: WrappedResultSet => Option[B9],
    extractTo10: WrappedResultSet => Option[B10],
    extractTo11: WrappedResultSet => Option[B11],
    extractTo12: WrappedResultSet => Option[B12],
    extractTo13: WrappedResultSet => Option[B13],
    extractTo14: WrappedResultSet => Option[B14],
    extractTo15: WrappedResultSet => Option[B15],
    extractTo16: WrappedResultSet => Option[B16],
    extractTo17: WrappedResultSet => Option[B17])(
    transform: (A, Seq[B1], Seq[B2], Seq[B3], Seq[B4], Seq[B5], Seq[B6], Seq[B7], Seq[B8], Seq[B9], Seq[B10], Seq[B11], Seq[B12], Seq[B13], Seq[B14], Seq[B15], Seq[B16], Seq[B17]) => Z)(
    implicit
    cxt: EC = ECGlobal): Future[Iterable[Z]] = {
    val _parameters = ensureAndNormalizeParameters(parameters)
    withListeners(statement, _parameters) {
      queryLogging(statement, _parameters)

      def processResultSet(
        result: LinkedHashMap[A, (Seq[B1], Seq[B2], Seq[B3], Seq[B4], Seq[B5], Seq[B6], Seq[B7], Seq[B8], Seq[B9], Seq[B10], Seq[B11], Seq[B12], Seq[B13], Seq[B14], Seq[B15], Seq[B16], Seq[B17])],
        rs: WrappedResultSet): LinkedHashMap[A, (Seq[B1], Seq[B2], Seq[B3], Seq[B4], Seq[B5], Seq[B6], Seq[B7], Seq[B8], Seq[B9], Seq[B10], Seq[B11], Seq[B12], Seq[B13], Seq[B14], Seq[B15], Seq[B16], Seq[B17])] = {
        val o = extractOne(rs)
        val (to1, to2, to3, to4, to5, to6, to7, to8, to9, to10, to11, to12, to13, to14, to15, to16, to17) = (
          extractTo1(rs),
          extractTo2(rs),
          extractTo3(rs),
          extractTo4(rs),
          extractTo5(rs),
          extractTo6(rs),
          extractTo7(rs),
          extractTo8(rs),
          extractTo9(rs),
          extractTo10(rs),
          extractTo11(rs),
          extractTo12(rs),
          extractTo13(rs),
          extractTo14(rs),
          extractTo15(rs),
          extractTo16(rs),
          extractTo17(rs))
        result.keys.find(_ == o).map { _ =>
          to1.orElse(to2).orElse(to3).orElse(to4).orElse(to5).orElse(to6).orElse(to7).orElse(to8).orElse(to9).orElse(to10).orElse(to11).orElse(to12).orElse(to13).orElse(to14).orElse(to15).orElse(to16).orElse(to17).map { _ =>
            val (ts1, ts2, ts3, ts4, ts5, ts6, ts7, ts8, ts9, ts10, ts11, ts12, ts13, ts14, ts15, ts16, ts17) = result.apply(o)
            result += (o -> (
              to1.map(t => if (ts1.contains(t)) ts1 else ts1 :+ t).getOrElse(ts1),
              to2.map(t => if (ts2.contains(t)) ts2 else ts2 :+ t).getOrElse(ts2),
              to3.map(t => if (ts3.contains(t)) ts3 else ts3 :+ t).getOrElse(ts3),
              to4.map(t => if (ts4.contains(t)) ts4 else ts4 :+ t).getOrElse(ts4),
              to5.map(t => if (ts5.contains(t)) ts5 else ts5 :+ t).getOrElse(ts5),
              to6.map(t => if (ts6.contains(t)) ts6 else ts6 :+ t).getOrElse(ts6),
              to7.map(t => if (ts7.contains(t)) ts7 else ts7 :+ t).getOrElse(ts7),
              to8.map(t => if (ts8.contains(t)) ts8 else ts8 :+ t).getOrElse(ts8),
              to9.map(t => if (ts9.contains(t)) ts9 else ts9 :+ t).getOrElse(ts9),
              to10.map(t => if (ts10.contains(t)) ts10 else ts10 :+ t).getOrElse(ts10),
              to11.map(t => if (ts11.contains(t)) ts11 else ts11 :+ t).getOrElse(ts11),
              to12.map(t => if (ts12.contains(t)) ts12 else ts12 :+ t).getOrElse(ts12),
              to13.map(t => if (ts13.contains(t)) ts13 else ts13 :+ t).getOrElse(ts13),
              to14.map(t => if (ts14.contains(t)) ts14 else ts14 :+ t).getOrElse(ts14),
              to15.map(t => if (ts15.contains(t)) ts15 else ts15 :+ t).getOrElse(ts15),
              to16.map(t => if (ts16.contains(t)) ts16 else ts16 :+ t).getOrElse(ts16),
              to17.map(t => if (ts17.contains(t)) ts17 else ts17 :+ t).getOrElse(ts17)))
          }.getOrElse(result)
        }.getOrElse {
          result += (
            o -> (
              to1.map(t => Vector(t)).getOrElse(Vector()),
              to2.map(t => Vector(t)).getOrElse(Vector()),
              to3.map(t => Vector(t)).getOrElse(Vector()),
              to4.map(t => Vector(t)).getOrElse(Vector()),
              to5.map(t => Vector(t)).getOrElse(Vector()),
              to6.map(t => Vector(t)).getOrElse(Vector()),
              to7.map(t => Vector(t)).getOrElse(Vector()),
              to8.map(t => Vector(t)).getOrElse(Vector()),
              to9.map(t => Vector(t)).getOrElse(Vector()),
              to10.map(t => Vector(t)).getOrElse(Vector()),
              to11.map(t => Vector(t)).getOrElse(Vector()),
              to12.map(t => Vector(t)).getOrElse(Vector()),
              to13.map(t => Vector(t)).getOrElse(Vector()),
              to14.map(t => Vector(t)).getOrElse(Vector()),
              to15.map(t => Vector(t)).getOrElse(Vector()),
              to16.map(t => Vector(t)).getOrElse(Vector()),
              to17.map(t => Vector(t)).getOrElse(Vector())))
        }
      }

      connection.sendPreparedStatement(statement, _parameters: _*).map { result =>
        result.rows.map { ars =>
          new AsyncResultSetIterator(ars).foldLeft(
            LinkedHashMap[A, (Seq[B1], Seq[B2], Seq[B3], Seq[B4], Seq[B5], Seq[B6], Seq[B7], Seq[B8], Seq[B9], Seq[B10], Seq[B11], Seq[B12], Seq[B13], Seq[B14], Seq[B15], Seq[B16], Seq[B17])]())(processResultSet).map {
              case (one, (t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11, t12, t13, t14, t15, t16, t17)) => transform(one, t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11, t12, t13, t14, t15, t16, t17)
            }
        }.getOrElse(Nil)
      }
    }
  }

  @deprecated("will be removed. use oneToManies18Iterable", "0.12.0")
  def oneToManies18Traversable[A, B1, B2, B3, B4, B5, B6, B7, B8, B9, B10, B11, B12, B13, B14, B15, B16, B17, B18, Z](
    statement: String,
    parameters: Any*)(
    extractOne: WrappedResultSet => A)(
    extractTo1: WrappedResultSet => Option[B1],
    extractTo2: WrappedResultSet => Option[B2],
    extractTo3: WrappedResultSet => Option[B3],
    extractTo4: WrappedResultSet => Option[B4],
    extractTo5: WrappedResultSet => Option[B5],
    extractTo6: WrappedResultSet => Option[B6],
    extractTo7: WrappedResultSet => Option[B7],
    extractTo8: WrappedResultSet => Option[B8],
    extractTo9: WrappedResultSet => Option[B9],
    extractTo10: WrappedResultSet => Option[B10],
    extractTo11: WrappedResultSet => Option[B11],
    extractTo12: WrappedResultSet => Option[B12],
    extractTo13: WrappedResultSet => Option[B13],
    extractTo14: WrappedResultSet => Option[B14],
    extractTo15: WrappedResultSet => Option[B15],
    extractTo16: WrappedResultSet => Option[B16],
    extractTo17: WrappedResultSet => Option[B17],
    extractTo18: WrappedResultSet => Option[B18])(
    transform: (A, Seq[B1], Seq[B2], Seq[B3], Seq[B4], Seq[B5], Seq[B6], Seq[B7], Seq[B8], Seq[B9], Seq[B10], Seq[B11], Seq[B12], Seq[B13], Seq[B14], Seq[B15], Seq[B16], Seq[B17], Seq[B18]) => Z)(
    implicit
    cxt: EC = ECGlobal): Future[Iterable[Z]] =
    oneToManies18Iterable[A, B1, B2, B3, B4, B5, B6, B7, B8, B9, B10, B11, B12, B13, B14, B15, B16, B17, B18, Z](statement, parameters: _*)(extractOne)(
      extractTo1,
      extractTo2,
      extractTo3,
      extractTo4,
      extractTo5,
      extractTo6,
      extractTo7,
      extractTo8,
      extractTo9,
      extractTo10,
      extractTo11,
      extractTo12,
      extractTo13,
      extractTo14,
      extractTo15,
      extractTo16,
      extractTo17,
      extractTo18)(transform)
  def oneToManies18Iterable[A, B1, B2, B3, B4, B5, B6, B7, B8, B9, B10, B11, B12, B13, B14, B15, B16, B17, B18, Z](
    statement: String,
    parameters: Any*)(
    extractOne: WrappedResultSet => A)(
    extractTo1: WrappedResultSet => Option[B1],
    extractTo2: WrappedResultSet => Option[B2],
    extractTo3: WrappedResultSet => Option[B3],
    extractTo4: WrappedResultSet => Option[B4],
    extractTo5: WrappedResultSet => Option[B5],
    extractTo6: WrappedResultSet => Option[B6],
    extractTo7: WrappedResultSet => Option[B7],
    extractTo8: WrappedResultSet => Option[B8],
    extractTo9: WrappedResultSet => Option[B9],
    extractTo10: WrappedResultSet => Option[B10],
    extractTo11: WrappedResultSet => Option[B11],
    extractTo12: WrappedResultSet => Option[B12],
    extractTo13: WrappedResultSet => Option[B13],
    extractTo14: WrappedResultSet => Option[B14],
    extractTo15: WrappedResultSet => Option[B15],
    extractTo16: WrappedResultSet => Option[B16],
    extractTo17: WrappedResultSet => Option[B17],
    extractTo18: WrappedResultSet => Option[B18])(
    transform: (A, Seq[B1], Seq[B2], Seq[B3], Seq[B4], Seq[B5], Seq[B6], Seq[B7], Seq[B8], Seq[B9], Seq[B10], Seq[B11], Seq[B12], Seq[B13], Seq[B14], Seq[B15], Seq[B16], Seq[B17], Seq[B18]) => Z)(
    implicit
    cxt: EC = ECGlobal): Future[Iterable[Z]] = {
    val _parameters = ensureAndNormalizeParameters(parameters)
    withListeners(statement, _parameters) {
      queryLogging(statement, _parameters)

      def processResultSet(
        result: LinkedHashMap[A, (Seq[B1], Seq[B2], Seq[B3], Seq[B4], Seq[B5], Seq[B6], Seq[B7], Seq[B8], Seq[B9], Seq[B10], Seq[B11], Seq[B12], Seq[B13], Seq[B14], Seq[B15], Seq[B16], Seq[B17], Seq[B18])],
        rs: WrappedResultSet): LinkedHashMap[A, (Seq[B1], Seq[B2], Seq[B3], Seq[B4], Seq[B5], Seq[B6], Seq[B7], Seq[B8], Seq[B9], Seq[B10], Seq[B11], Seq[B12], Seq[B13], Seq[B14], Seq[B15], Seq[B16], Seq[B17], Seq[B18])] = {
        val o = extractOne(rs)
        val (to1, to2, to3, to4, to5, to6, to7, to8, to9, to10, to11, to12, to13, to14, to15, to16, to17, to18) = (
          extractTo1(rs),
          extractTo2(rs),
          extractTo3(rs),
          extractTo4(rs),
          extractTo5(rs),
          extractTo6(rs),
          extractTo7(rs),
          extractTo8(rs),
          extractTo9(rs),
          extractTo10(rs),
          extractTo11(rs),
          extractTo12(rs),
          extractTo13(rs),
          extractTo14(rs),
          extractTo15(rs),
          extractTo16(rs),
          extractTo17(rs),
          extractTo18(rs))
        result.keys.find(_ == o).map { _ =>
          to1.orElse(to2).orElse(to3).orElse(to4).orElse(to5).orElse(to6).orElse(to7).orElse(to8).orElse(to9).orElse(to10).orElse(to11).orElse(to12).orElse(to13).orElse(to14).orElse(to15).orElse(to16).orElse(to17).orElse(to18).map { _ =>
            val (ts1, ts2, ts3, ts4, ts5, ts6, ts7, ts8, ts9, ts10, ts11, ts12, ts13, ts14, ts15, ts16, ts17, ts18) = result.apply(o)
            result += (o -> (
              to1.map(t => if (ts1.contains(t)) ts1 else ts1 :+ t).getOrElse(ts1),
              to2.map(t => if (ts2.contains(t)) ts2 else ts2 :+ t).getOrElse(ts2),
              to3.map(t => if (ts3.contains(t)) ts3 else ts3 :+ t).getOrElse(ts3),
              to4.map(t => if (ts4.contains(t)) ts4 else ts4 :+ t).getOrElse(ts4),
              to5.map(t => if (ts5.contains(t)) ts5 else ts5 :+ t).getOrElse(ts5),
              to6.map(t => if (ts6.contains(t)) ts6 else ts6 :+ t).getOrElse(ts6),
              to7.map(t => if (ts7.contains(t)) ts7 else ts7 :+ t).getOrElse(ts7),
              to8.map(t => if (ts8.contains(t)) ts8 else ts8 :+ t).getOrElse(ts8),
              to9.map(t => if (ts9.contains(t)) ts9 else ts9 :+ t).getOrElse(ts9),
              to10.map(t => if (ts10.contains(t)) ts10 else ts10 :+ t).getOrElse(ts10),
              to11.map(t => if (ts11.contains(t)) ts11 else ts11 :+ t).getOrElse(ts11),
              to12.map(t => if (ts12.contains(t)) ts12 else ts12 :+ t).getOrElse(ts12),
              to13.map(t => if (ts13.contains(t)) ts13 else ts13 :+ t).getOrElse(ts13),
              to14.map(t => if (ts14.contains(t)) ts14 else ts14 :+ t).getOrElse(ts14),
              to15.map(t => if (ts15.contains(t)) ts15 else ts15 :+ t).getOrElse(ts15),
              to16.map(t => if (ts16.contains(t)) ts16 else ts16 :+ t).getOrElse(ts16),
              to17.map(t => if (ts17.contains(t)) ts17 else ts17 :+ t).getOrElse(ts17),
              to18.map(t => if (ts18.contains(t)) ts18 else ts18 :+ t).getOrElse(ts18)))
          }.getOrElse(result)
        }.getOrElse {
          result += (
            o -> (
              to1.map(t => Vector(t)).getOrElse(Vector()),
              to2.map(t => Vector(t)).getOrElse(Vector()),
              to3.map(t => Vector(t)).getOrElse(Vector()),
              to4.map(t => Vector(t)).getOrElse(Vector()),
              to5.map(t => Vector(t)).getOrElse(Vector()),
              to6.map(t => Vector(t)).getOrElse(Vector()),
              to7.map(t => Vector(t)).getOrElse(Vector()),
              to8.map(t => Vector(t)).getOrElse(Vector()),
              to9.map(t => Vector(t)).getOrElse(Vector()),
              to10.map(t => Vector(t)).getOrElse(Vector()),
              to11.map(t => Vector(t)).getOrElse(Vector()),
              to12.map(t => Vector(t)).getOrElse(Vector()),
              to13.map(t => Vector(t)).getOrElse(Vector()),
              to14.map(t => Vector(t)).getOrElse(Vector()),
              to15.map(t => Vector(t)).getOrElse(Vector()),
              to16.map(t => Vector(t)).getOrElse(Vector()),
              to17.map(t => Vector(t)).getOrElse(Vector()),
              to18.map(t => Vector(t)).getOrElse(Vector())))
        }
      }

      connection.sendPreparedStatement(statement, _parameters: _*).map { result =>
        result.rows.map { ars =>
          new AsyncResultSetIterator(ars).foldLeft(
            LinkedHashMap[A, (Seq[B1], Seq[B2], Seq[B3], Seq[B4], Seq[B5], Seq[B6], Seq[B7], Seq[B8], Seq[B9], Seq[B10], Seq[B11], Seq[B12], Seq[B13], Seq[B14], Seq[B15], Seq[B16], Seq[B17], Seq[B18])]())(processResultSet).map {
              case (one, (t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11, t12, t13, t14, t15, t16, t17, t18)) => transform(one, t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11, t12, t13, t14, t15, t16, t17, t18)
            }
        }.getOrElse(Nil)
      }
    }
  }

  @deprecated("will be removed. use oneToManies19Iterable", "0.12.0")
  def oneToManies19Traversable[A, B1, B2, B3, B4, B5, B6, B7, B8, B9, B10, B11, B12, B13, B14, B15, B16, B17, B18, B19, Z](
    statement: String,
    parameters: Any*)(
    extractOne: WrappedResultSet => A)(
    extractTo1: WrappedResultSet => Option[B1],
    extractTo2: WrappedResultSet => Option[B2],
    extractTo3: WrappedResultSet => Option[B3],
    extractTo4: WrappedResultSet => Option[B4],
    extractTo5: WrappedResultSet => Option[B5],
    extractTo6: WrappedResultSet => Option[B6],
    extractTo7: WrappedResultSet => Option[B7],
    extractTo8: WrappedResultSet => Option[B8],
    extractTo9: WrappedResultSet => Option[B9],
    extractTo10: WrappedResultSet => Option[B10],
    extractTo11: WrappedResultSet => Option[B11],
    extractTo12: WrappedResultSet => Option[B12],
    extractTo13: WrappedResultSet => Option[B13],
    extractTo14: WrappedResultSet => Option[B14],
    extractTo15: WrappedResultSet => Option[B15],
    extractTo16: WrappedResultSet => Option[B16],
    extractTo17: WrappedResultSet => Option[B17],
    extractTo18: WrappedResultSet => Option[B18],
    extractTo19: WrappedResultSet => Option[B19])(
    transform: (A, Seq[B1], Seq[B2], Seq[B3], Seq[B4], Seq[B5], Seq[B6], Seq[B7], Seq[B8], Seq[B9], Seq[B10], Seq[B11], Seq[B12], Seq[B13], Seq[B14], Seq[B15], Seq[B16], Seq[B17], Seq[B18], Seq[B19]) => Z)(
    implicit
    cxt: EC = ECGlobal): Future[Iterable[Z]] =
    oneToManies19Iterable[A, B1, B2, B3, B4, B5, B6, B7, B8, B9, B10, B11, B12, B13, B14, B15, B16, B17, B18, B19, Z](statement, parameters: _*)(extractOne)(
      extractTo1,
      extractTo2,
      extractTo3,
      extractTo4,
      extractTo5,
      extractTo6,
      extractTo7,
      extractTo8,
      extractTo9,
      extractTo10,
      extractTo11,
      extractTo12,
      extractTo13,
      extractTo14,
      extractTo15,
      extractTo16,
      extractTo17,
      extractTo18,
      extractTo19)(transform)
  def oneToManies19Iterable[A, B1, B2, B3, B4, B5, B6, B7, B8, B9, B10, B11, B12, B13, B14, B15, B16, B17, B18, B19, Z](
    statement: String,
    parameters: Any*)(
    extractOne: WrappedResultSet => A)(
    extractTo1: WrappedResultSet => Option[B1],
    extractTo2: WrappedResultSet => Option[B2],
    extractTo3: WrappedResultSet => Option[B3],
    extractTo4: WrappedResultSet => Option[B4],
    extractTo5: WrappedResultSet => Option[B5],
    extractTo6: WrappedResultSet => Option[B6],
    extractTo7: WrappedResultSet => Option[B7],
    extractTo8: WrappedResultSet => Option[B8],
    extractTo9: WrappedResultSet => Option[B9],
    extractTo10: WrappedResultSet => Option[B10],
    extractTo11: WrappedResultSet => Option[B11],
    extractTo12: WrappedResultSet => Option[B12],
    extractTo13: WrappedResultSet => Option[B13],
    extractTo14: WrappedResultSet => Option[B14],
    extractTo15: WrappedResultSet => Option[B15],
    extractTo16: WrappedResultSet => Option[B16],
    extractTo17: WrappedResultSet => Option[B17],
    extractTo18: WrappedResultSet => Option[B18],
    extractTo19: WrappedResultSet => Option[B19])(
    transform: (A, Seq[B1], Seq[B2], Seq[B3], Seq[B4], Seq[B5], Seq[B6], Seq[B7], Seq[B8], Seq[B9], Seq[B10], Seq[B11], Seq[B12], Seq[B13], Seq[B14], Seq[B15], Seq[B16], Seq[B17], Seq[B18], Seq[B19]) => Z)(
    implicit
    cxt: EC = ECGlobal): Future[Iterable[Z]] = {
    val _parameters = ensureAndNormalizeParameters(parameters)
    withListeners(statement, _parameters) {
      queryLogging(statement, _parameters)

      def processResultSet(
        result: LinkedHashMap[A, (Seq[B1], Seq[B2], Seq[B3], Seq[B4], Seq[B5], Seq[B6], Seq[B7], Seq[B8], Seq[B9], Seq[B10], Seq[B11], Seq[B12], Seq[B13], Seq[B14], Seq[B15], Seq[B16], Seq[B17], Seq[B18], Seq[B19])],
        rs: WrappedResultSet): LinkedHashMap[A, (Seq[B1], Seq[B2], Seq[B3], Seq[B4], Seq[B5], Seq[B6], Seq[B7], Seq[B8], Seq[B9], Seq[B10], Seq[B11], Seq[B12], Seq[B13], Seq[B14], Seq[B15], Seq[B16], Seq[B17], Seq[B18], Seq[B19])] = {
        val o = extractOne(rs)
        val (to1, to2, to3, to4, to5, to6, to7, to8, to9, to10, to11, to12, to13, to14, to15, to16, to17, to18, to19) = (
          extractTo1(rs),
          extractTo2(rs),
          extractTo3(rs),
          extractTo4(rs),
          extractTo5(rs),
          extractTo6(rs),
          extractTo7(rs),
          extractTo8(rs),
          extractTo9(rs),
          extractTo10(rs),
          extractTo11(rs),
          extractTo12(rs),
          extractTo13(rs),
          extractTo14(rs),
          extractTo15(rs),
          extractTo16(rs),
          extractTo17(rs),
          extractTo18(rs),
          extractTo19(rs))
        result.keys.find(_ == o).map { _ =>
          to1.orElse(to2).orElse(to3).orElse(to4).orElse(to5).orElse(to6).orElse(to7).orElse(to8).orElse(to9).orElse(to10).orElse(to11).orElse(to12).orElse(to13).orElse(to14).orElse(to15).orElse(to16).orElse(to17).orElse(to18).orElse(to19).map { _ =>
            val (ts1, ts2, ts3, ts4, ts5, ts6, ts7, ts8, ts9, ts10, ts11, ts12, ts13, ts14, ts15, ts16, ts17, ts18, ts19) = result.apply(o)
            result += (o -> (
              to1.map(t => if (ts1.contains(t)) ts1 else ts1 :+ t).getOrElse(ts1),
              to2.map(t => if (ts2.contains(t)) ts2 else ts2 :+ t).getOrElse(ts2),
              to3.map(t => if (ts3.contains(t)) ts3 else ts3 :+ t).getOrElse(ts3),
              to4.map(t => if (ts4.contains(t)) ts4 else ts4 :+ t).getOrElse(ts4),
              to5.map(t => if (ts5.contains(t)) ts5 else ts5 :+ t).getOrElse(ts5),
              to6.map(t => if (ts6.contains(t)) ts6 else ts6 :+ t).getOrElse(ts6),
              to7.map(t => if (ts7.contains(t)) ts7 else ts7 :+ t).getOrElse(ts7),
              to8.map(t => if (ts8.contains(t)) ts8 else ts8 :+ t).getOrElse(ts8),
              to9.map(t => if (ts9.contains(t)) ts9 else ts9 :+ t).getOrElse(ts9),
              to10.map(t => if (ts10.contains(t)) ts10 else ts10 :+ t).getOrElse(ts10),
              to11.map(t => if (ts11.contains(t)) ts11 else ts11 :+ t).getOrElse(ts11),
              to12.map(t => if (ts12.contains(t)) ts12 else ts12 :+ t).getOrElse(ts12),
              to13.map(t => if (ts13.contains(t)) ts13 else ts13 :+ t).getOrElse(ts13),
              to14.map(t => if (ts14.contains(t)) ts14 else ts14 :+ t).getOrElse(ts14),
              to15.map(t => if (ts15.contains(t)) ts15 else ts15 :+ t).getOrElse(ts15),
              to16.map(t => if (ts16.contains(t)) ts16 else ts16 :+ t).getOrElse(ts16),
              to17.map(t => if (ts17.contains(t)) ts17 else ts17 :+ t).getOrElse(ts17),
              to18.map(t => if (ts18.contains(t)) ts18 else ts18 :+ t).getOrElse(ts18),
              to19.map(t => if (ts19.contains(t)) ts19 else ts19 :+ t).getOrElse(ts19)))
          }.getOrElse(result)
        }.getOrElse {
          result += (
            o -> (
              to1.map(t => Vector(t)).getOrElse(Vector()),
              to2.map(t => Vector(t)).getOrElse(Vector()),
              to3.map(t => Vector(t)).getOrElse(Vector()),
              to4.map(t => Vector(t)).getOrElse(Vector()),
              to5.map(t => Vector(t)).getOrElse(Vector()),
              to6.map(t => Vector(t)).getOrElse(Vector()),
              to7.map(t => Vector(t)).getOrElse(Vector()),
              to8.map(t => Vector(t)).getOrElse(Vector()),
              to9.map(t => Vector(t)).getOrElse(Vector()),
              to10.map(t => Vector(t)).getOrElse(Vector()),
              to11.map(t => Vector(t)).getOrElse(Vector()),
              to12.map(t => Vector(t)).getOrElse(Vector()),
              to13.map(t => Vector(t)).getOrElse(Vector()),
              to14.map(t => Vector(t)).getOrElse(Vector()),
              to15.map(t => Vector(t)).getOrElse(Vector()),
              to16.map(t => Vector(t)).getOrElse(Vector()),
              to17.map(t => Vector(t)).getOrElse(Vector()),
              to18.map(t => Vector(t)).getOrElse(Vector()),
              to19.map(t => Vector(t)).getOrElse(Vector())))
        }
      }

      connection.sendPreparedStatement(statement, _parameters: _*).map { result =>
        result.rows.map { ars =>
          new AsyncResultSetIterator(ars).foldLeft(
            LinkedHashMap[A, (Seq[B1], Seq[B2], Seq[B3], Seq[B4], Seq[B5], Seq[B6], Seq[B7], Seq[B8], Seq[B9], Seq[B10], Seq[B11], Seq[B12], Seq[B13], Seq[B14], Seq[B15], Seq[B16], Seq[B17], Seq[B18], Seq[B19])]())(processResultSet).map {
              case (one, (t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11, t12, t13, t14, t15, t16, t17, t18, t19)) => transform(one, t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11, t12, t13, t14, t15, t16, t17, t18, t19)
            }
        }.getOrElse(Nil)
      }
    }
  }

  @deprecated("will be removed. use oneToManies20Iterable", "0.12.0")
  def oneToManies20Traversable[A, B1, B2, B3, B4, B5, B6, B7, B8, B9, B10, B11, B12, B13, B14, B15, B16, B17, B18, B19, B20, Z](
    statement: String,
    parameters: Any*)(
    extractOne: WrappedResultSet => A)(
    extractTo1: WrappedResultSet => Option[B1],
    extractTo2: WrappedResultSet => Option[B2],
    extractTo3: WrappedResultSet => Option[B3],
    extractTo4: WrappedResultSet => Option[B4],
    extractTo5: WrappedResultSet => Option[B5],
    extractTo6: WrappedResultSet => Option[B6],
    extractTo7: WrappedResultSet => Option[B7],
    extractTo8: WrappedResultSet => Option[B8],
    extractTo9: WrappedResultSet => Option[B9],
    extractTo10: WrappedResultSet => Option[B10],
    extractTo11: WrappedResultSet => Option[B11],
    extractTo12: WrappedResultSet => Option[B12],
    extractTo13: WrappedResultSet => Option[B13],
    extractTo14: WrappedResultSet => Option[B14],
    extractTo15: WrappedResultSet => Option[B15],
    extractTo16: WrappedResultSet => Option[B16],
    extractTo17: WrappedResultSet => Option[B17],
    extractTo18: WrappedResultSet => Option[B18],
    extractTo19: WrappedResultSet => Option[B19],
    extractTo20: WrappedResultSet => Option[B20])(
    transform: (A, Seq[B1], Seq[B2], Seq[B3], Seq[B4], Seq[B5], Seq[B6], Seq[B7], Seq[B8], Seq[B9], Seq[B10], Seq[B11], Seq[B12], Seq[B13], Seq[B14], Seq[B15], Seq[B16], Seq[B17], Seq[B18], Seq[B19], Seq[B20]) => Z)(
    implicit
    cxt: EC = ECGlobal): Future[Iterable[Z]] =
    oneToManies20Iterable[A, B1, B2, B3, B4, B5, B6, B7, B8, B9, B10, B11, B12, B13, B14, B15, B16, B17, B18, B19, B20, Z](statement, parameters: _*)(extractOne)(
      extractTo1,
      extractTo2,
      extractTo3,
      extractTo4,
      extractTo5,
      extractTo6,
      extractTo7,
      extractTo8,
      extractTo9,
      extractTo10,
      extractTo11,
      extractTo12,
      extractTo13,
      extractTo14,
      extractTo15,
      extractTo16,
      extractTo17,
      extractTo18,
      extractTo19,
      extractTo20)(transform)
  def oneToManies20Iterable[A, B1, B2, B3, B4, B5, B6, B7, B8, B9, B10, B11, B12, B13, B14, B15, B16, B17, B18, B19, B20, Z](
    statement: String,
    parameters: Any*)(
    extractOne: WrappedResultSet => A)(
    extractTo1: WrappedResultSet => Option[B1],
    extractTo2: WrappedResultSet => Option[B2],
    extractTo3: WrappedResultSet => Option[B3],
    extractTo4: WrappedResultSet => Option[B4],
    extractTo5: WrappedResultSet => Option[B5],
    extractTo6: WrappedResultSet => Option[B6],
    extractTo7: WrappedResultSet => Option[B7],
    extractTo8: WrappedResultSet => Option[B8],
    extractTo9: WrappedResultSet => Option[B9],
    extractTo10: WrappedResultSet => Option[B10],
    extractTo11: WrappedResultSet => Option[B11],
    extractTo12: WrappedResultSet => Option[B12],
    extractTo13: WrappedResultSet => Option[B13],
    extractTo14: WrappedResultSet => Option[B14],
    extractTo15: WrappedResultSet => Option[B15],
    extractTo16: WrappedResultSet => Option[B16],
    extractTo17: WrappedResultSet => Option[B17],
    extractTo18: WrappedResultSet => Option[B18],
    extractTo19: WrappedResultSet => Option[B19],
    extractTo20: WrappedResultSet => Option[B20])(
    transform: (A, Seq[B1], Seq[B2], Seq[B3], Seq[B4], Seq[B5], Seq[B6], Seq[B7], Seq[B8], Seq[B9], Seq[B10], Seq[B11], Seq[B12], Seq[B13], Seq[B14], Seq[B15], Seq[B16], Seq[B17], Seq[B18], Seq[B19], Seq[B20]) => Z)(
    implicit
    cxt: EC = ECGlobal): Future[Iterable[Z]] = {
    val _parameters = ensureAndNormalizeParameters(parameters)
    withListeners(statement, _parameters) {
      queryLogging(statement, _parameters)

      def processResultSet(
        result: LinkedHashMap[A, (Seq[B1], Seq[B2], Seq[B3], Seq[B4], Seq[B5], Seq[B6], Seq[B7], Seq[B8], Seq[B9], Seq[B10], Seq[B11], Seq[B12], Seq[B13], Seq[B14], Seq[B15], Seq[B16], Seq[B17], Seq[B18], Seq[B19], Seq[B20])],
        rs: WrappedResultSet): LinkedHashMap[A, (Seq[B1], Seq[B2], Seq[B3], Seq[B4], Seq[B5], Seq[B6], Seq[B7], Seq[B8], Seq[B9], Seq[B10], Seq[B11], Seq[B12], Seq[B13], Seq[B14], Seq[B15], Seq[B16], Seq[B17], Seq[B18], Seq[B19], Seq[B20])] = {
        val o = extractOne(rs)
        val (to1, to2, to3, to4, to5, to6, to7, to8, to9, to10, to11, to12, to13, to14, to15, to16, to17, to18, to19, to20) = (
          extractTo1(rs),
          extractTo2(rs),
          extractTo3(rs),
          extractTo4(rs),
          extractTo5(rs),
          extractTo6(rs),
          extractTo7(rs),
          extractTo8(rs),
          extractTo9(rs),
          extractTo10(rs),
          extractTo11(rs),
          extractTo12(rs),
          extractTo13(rs),
          extractTo14(rs),
          extractTo15(rs),
          extractTo16(rs),
          extractTo17(rs),
          extractTo18(rs),
          extractTo19(rs),
          extractTo20(rs))
        result.keys.find(_ == o).map { _ =>
          to1.orElse(to2).orElse(to3).orElse(to4).orElse(to5).orElse(to6).orElse(to7).orElse(to8).orElse(to9).orElse(to10).orElse(to11).orElse(to12).orElse(to13).orElse(to14).orElse(to15).orElse(to16).orElse(to17).orElse(to18).orElse(to19).orElse(to20).map { _ =>
            val (ts1, ts2, ts3, ts4, ts5, ts6, ts7, ts8, ts9, ts10, ts11, ts12, ts13, ts14, ts15, ts16, ts17, ts18, ts19, ts20) = result.apply(o)
            result += (o -> (
              to1.map(t => if (ts1.contains(t)) ts1 else ts1 :+ t).getOrElse(ts1),
              to2.map(t => if (ts2.contains(t)) ts2 else ts2 :+ t).getOrElse(ts2),
              to3.map(t => if (ts3.contains(t)) ts3 else ts3 :+ t).getOrElse(ts3),
              to4.map(t => if (ts4.contains(t)) ts4 else ts4 :+ t).getOrElse(ts4),
              to5.map(t => if (ts5.contains(t)) ts5 else ts5 :+ t).getOrElse(ts5),
              to6.map(t => if (ts6.contains(t)) ts6 else ts6 :+ t).getOrElse(ts6),
              to7.map(t => if (ts7.contains(t)) ts7 else ts7 :+ t).getOrElse(ts7),
              to8.map(t => if (ts8.contains(t)) ts8 else ts8 :+ t).getOrElse(ts8),
              to9.map(t => if (ts9.contains(t)) ts9 else ts9 :+ t).getOrElse(ts9),
              to10.map(t => if (ts10.contains(t)) ts10 else ts10 :+ t).getOrElse(ts10),
              to11.map(t => if (ts11.contains(t)) ts11 else ts11 :+ t).getOrElse(ts11),
              to12.map(t => if (ts12.contains(t)) ts12 else ts12 :+ t).getOrElse(ts12),
              to13.map(t => if (ts13.contains(t)) ts13 else ts13 :+ t).getOrElse(ts13),
              to14.map(t => if (ts14.contains(t)) ts14 else ts14 :+ t).getOrElse(ts14),
              to15.map(t => if (ts15.contains(t)) ts15 else ts15 :+ t).getOrElse(ts15),
              to16.map(t => if (ts16.contains(t)) ts16 else ts16 :+ t).getOrElse(ts16),
              to17.map(t => if (ts17.contains(t)) ts17 else ts17 :+ t).getOrElse(ts17),
              to18.map(t => if (ts18.contains(t)) ts18 else ts18 :+ t).getOrElse(ts18),
              to19.map(t => if (ts19.contains(t)) ts19 else ts19 :+ t).getOrElse(ts19),
              to20.map(t => if (ts20.contains(t)) ts20 else ts20 :+ t).getOrElse(ts20)))
          }.getOrElse(result)
        }.getOrElse {
          result += (
            o -> (
              to1.map(t => Vector(t)).getOrElse(Vector()),
              to2.map(t => Vector(t)).getOrElse(Vector()),
              to3.map(t => Vector(t)).getOrElse(Vector()),
              to4.map(t => Vector(t)).getOrElse(Vector()),
              to5.map(t => Vector(t)).getOrElse(Vector()),
              to6.map(t => Vector(t)).getOrElse(Vector()),
              to7.map(t => Vector(t)).getOrElse(Vector()),
              to8.map(t => Vector(t)).getOrElse(Vector()),
              to9.map(t => Vector(t)).getOrElse(Vector()),
              to10.map(t => Vector(t)).getOrElse(Vector()),
              to11.map(t => Vector(t)).getOrElse(Vector()),
              to12.map(t => Vector(t)).getOrElse(Vector()),
              to13.map(t => Vector(t)).getOrElse(Vector()),
              to14.map(t => Vector(t)).getOrElse(Vector()),
              to15.map(t => Vector(t)).getOrElse(Vector()),
              to16.map(t => Vector(t)).getOrElse(Vector()),
              to17.map(t => Vector(t)).getOrElse(Vector()),
              to18.map(t => Vector(t)).getOrElse(Vector()),
              to19.map(t => Vector(t)).getOrElse(Vector()),
              to20.map(t => Vector(t)).getOrElse(Vector())))
        }
      }

      connection.sendPreparedStatement(statement, _parameters: _*).map { result =>
        result.rows.map { ars =>
          new AsyncResultSetIterator(ars).foldLeft(
            LinkedHashMap[A, (Seq[B1], Seq[B2], Seq[B3], Seq[B4], Seq[B5], Seq[B6], Seq[B7], Seq[B8], Seq[B9], Seq[B10], Seq[B11], Seq[B12], Seq[B13], Seq[B14], Seq[B15], Seq[B16], Seq[B17], Seq[B18], Seq[B19], Seq[B20])]())(processResultSet).map {
              case (one, (t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11, t12, t13, t14, t15, t16, t17, t18, t19, t20)) => transform(one, t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11, t12, t13, t14, t15, t16, t17, t18, t19, t20)
            }
        }.getOrElse(Nil)
      }
    }
  }

  @deprecated("will be removed. use oneToManies21Iterable", "0.12.0")
  def oneToManies21Traversable[A, B1, B2, B3, B4, B5, B6, B7, B8, B9, B10, B11, B12, B13, B14, B15, B16, B17, B18, B19, B20, B21, Z](
    statement: String,
    parameters: Any*)(
    extractOne: WrappedResultSet => A)(
    extractTo1: WrappedResultSet => Option[B1],
    extractTo2: WrappedResultSet => Option[B2],
    extractTo3: WrappedResultSet => Option[B3],
    extractTo4: WrappedResultSet => Option[B4],
    extractTo5: WrappedResultSet => Option[B5],
    extractTo6: WrappedResultSet => Option[B6],
    extractTo7: WrappedResultSet => Option[B7],
    extractTo8: WrappedResultSet => Option[B8],
    extractTo9: WrappedResultSet => Option[B9],
    extractTo10: WrappedResultSet => Option[B10],
    extractTo11: WrappedResultSet => Option[B11],
    extractTo12: WrappedResultSet => Option[B12],
    extractTo13: WrappedResultSet => Option[B13],
    extractTo14: WrappedResultSet => Option[B14],
    extractTo15: WrappedResultSet => Option[B15],
    extractTo16: WrappedResultSet => Option[B16],
    extractTo17: WrappedResultSet => Option[B17],
    extractTo18: WrappedResultSet => Option[B18],
    extractTo19: WrappedResultSet => Option[B19],
    extractTo20: WrappedResultSet => Option[B20],
    extractTo21: WrappedResultSet => Option[B21])(
    transform: (A, Seq[B1], Seq[B2], Seq[B3], Seq[B4], Seq[B5], Seq[B6], Seq[B7], Seq[B8], Seq[B9], Seq[B10], Seq[B11], Seq[B12], Seq[B13], Seq[B14], Seq[B15], Seq[B16], Seq[B17], Seq[B18], Seq[B19], Seq[B20], Seq[B21]) => Z)(
    implicit
    cxt: EC = ECGlobal): Future[Iterable[Z]] =
    oneToManies21Iterable[A, B1, B2, B3, B4, B5, B6, B7, B8, B9, B10, B11, B12, B13, B14, B15, B16, B17, B18, B19, B20, B21, Z](statement, parameters: _*)(extractOne)(
      extractTo1,
      extractTo2,
      extractTo3,
      extractTo4,
      extractTo5,
      extractTo6,
      extractTo7,
      extractTo8,
      extractTo9,
      extractTo10,
      extractTo11,
      extractTo12,
      extractTo13,
      extractTo14,
      extractTo15,
      extractTo16,
      extractTo17,
      extractTo18,
      extractTo19,
      extractTo20,
      extractTo21)(transform)
  def oneToManies21Iterable[A, B1, B2, B3, B4, B5, B6, B7, B8, B9, B10, B11, B12, B13, B14, B15, B16, B17, B18, B19, B20, B21, Z](
    statement: String,
    parameters: Any*)(
    extractOne: WrappedResultSet => A)(
    extractTo1: WrappedResultSet => Option[B1],
    extractTo2: WrappedResultSet => Option[B2],
    extractTo3: WrappedResultSet => Option[B3],
    extractTo4: WrappedResultSet => Option[B4],
    extractTo5: WrappedResultSet => Option[B5],
    extractTo6: WrappedResultSet => Option[B6],
    extractTo7: WrappedResultSet => Option[B7],
    extractTo8: WrappedResultSet => Option[B8],
    extractTo9: WrappedResultSet => Option[B9],
    extractTo10: WrappedResultSet => Option[B10],
    extractTo11: WrappedResultSet => Option[B11],
    extractTo12: WrappedResultSet => Option[B12],
    extractTo13: WrappedResultSet => Option[B13],
    extractTo14: WrappedResultSet => Option[B14],
    extractTo15: WrappedResultSet => Option[B15],
    extractTo16: WrappedResultSet => Option[B16],
    extractTo17: WrappedResultSet => Option[B17],
    extractTo18: WrappedResultSet => Option[B18],
    extractTo19: WrappedResultSet => Option[B19],
    extractTo20: WrappedResultSet => Option[B20],
    extractTo21: WrappedResultSet => Option[B21])(
    transform: (A, Seq[B1], Seq[B2], Seq[B3], Seq[B4], Seq[B5], Seq[B6], Seq[B7], Seq[B8], Seq[B9], Seq[B10], Seq[B11], Seq[B12], Seq[B13], Seq[B14], Seq[B15], Seq[B16], Seq[B17], Seq[B18], Seq[B19], Seq[B20], Seq[B21]) => Z)(
    implicit
    cxt: EC = ECGlobal): Future[Iterable[Z]] = {
    val _parameters = ensureAndNormalizeParameters(parameters)
    withListeners(statement, _parameters) {
      queryLogging(statement, _parameters)

      def processResultSet(
        result: LinkedHashMap[A, (Seq[B1], Seq[B2], Seq[B3], Seq[B4], Seq[B5], Seq[B6], Seq[B7], Seq[B8], Seq[B9], Seq[B10], Seq[B11], Seq[B12], Seq[B13], Seq[B14], Seq[B15], Seq[B16], Seq[B17], Seq[B18], Seq[B19], Seq[B20], Seq[B21])],
        rs: WrappedResultSet): LinkedHashMap[A, (Seq[B1], Seq[B2], Seq[B3], Seq[B4], Seq[B5], Seq[B6], Seq[B7], Seq[B8], Seq[B9], Seq[B10], Seq[B11], Seq[B12], Seq[B13], Seq[B14], Seq[B15], Seq[B16], Seq[B17], Seq[B18], Seq[B19], Seq[B20], Seq[B21])] = {
        val o = extractOne(rs)
        val (to1, to2, to3, to4, to5, to6, to7, to8, to9, to10, to11, to12, to13, to14, to15, to16, to17, to18, to19, to20, to21) = (
          extractTo1(rs),
          extractTo2(rs),
          extractTo3(rs),
          extractTo4(rs),
          extractTo5(rs),
          extractTo6(rs),
          extractTo7(rs),
          extractTo8(rs),
          extractTo9(rs),
          extractTo10(rs),
          extractTo11(rs),
          extractTo12(rs),
          extractTo13(rs),
          extractTo14(rs),
          extractTo15(rs),
          extractTo16(rs),
          extractTo17(rs),
          extractTo18(rs),
          extractTo19(rs),
          extractTo20(rs),
          extractTo21(rs))
        result.keys.find(_ == o).map { _ =>
          to1.orElse(to2).orElse(to3).orElse(to4).orElse(to5).orElse(to6).orElse(to7).orElse(to8).orElse(to9).orElse(to10).orElse(to11).orElse(to12).orElse(to13).orElse(to14).orElse(to15).orElse(to16).orElse(to17).orElse(to18).orElse(to19).orElse(to20).orElse(to21).map { _ =>
            val (ts1, ts2, ts3, ts4, ts5, ts6, ts7, ts8, ts9, ts10, ts11, ts12, ts13, ts14, ts15, ts16, ts17, ts18, ts19, ts20, ts21) = result.apply(o)
            result += (o -> (
              to1.map(t => if (ts1.contains(t)) ts1 else ts1 :+ t).getOrElse(ts1),
              to2.map(t => if (ts2.contains(t)) ts2 else ts2 :+ t).getOrElse(ts2),
              to3.map(t => if (ts3.contains(t)) ts3 else ts3 :+ t).getOrElse(ts3),
              to4.map(t => if (ts4.contains(t)) ts4 else ts4 :+ t).getOrElse(ts4),
              to5.map(t => if (ts5.contains(t)) ts5 else ts5 :+ t).getOrElse(ts5),
              to6.map(t => if (ts6.contains(t)) ts6 else ts6 :+ t).getOrElse(ts6),
              to7.map(t => if (ts7.contains(t)) ts7 else ts7 :+ t).getOrElse(ts7),
              to8.map(t => if (ts8.contains(t)) ts8 else ts8 :+ t).getOrElse(ts8),
              to9.map(t => if (ts9.contains(t)) ts9 else ts9 :+ t).getOrElse(ts9),
              to10.map(t => if (ts10.contains(t)) ts10 else ts10 :+ t).getOrElse(ts10),
              to11.map(t => if (ts11.contains(t)) ts11 else ts11 :+ t).getOrElse(ts11),
              to12.map(t => if (ts12.contains(t)) ts12 else ts12 :+ t).getOrElse(ts12),
              to13.map(t => if (ts13.contains(t)) ts13 else ts13 :+ t).getOrElse(ts13),
              to14.map(t => if (ts14.contains(t)) ts14 else ts14 :+ t).getOrElse(ts14),
              to15.map(t => if (ts15.contains(t)) ts15 else ts15 :+ t).getOrElse(ts15),
              to16.map(t => if (ts16.contains(t)) ts16 else ts16 :+ t).getOrElse(ts16),
              to17.map(t => if (ts17.contains(t)) ts17 else ts17 :+ t).getOrElse(ts17),
              to18.map(t => if (ts18.contains(t)) ts18 else ts18 :+ t).getOrElse(ts18),
              to19.map(t => if (ts19.contains(t)) ts19 else ts19 :+ t).getOrElse(ts19),
              to20.map(t => if (ts20.contains(t)) ts20 else ts20 :+ t).getOrElse(ts20),
              to21.map(t => if (ts21.contains(t)) ts21 else ts21 :+ t).getOrElse(ts21)))
          }.getOrElse(result)
        }.getOrElse {
          result += (
            o -> (
              to1.map(t => Vector(t)).getOrElse(Vector()),
              to2.map(t => Vector(t)).getOrElse(Vector()),
              to3.map(t => Vector(t)).getOrElse(Vector()),
              to4.map(t => Vector(t)).getOrElse(Vector()),
              to5.map(t => Vector(t)).getOrElse(Vector()),
              to6.map(t => Vector(t)).getOrElse(Vector()),
              to7.map(t => Vector(t)).getOrElse(Vector()),
              to8.map(t => Vector(t)).getOrElse(Vector()),
              to9.map(t => Vector(t)).getOrElse(Vector()),
              to10.map(t => Vector(t)).getOrElse(Vector()),
              to11.map(t => Vector(t)).getOrElse(Vector()),
              to12.map(t => Vector(t)).getOrElse(Vector()),
              to13.map(t => Vector(t)).getOrElse(Vector()),
              to14.map(t => Vector(t)).getOrElse(Vector()),
              to15.map(t => Vector(t)).getOrElse(Vector()),
              to16.map(t => Vector(t)).getOrElse(Vector()),
              to17.map(t => Vector(t)).getOrElse(Vector()),
              to18.map(t => Vector(t)).getOrElse(Vector()),
              to19.map(t => Vector(t)).getOrElse(Vector()),
              to20.map(t => Vector(t)).getOrElse(Vector()),
              to21.map(t => Vector(t)).getOrElse(Vector())))
        }
      }

      connection.sendPreparedStatement(statement, _parameters: _*).map { result =>
        result.rows.map { ars =>
          new AsyncResultSetIterator(ars).foldLeft(
            LinkedHashMap[A, (Seq[B1], Seq[B2], Seq[B3], Seq[B4], Seq[B5], Seq[B6], Seq[B7], Seq[B8], Seq[B9], Seq[B10], Seq[B11], Seq[B12], Seq[B13], Seq[B14], Seq[B15], Seq[B16], Seq[B17], Seq[B18], Seq[B19], Seq[B20], Seq[B21])]())(processResultSet).map {
              case (one, (t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11, t12, t13, t14, t15, t16, t17, t18, t19, t20, t21)) => transform(one, t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11, t12, t13, t14, t15, t16, t17, t18, t19, t20, t21)
            }
        }.getOrElse(Nil)
      }
    }
  }

  protected def queryLogging(statement: String, parameters: Seq[Any]): Unit = {
    if (loggingSQLAndTime.enabled) {
      log.withLevel(loggingSQLAndTime.logLevel)(s"[SQL Execution] '${statement}' with (${parameters.mkString(",")})")
    }
  }

  protected def ensureAndNormalizeParameters(parameters: Seq[Any]): Seq[Any] = {
    parameters.map {
      case binder: ParameterBinder =>
        // use a mock preparedstatement to resolve parameters using parameterbinders the same way it's implemented in scalikejdbc
        val ps = new MockPreparedStatement()
        binder(ps, 0)
        ps.value
      case rawValue =>
        rawValue
    }
  }

  protected def withListeners[A](statement: String, parameters: Seq[Any], startMillis: Long = System.currentTimeMillis)(
    f: Future[A])(implicit cxt: EC = EC.global): Future[A] = {
    f.onComplete {
      case Success(_) =>
        val millis = System.currentTimeMillis - startMillis
        GlobalSettings.queryCompletionListener.apply(statement, parameters, millis)
      case Failure(e) =>
        GlobalSettings.queryFailureListener.apply(statement, parameters, e)
    }
    f
  }

}

/**
 * Shared Asynchronous DB session
 */
case class SharedAsyncDBSession(connection: AsyncConnection) extends AsyncDBSession

/**
 * Asynchronous Transactional DB Session
 */
case class TxAsyncDBSession(connection: NonSharedAsyncConnection) extends AsyncDBSession {

  def isActive: Boolean = connection.isActive

  def begin()(implicit ctx: EC = ECGlobal): Future[AsyncQueryResult] = connection.sendQuery("BEGIN")

  def rollback()(implicit ctx: EC = ECGlobal): Future[AsyncQueryResult] = connection.sendQuery("ROLLBACK")

  def commit()(implicit ctx: EC = ECGlobal): Future[AsyncQueryResult] = connection.sendQuery("COMMIT")

  def release(): Unit = connection.release()

}
