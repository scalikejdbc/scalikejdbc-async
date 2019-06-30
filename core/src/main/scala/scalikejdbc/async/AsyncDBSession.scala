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
import scalikejdbc.async.ShortenedNames._
import java.sql.PreparedStatement
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
          new AsyncResultSetIterator(ars).map(rs => extractor(rs)).toList
        }.getOrElse(Nil)
      }
    }
  }

  def single[A](statement: String, parameters: Any*)(extractor: WrappedResultSet => A)(
    implicit
    cxt: EC = ECGlobal): Future[Option[A]] = {
    val _parameters = ensureAndNormalizeParameters(parameters)
    iterable(statement, _parameters: _*)(extractor).map { results =>
      results match {
        case Nil => None
        case one :: Nil => Option(one)
        case _ => throw TooManyRowsException(1, results.size)
      }
    }
  }

  def list[A](statement: String, parameters: Any*)(extractor: WrappedResultSet => A)(implicit cxt: EC = ECGlobal): Future[List[A]] = {
    val _parameters = ensureAndNormalizeParameters(parameters)
    (iterable[A](statement, _parameters: _*)(extractor)).map(_.toList)
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

      def processResultSet(oneToOne: (LinkedHashMap[A, Option[B]]), rs: WrappedResultSet): LinkedHashMap[A, Option[B]] = {
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

      def processResultSet(oneToMany: (LinkedHashMap[A, Seq[B]]), rs: WrappedResultSet): LinkedHashMap[A, Seq[B]] = {
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

      def processResultSet(result: (LinkedHashMap[A, (Seq[B1], Seq[B2])]), rs: WrappedResultSet): LinkedHashMap[A, (Seq[B1], Seq[B2])] = {
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
          result += (o -> (to1.map(t => Vector(t)).getOrElse(Vector()), to2.map(t => Vector(t)).getOrElse(Vector())))
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

      def processResultSet(result: (LinkedHashMap[A, (Seq[B1], Seq[B2], Seq[B3])]), rs: WrappedResultSet): LinkedHashMap[A, (Seq[B1], Seq[B2], Seq[B3])] = {
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
              to1.map(t => Vector(t)).getOrElse(Vector()),
              to2.map(t => Vector(t)).getOrElse(Vector()),
              to3.map(t => Vector(t)).getOrElse(Vector())))
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

      def processResultSet(result: (LinkedHashMap[A, (Seq[B1], Seq[B2], Seq[B3], Seq[B4])]), rs: WrappedResultSet): LinkedHashMap[A, (Seq[B1], Seq[B2], Seq[B3], Seq[B4])] = {
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
              to1.map(t => Vector(t)).getOrElse(Vector()),
              to2.map(t => Vector(t)).getOrElse(Vector()),
              to3.map(t => Vector(t)).getOrElse(Vector()),
              to4.map(t => Vector(t)).getOrElse(Vector())))
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
        result: (LinkedHashMap[A, (Seq[B1], Seq[B2], Seq[B3], Seq[B4], Seq[B5])]),
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
              to1.map(t => Vector(t)).getOrElse(Vector()),
              to2.map(t => Vector(t)).getOrElse(Vector()),
              to3.map(t => Vector(t)).getOrElse(Vector()),
              to4.map(t => Vector(t)).getOrElse(Vector()),
              to5.map(t => Vector(t)).getOrElse(Vector())))
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
        result: (LinkedHashMap[A, (Seq[B1], Seq[B2], Seq[B3], Seq[B4], Seq[B5], Seq[B6])]),
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
              to1.map(t => Vector(t)).getOrElse(Vector()),
              to2.map(t => Vector(t)).getOrElse(Vector()),
              to3.map(t => Vector(t)).getOrElse(Vector()),
              to4.map(t => Vector(t)).getOrElse(Vector()),
              to5.map(t => Vector(t)).getOrElse(Vector()),
              to6.map(t => Vector(t)).getOrElse(Vector())))
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
        result: (LinkedHashMap[A, (Seq[B1], Seq[B2], Seq[B3], Seq[B4], Seq[B5], Seq[B6], Seq[B7])]),
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
              to1.map(t => Vector(t)).getOrElse(Vector()),
              to2.map(t => Vector(t)).getOrElse(Vector()),
              to3.map(t => Vector(t)).getOrElse(Vector()),
              to4.map(t => Vector(t)).getOrElse(Vector()),
              to5.map(t => Vector(t)).getOrElse(Vector()),
              to6.map(t => Vector(t)).getOrElse(Vector()),
              to7.map(t => Vector(t)).getOrElse(Vector())))
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
        result: (LinkedHashMap[A, (Seq[B1], Seq[B2], Seq[B3], Seq[B4], Seq[B5], Seq[B6], Seq[B7], Seq[B8])]),
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
              to1.map(t => Vector(t)).getOrElse(Vector()),
              to2.map(t => Vector(t)).getOrElse(Vector()),
              to3.map(t => Vector(t)).getOrElse(Vector()),
              to4.map(t => Vector(t)).getOrElse(Vector()),
              to5.map(t => Vector(t)).getOrElse(Vector()),
              to6.map(t => Vector(t)).getOrElse(Vector()),
              to7.map(t => Vector(t)).getOrElse(Vector()),
              to8.map(t => Vector(t)).getOrElse(Vector())))
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
        result: (LinkedHashMap[A, (Seq[B1], Seq[B2], Seq[B3], Seq[B4], Seq[B5], Seq[B6], Seq[B7], Seq[B8], Seq[B9])]),
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
              to1.map(t => Vector(t)).getOrElse(Vector()),
              to2.map(t => Vector(t)).getOrElse(Vector()),
              to3.map(t => Vector(t)).getOrElse(Vector()),
              to4.map(t => Vector(t)).getOrElse(Vector()),
              to5.map(t => Vector(t)).getOrElse(Vector()),
              to6.map(t => Vector(t)).getOrElse(Vector()),
              to7.map(t => Vector(t)).getOrElse(Vector()),
              to8.map(t => Vector(t)).getOrElse(Vector()),
              to9.map(t => Vector(t)).getOrElse(Vector())))
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
    f.onSuccess {
      case _ =>
        val millis = System.currentTimeMillis - startMillis
        GlobalSettings.queryCompletionListener.apply(statement, parameters, millis)
    }
    f.onFailure { case e: Throwable => GlobalSettings.queryFailureListener.apply(statement, parameters, e) }
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
