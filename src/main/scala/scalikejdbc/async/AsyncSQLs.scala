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

class AsyncSQLExecution(sql: SQLExecution) {
  def future()(implicit session: AsyncDBSession,
    cxt: ExecutionContext = ExecutionContext.Implicits.global): Future[Boolean] = {
    session.execute(sql.statement, sql.parameters: _*)
  }
}

class AsyncSQLUpdate(sql: SQLUpdate) {
  def future()(implicit session: AsyncDBSession,
    cxt: ExecutionContext = ExecutionContext.Implicits.global): Future[Int] = {
    session.update(sql.statement, sql.parameters: _*)
  }
}

class AsyncSQLUpdateAndReturnGeneratedKey(sql: SQLUpdateWithGeneratedKey) {
  def future()(implicit session: AsyncDBSession,
    cxt: ExecutionContext = ExecutionContext.Implicits.global): Future[Long] = {
    session.updateAndReturnGeneratedKey(sql.statement, sql.parameters: _*)
  }
}

class AsyncSQLToOption[A](sql: SQLToOption[A, HasExtractor]) {
  def future[A]()(implicit session: AsyncDBSession,
    cxt: ExecutionContext = ExecutionContext.Implicits.global): Future[Option[A]] = {
    session.single(sql.statement, sql.parameters: _*)(sql.extractor).asInstanceOf[Future[Option[A]]]
  }
}

class AsyncSQLToTraversable[A](sql: SQLToTraversable[A, HasExtractor]) {
  def future[A]()(implicit session: AsyncDBSession,
    cxt: ExecutionContext = ExecutionContext.Implicits.global): Future[Traversable[A]] = {
    session.traversable(sql.statement, sql.parameters: _*)(sql.extractor).asInstanceOf[Future[List[A]]]
  }
}

class AsyncSQLToList[A](sql: SQLToList[A, HasExtractor]) {
  def future[A]()(implicit session: AsyncDBSession,
    cxt: ExecutionContext = ExecutionContext.Implicits.global): Future[List[A]] = {
    session.list(sql.statement, sql.parameters: _*)(sql.extractor).asInstanceOf[Future[List[A]]]
  }
}

