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

/**
 * Asynchronous DB Session
 */
trait AsyncDBSession extends LogSupport {

  val connection: AsyncConnection

  def execute(statement: String, parameters: Any*)(implicit cxt: EC = ECGlobal): Future[Boolean] =
    withListeners(statement, parameters) {
      queryLogging(statement, parameters)
      if (connection.isShared) {
        // create local transaction because postgresql-async 0.2.4 seems not to be stable with PostgreSQL without transaction
        connection.toNonSharedConnection().map(c => TxAsyncDBSession(c)).flatMap { tx: TxAsyncDBSession =>
          tx.execute(statement, parameters: _*)
        }
      } else {
        connection.sendPreparedStatement(statement, parameters: _*).map { result =>
          result.rowsAffected.map(_ > 0).getOrElse(false)
        }
      }
    }

  def update(statement: String, parameters: Any*)(implicit cxt: EC = ECGlobal): Future[Int] =
    withListeners(statement, parameters) {
      queryLogging(statement, parameters)
      if (connection.isShared) {
        // create local transaction because postgresql-async 0.2.4 seems not to be stable with PostgreSQL without transaction
        connection.toNonSharedConnection().map(c => TxAsyncDBSession(c)).flatMap { tx: TxAsyncDBSession =>
          tx.update(statement, parameters: _*)
        }
      } else {
        connection.sendPreparedStatement(statement, parameters: _*).map { result =>
          result.rowsAffected.map(_.toInt).getOrElse(0)
        }
      }
    }

  def updateAndReturnGeneratedKey(statement: String, parameters: Any*)(implicit cxt: EC = ECGlobal): Future[Long] =
    withListeners(statement, parameters) {
      queryLogging(statement, parameters)
      connection.toNonSharedConnection().flatMap { conn =>
        conn.sendPreparedStatement(statement, parameters: _*).map { result =>
          result.generatedKey.getOrElse {
            throw new IllegalArgumentException(ErrorMessage.FAILED_TO_RETRIEVE_GENERATED_KEY + " SQL: '" + statement + "'")
          }
        }
      }
    }

  def traversable[A](statement: String, parameters: Any*)(extractor: WrappedResultSet => A)(implicit cxt: EC = ECGlobal): Future[Traversable[A]] =
    withListeners(statement, parameters) {
      queryLogging(statement, parameters)
      connection.sendPreparedStatement(statement, parameters: _*).map { result =>
        result.rows.map { ars =>
          new AsyncResultSetTraversable(ars).map(rs => extractor(rs))
        }.getOrElse(Nil)
      }
    }

  def single[A](statement: String, parameters: Any*)(extractor: WrappedResultSet => A)(
    implicit cxt: EC = ECGlobal): Future[Option[A]] = {
    traversable(statement, parameters: _*)(extractor).map { results =>
      results match {
        case Nil => None
        case one :: Nil => Option(one)
        case _ => throw new TooManyRowsException(1, results.size)
      }
    }
  }

  def list[A](statement: String, parameters: Any*)(extractor: WrappedResultSet => A)(implicit cxt: EC = ECGlobal): Future[List[A]] = {
    (traversable[A](statement, parameters: _*)(extractor)).map(_.toList)
  }

  def oneToOneTraversable[A, B, Z](statement: String, parameters: Any*)(extractOne: (WrappedResultSet) => A)(extractTo: (WrappedResultSet) => Option[B])(transform: (A, B) => Z)(
    implicit cxt: EC = ECGlobal): Future[Traversable[Z]] =
    withListeners(statement, parameters) {
      queryLogging(statement, parameters)

      def processResultSet(oneToOne: (LinkedHashMap[A, Option[B]]), rs: WrappedResultSet): LinkedHashMap[A, Option[B]] = {
        val o = extractOne(rs)
        oneToOne.keys.find(_ == o).map {
          case Some(found) => throw new IllegalRelationshipException(ErrorMessage.INVALID_ONE_TO_ONE_RELATION)
        }.getOrElse {
          oneToOne += (o -> extractTo(rs))
        }
      }
      connection.sendPreparedStatement(statement, parameters: _*).map { result =>
        result.rows.map { ars =>
          new AsyncResultSetTraversable(ars).foldLeft(LinkedHashMap[A, Option[B]]())(processResultSet).map {
            case (one, Some(to)) => transform(one, to)
            case (one, None) => one.asInstanceOf[Z]
          }
        }.getOrElse(Nil)
      }
    }

  def oneToManyTraversable[A, B, Z](statement: String, parameters: Any*)(extractOne: (WrappedResultSet) => A)(extractTo: (WrappedResultSet) => Option[B])(transform: (A, Seq[B]) => Z)(
    implicit cxt: EC = ECGlobal): Future[Traversable[Z]] =
    withListeners(statement, parameters) {
      queryLogging(statement, parameters)

      def processResultSet(oneToMany: (LinkedHashMap[A, Seq[B]]), rs: WrappedResultSet): LinkedHashMap[A, Seq[B]] = {
        val o = extractOne(rs)
        oneToMany.keys.find(_ == o).map { _ =>
          extractTo(rs).map(many => oneToMany += (o -> (oneToMany.apply(o) :+ many))).getOrElse(oneToMany)
        }.getOrElse {
          oneToMany += (o -> extractTo(rs).map(many => Vector(many)).getOrElse(Nil))
        }
      }
      connection.sendPreparedStatement(statement, parameters: _*).map { result =>
        result.rows.map { ars =>
          new AsyncResultSetTraversable(ars).foldLeft(LinkedHashMap[A, Seq[B]]())(processResultSet).map {
            case (one, to) => transform(one, to)
          }
        }.getOrElse(Nil)
      }
    }

  def oneToManies2Traversable[A, B1, B2, Z](statement: String, parameters: Any*)(
    extractOne: (WrappedResultSet) => A)(
      extractTo1: (WrappedResultSet) => Option[B1],
      extractTo2: (WrappedResultSet) => Option[B2])(transform: (A, Seq[B1], Seq[B2]) => Z)(implicit cxt: EC = ECGlobal): Future[Traversable[Z]] =
    withListeners(statement, parameters) {
      queryLogging(statement, parameters)

      def processResultSet(result: (LinkedHashMap[A, (Seq[B1], Seq[B2])]), rs: WrappedResultSet): LinkedHashMap[A, (Seq[B1], Seq[B2])] = {
        val o = extractOne(rs)
        val (to1, to2) = (extractTo1(rs), extractTo2(rs))
        result.keys.find(_ == o).map { _ =>
          to1.orElse(to2).map { _ =>
            val (ts1, ts2) = result.apply(o)
            result += (o -> (
              to1.map(t => if (ts1.contains(t)) ts1 else ts1 :+ t).getOrElse(ts1),
              to2.map(t => if (ts2.contains(t)) ts2 else ts2 :+ t).getOrElse(ts2)
            ))
          }.getOrElse(result)
        }.getOrElse {
          result += (o -> (to1.map(t => Vector(t)).getOrElse(Vector()), to2.map(t => Vector(t)).getOrElse(Vector())))
        }
      }
      connection.sendPreparedStatement(statement, parameters: _*).map { result =>
        result.rows.map { ars =>
          new AsyncResultSetTraversable(ars).foldLeft(LinkedHashMap[A, (Seq[B1], Seq[B2])]())(processResultSet).map {
            case (one, (t1, t2)) => transform(one, t1, t2)
          }
        }.getOrElse(Nil)
      }
    }

  def oneToManies3Traversable[A, B1, B2, B3, Z](statement: String, parameters: Any*)(
    extractOne: (WrappedResultSet) => A)(
      extractTo1: (WrappedResultSet) => Option[B1],
      extractTo2: (WrappedResultSet) => Option[B2],
      extractTo3: (WrappedResultSet) => Option[B3])(transform: (A, Seq[B1], Seq[B2], Seq[B3]) => Z)(implicit cxt: EC = ECGlobal): Future[Traversable[Z]] =
    withListeners(statement, parameters) {
      queryLogging(statement, parameters)

      def processResultSet(result: (LinkedHashMap[A, (Seq[B1], Seq[B2], Seq[B3])]), rs: WrappedResultSet): LinkedHashMap[A, (Seq[B1], Seq[B2], Seq[B3])] = {
        val o = extractOne(rs)
        val (to1, to2, to3) = (extractTo1(rs), extractTo2(rs), extractTo3(rs))
        result.keys.find(_ == o).map { _ =>
          to1.orElse(to2).orElse(to3).map { _ =>
            val (ts1, ts2, ts3) = result.apply(o)
            result += (o -> (
              to1.map(t => if (ts1.contains(t)) ts1 else ts1 :+ t).getOrElse(ts1),
              to2.map(t => if (ts2.contains(t)) ts2 else ts2 :+ t).getOrElse(ts2),
              to3.map(t => if (ts3.contains(t)) ts3 else ts3 :+ t).getOrElse(ts3)
            ))
          }.getOrElse(result)
        }.getOrElse {
          result += (
            o -> (
              to1.map(t => Vector(t)).getOrElse(Vector()),
              to2.map(t => Vector(t)).getOrElse(Vector()),
              to3.map(t => Vector(t)).getOrElse(Vector())
            )
          )
        }
      }
      connection.sendPreparedStatement(statement, parameters: _*).map { result =>
        result.rows.map { ars =>
          new AsyncResultSetTraversable(ars).foldLeft(LinkedHashMap[A, (Seq[B1], Seq[B2], Seq[B3])]())(processResultSet).map {
            case (one, (t1, t2, t3)) => transform(one, t1, t2, t3)
          }
        }.getOrElse(Nil)
      }
    }

  def oneToManies4Traversable[A, B1, B2, B3, B4, Z](statement: String, parameters: Any*)(
    extractOne: (WrappedResultSet) => A)(
      extractTo1: (WrappedResultSet) => Option[B1],
      extractTo2: (WrappedResultSet) => Option[B2],
      extractTo3: (WrappedResultSet) => Option[B3],
      extractTo4: (WrappedResultSet) => Option[B4])(transform: (A, Seq[B1], Seq[B2], Seq[B3], Seq[B4]) => Z)(implicit cxt: EC = ECGlobal): Future[Traversable[Z]] =
    withListeners(statement, parameters) {
      queryLogging(statement, parameters)

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
              to4.map(t => if (ts4.contains(t)) ts4 else ts4 :+ t).getOrElse(ts4)
            ))
          }.getOrElse(result)
        }.getOrElse {
          result += (
            o -> (
              to1.map(t => Vector(t)).getOrElse(Vector()),
              to2.map(t => Vector(t)).getOrElse(Vector()),
              to3.map(t => Vector(t)).getOrElse(Vector()),
              to4.map(t => Vector(t)).getOrElse(Vector())
            )
          )
        }
      }
      connection.sendPreparedStatement(statement, parameters: _*).map { result =>
        result.rows.map { ars =>
          new AsyncResultSetTraversable(ars).foldLeft(LinkedHashMap[A, (Seq[B1], Seq[B2], Seq[B3], Seq[B4])]())(processResultSet).map {
            case (one, (t1, t2, t3, t4)) => transform(one, t1, t2, t3, t4)
          }
        }.getOrElse(Nil)
      }
    }

  def oneToManies5Traversable[A, B1, B2, B3, B4, B5, Z](statement: String, parameters: Any*)(
    extractOne: (WrappedResultSet) => A)(
      extractTo1: (WrappedResultSet) => Option[B1],
      extractTo2: (WrappedResultSet) => Option[B2],
      extractTo3: (WrappedResultSet) => Option[B3],
      extractTo4: (WrappedResultSet) => Option[B4],
      extractTo5: (WrappedResultSet) => Option[B5])(transform: (A, Seq[B1], Seq[B2], Seq[B3], Seq[B4], Seq[B5]) => Z)(implicit cxt: EC = ECGlobal): Future[Traversable[Z]] =
    withListeners(statement, parameters) {
      queryLogging(statement, parameters)

      def processResultSet(result: (LinkedHashMap[A, (Seq[B1], Seq[B2], Seq[B3], Seq[B4], Seq[B5])]), rs: WrappedResultSet): LinkedHashMap[A, (Seq[B1], Seq[B2], Seq[B3], Seq[B4], Seq[B5])] = {
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
              to5.map(t => if (ts5.contains(t)) ts5 else ts5 :+ t).getOrElse(ts5)
            ))
          }.getOrElse(result)
        }.getOrElse {
          result += (
            o -> (
              to1.map(t => Vector(t)).getOrElse(Vector()),
              to2.map(t => Vector(t)).getOrElse(Vector()),
              to3.map(t => Vector(t)).getOrElse(Vector()),
              to4.map(t => Vector(t)).getOrElse(Vector()),
              to5.map(t => Vector(t)).getOrElse(Vector())
            )
          )
        }
      }
      connection.sendPreparedStatement(statement, parameters: _*).map { result =>
        result.rows.map { ars =>
          new AsyncResultSetTraversable(ars).foldLeft(LinkedHashMap[A, (Seq[B1], Seq[B2], Seq[B3], Seq[B4], Seq[B5])]())(processResultSet).map {
            case (one, (t1, t2, t3, t4, t5)) => transform(one, t1, t2, t3, t4, t5)
          }
        }.getOrElse(Nil)
      }
    }

  protected def queryLogging(statement: String, parameters: Seq[Any]): Unit = {
    if (loggingSQLAndTime.enabled) {
      log.withLevel(loggingSQLAndTime.logLevel)(s"[SQL Execution] '${statement}' with (${parameters.mkString(",")})")
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
