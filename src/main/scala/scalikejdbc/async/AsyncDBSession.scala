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

/**
 * Async DB session
 */
case class AsyncDBSession(connection: AsyncConnection) extends LogSupport {

  import GlobalSettings.loggingSQLAndTime

  def execute(statement: String, parameters: Any*)(
    implicit cxt: ExecutionContext = ExecutionContext.Implicits.global): Future[Boolean] = {
    queryLogging(statement, parameters)
    connection.sendPreparedStatement(statement, parameters: _*).map { result =>
      result.rowsAffected.map { count => count > 0 }.getOrElse(false)
    }
  }

  def update(statement: String, parameters: Any*)(
    implicit cxt: ExecutionContext = ExecutionContext.Implicits.global): Future[Int] = {
    queryLogging(statement, parameters)
    connection.sendPreparedStatement(statement, parameters: _*).map { result =>
      result.rowsAffected.map(_.toInt).getOrElse(0)
    }
  }

  def updateAndReturnGeneratedKey(statement: String, parameters: Any*)(
    implicit cxt: ExecutionContext = ExecutionContext.Implicits.global): Future[Long] = {
    queryLogging(statement, parameters)
    connection.toNonSharedConnection().flatMap { conn =>
      conn.sendPreparedStatement(statement, parameters: _*).map { result =>
        result.generatedKey.getOrElse {
          throw new IllegalArgumentException(ErrorMessage.FAILED_TO_RETRIEVE_GENERATED_KEY + " SQL: '" + statement + "'")
        }
      }
    }
  }

  def traversable[A](statement: String, parameters: Any*)(extractor: WrappedResultSet => A)(
    implicit cxt: ExecutionContext = ExecutionContext.Implicits.global): Future[Traversable[A]] = {
    queryLogging(statement, parameters)
    connection.sendPreparedStatement(statement, parameters: _*).map { result =>
      result.rows.map { ars =>
        new AsyncResultSetTraversable(ars).map(rs => extractor(rs))
      }.getOrElse(Nil)
    }
  }

  def single[A](statement: String, parameters: Any*)(extractor: WrappedResultSet => A)(
    implicit cxt: ExecutionContext = ExecutionContext.Implicits.global): Future[Option[A]] = {
    traversable(statement, parameters: _*)(extractor).map { results =>
      results match {
        case Nil => None
        case one :: Nil => Option(one)
        case _ => throw new TooManyRowsException(1, results.size)
      }
    }
  }

  def list[A](statement: String, parameters: Any*)(extractor: WrappedResultSet => A)(
    implicit cxt: ExecutionContext = ExecutionContext.Implicits.global): Future[List[A]] = {
    (traversable[A](statement, parameters: _*)(extractor)).map(_.toList)
  }

  private[this] def queryLogging(statement: String, parameters: Seq[Any]): Unit = {
    if (loggingSQLAndTime.enabled) {
      log.withLevel(loggingSQLAndTime.logLevel)(s"[SQL Execution] '${statement}' with (${parameters.mkString(",")})")
    }
  }

}

