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
trait AsyncDBSession extends AsyncDBSessionBoilerplate with LogSupport {

  val connection: AsyncConnection

  def execute(statement: String, parameters: Any*)(implicit
    cxt: EC = ECGlobal
  ): Future[Boolean] = {
    val _parameters = ensureAndNormalizeParameters(parameters)
    withListeners(statement, _parameters) {
      queryLogging(statement, _parameters)
      connection.sendPreparedStatement(statement, _parameters: _*).map {
        result =>
          result.rowsAffected.exists(_ > 0)
      }
    }
  }

  def update(statement: String, parameters: Any*)(implicit
    cxt: EC = ECGlobal
  ): Future[Int] = {
    val _parameters = ensureAndNormalizeParameters(parameters)
    withListeners(statement, _parameters) {
      queryLogging(statement, _parameters)
      val statementResultF =
        if (connection.isShared) {
          // create local transaction if current session is not transactional
          connection.toNonSharedConnection().flatMap { conn =>
            AsyncTx.inTransaction(
              TxAsyncDBSession(conn),
              { (tx: TxAsyncDBSession) =>
                tx.connection.sendPreparedStatement(statement, _parameters: _*)
              }
            )
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

  def updateAndReturnGeneratedKey(statement: String, parameters: Any*)(implicit
    cxt: EC = ECGlobal
  ): Future[Long] = {
    println("updateAndReturnGeneratedKey")
    def readGeneratedKey(result: AsyncQueryResult): Future[Long] = {
      println("readGeneratedKey")
      result.generatedKey.map(_.getOrElse {
        throw new IllegalArgumentException(
          ErrorMessage.FAILED_TO_RETRIEVE_GENERATED_KEY + " SQL: '" + statement + "'"
        )
      })
    }
    val _parameters = ensureAndNormalizeParameters(parameters)
    withListeners(statement, _parameters) {
      queryLogging(statement, _parameters)
      println("queryLogging")
      if (connection.isShared) {
        // create local transaction if current session is not transactional
        connection.toNonSharedConnection().flatMap { conn =>
          AsyncTx.inTransaction(
            TxAsyncDBSession(conn),
            { (tx: TxAsyncDBSession) =>
              tx.connection
                .sendPreparedStatement(statement, _parameters: _*)
                .flatMap(readGeneratedKey)
            }
          )
        }
      } else {
        connection
          .sendPreparedStatement(statement, _parameters: _*)
          .flatMap(readGeneratedKey)
      }
    }
  }

  @deprecated("will be removed. use iterable", "0.12.0")
  def traversable[A](statement: String, parameters: Any*)(
    extractor: WrappedResultSet => A
  )(implicit cxt: EC = ECGlobal): Future[Iterable[A]] =
    iterable[A](statement, parameters: _*)(extractor)

  def iterable[A](statement: String, parameters: Any*)(
    extractor: WrappedResultSet => A
  )(implicit cxt: EC = ECGlobal): Future[Iterable[A]] = {
    val _parameters = ensureAndNormalizeParameters(parameters)
    withListeners(statement, _parameters) {
      queryLogging(statement, _parameters)
      connection.sendPreparedStatement(statement, _parameters: _*).map {
        result =>
          result.rows
            .map { ars =>
              new AsyncResultSetIterator(ars).map(extractor).toList
            }
            .getOrElse(Nil)
      }
    }
  }

  def single[A](statement: String, parameters: Any*)(
    extractor: WrappedResultSet => A
  )(implicit cxt: EC = ECGlobal): Future[Option[A]] = {
    val _parameters = ensureAndNormalizeParameters(parameters)
    iterable(statement, _parameters: _*)(extractor).map {
      case Nil => None
      case one :: Nil => Option(one)
      case results => throw TooManyRowsException(1, results.size)
    }
  }

  def list[A](statement: String, parameters: Any*)(
    extractor: WrappedResultSet => A
  )(implicit cxt: EC = ECGlobal): Future[List[A]] = {
    val _parameters = ensureAndNormalizeParameters(parameters)
    iterable[A](statement, _parameters: _*)(extractor).map(_.toList)
  }

  @deprecated("will be removed. use oneToOneIterable", "0.12.0")
  def oneToOneTraversable[A, B, Z](statement: String, parameters: Any*)(
    extractOne: WrappedResultSet => A
  )(
    extractTo: WrappedResultSet => Option[B]
  )(transform: (A, B) => Z)(implicit cxt: EC = ECGlobal): Future[Iterable[Z]] =
    oneToOneIterable[A, B, Z](statement, parameters: _*)(extractOne)(extractTo)(
      transform
    )

  def oneToOneIterable[A, B, Z](statement: String, parameters: Any*)(
    extractOne: WrappedResultSet => A
  )(extractTo: WrappedResultSet => Option[B])(
    transform: (A, B) => Z
  )(implicit cxt: EC = ECGlobal): Future[Iterable[Z]] = {
    val _parameters = ensureAndNormalizeParameters(parameters)
    withListeners(statement, _parameters) {
      queryLogging(statement, _parameters)

      def processResultSet(
        oneToOne: LinkedHashMap[A, Option[B]],
        rs: WrappedResultSet
      ): LinkedHashMap[A, Option[B]] = {
        val o = extractOne(rs)
        oneToOne.keys.find(_ == o) match {
          case Some(_) =>
            throw IllegalRelationshipException(
              ErrorMessage.INVALID_ONE_TO_ONE_RELATION
            )
          case _ => oneToOne += (o -> extractTo(rs))
        }
      }
      connection.sendPreparedStatement(statement, _parameters: _*).map {
        result =>
          result.rows
            .map { ars =>
              new AsyncResultSetIterator(ars)
                .foldLeft(LinkedHashMap[A, Option[B]]())(processResultSet)
                .map {
                  case (one, Some(to)) => transform(one, to)
                  case (one, None) => one.asInstanceOf[Z]
                }
            }
            .getOrElse(Nil)
      }
    }
  }

  @deprecated("will be removed. use oneToManyIterable", "0.12.0")
  def oneToManyTraversable[A, B, Z](statement: String, parameters: Any*)(
    extractOne: WrappedResultSet => A
  )(extractTo: WrappedResultSet => Option[B])(
    transform: (A, Seq[B]) => Z
  )(implicit cxt: EC = ECGlobal): Future[Iterable[Z]] =
    oneToManyIterable[A, B, Z](statement, parameters: _*)(extractOne)(
      extractTo
    )(transform)

  def oneToManyIterable[A, B, Z](statement: String, parameters: Any*)(
    extractOne: WrappedResultSet => A
  )(extractTo: WrappedResultSet => Option[B])(
    transform: (A, Seq[B]) => Z
  )(implicit cxt: EC = ECGlobal): Future[Iterable[Z]] = {
    val _parameters = ensureAndNormalizeParameters(parameters)
    withListeners(statement, _parameters) {
      queryLogging(statement, _parameters)

      def processResultSet(
        oneToMany: LinkedHashMap[A, Seq[B]],
        rs: WrappedResultSet
      ): LinkedHashMap[A, Seq[B]] = {
        val o = extractOne(rs)
        oneToMany.keys
          .find(_ == o)
          .map { _ =>
            extractTo(rs)
              .map(many => oneToMany += (o -> (oneToMany.apply(o) :+ many)))
              .getOrElse(oneToMany)
          }
          .getOrElse {
            oneToMany += (o -> extractTo(rs)
              .map(many => Vector(many))
              .getOrElse(Nil))
          }
      }
      connection.sendPreparedStatement(statement, _parameters: _*).map {
        result =>
          result.rows
            .map { ars =>
              new AsyncResultSetIterator(ars)
                .foldLeft(LinkedHashMap[A, Seq[B]]())(processResultSet)
                .map { case (one, to) =>
                  transform(one, to)
                }
            }
            .getOrElse(Nil)
      }
    }
  }

  protected def queryLogging(statement: String, parameters: Seq[Any]): Unit = {
    if (loggingSQLAndTime.enabled) {
      log.withLevel(loggingSQLAndTime.logLevel)(
        s"[SQL Execution] '${statement}' with (${parameters.mkString(",")})"
      )
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

  protected def withListeners[A](
    statement: String,
    parameters: Seq[Any],
    startMillis: Long = System.currentTimeMillis
  )(f: Future[A])(implicit cxt: EC = EC.global): Future[A] = {
    f.onComplete {
      case Success(_) =>
        val millis = System.currentTimeMillis - startMillis
        GlobalSettings.queryCompletionListener.apply(
          statement,
          parameters,
          millis
        )
      case Failure(e) =>
        GlobalSettings.queryFailureListener.apply(statement, parameters, e)
    }
    f
  }

}

/**
 * Shared Asynchronous DB session
 */
case class SharedAsyncDBSession(connection: AsyncConnection)
  extends AsyncDBSession

/**
 * Asynchronous Transactional DB Session
 */
case class TxAsyncDBSession(connection: NonSharedAsyncConnection)
  extends AsyncDBSession {

  def isActive: Boolean = connection.isActive

  def begin()(implicit ctx: EC = ECGlobal): Future[AsyncQueryResult] =
    connection.sendQuery("BEGIN")

  def rollback()(implicit ctx: EC = ECGlobal): Future[AsyncQueryResult] =
    connection.sendQuery("ROLLBACK")

  def commit()(implicit ctx: EC = ECGlobal): Future[AsyncQueryResult] =
    connection.sendQuery("COMMIT")

  def release(): Unit = connection.release()

}
