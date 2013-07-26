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
import java.sql.PreparedStatement

class AsyncSQLExecution(underlying: SQLExecution)
    extends SQLExecution(underlying.statement)(underlying.parameters: _*)((ps: PreparedStatement) => {})((ps: PreparedStatement) => {}) {

  def future()(implicit session: AsyncDBSession,
    cxt: ExecutionContext = ExecutionContext.Implicits.global): Future[Boolean] = {
    session.execute(underlying.statement, underlying.parameters: _*)
  }
}

class AsyncSQLUpdate(underlying: SQLUpdate)
    extends SQLUpdate(underlying.statement)(underlying.parameters: _*)((ps: PreparedStatement) => {})((ps: PreparedStatement) => {}) {

  def future()(implicit session: AsyncDBSession,
    cxt: ExecutionContext = ExecutionContext.Implicits.global): Future[Int] = {
    session.update(underlying.statement, underlying.parameters: _*)
  }
}

class AsyncSQLUpdateAndReturnGeneratedKey(underlying: SQLUpdateWithGeneratedKey)
    extends SQLUpdateWithGeneratedKey(underlying.statement)(underlying.parameters: _*)(1) {

  def future()(implicit session: AsyncDBSession,
    cxt: ExecutionContext = ExecutionContext.Implicits.global): Future[Long] = {
    session.updateAndReturnGeneratedKey(underlying.statement, underlying.parameters: _*)
  }
}

class AsyncSQLToOption[A, E <: WithExtractor](underlying: SQLToOption[A, E])
    extends SQLToOption[A, E](underlying.statement)(underlying.parameters: _*)(underlying.extractor)(SQL.Output.single) {
  import GeneralizedTypeConstraintsForWithExtractor._

  def future()(implicit session: AsyncDBSession,
    hasExtractor: ThisSQL =:= SQLWithExtractor,
    cxt: ExecutionContext = ExecutionContext.Implicits.global): Future[Option[A]] = {
    session.single(underlying.statement, underlying.parameters: _*)(underlying.extractor)
  }
}

class AsyncSQLToTraversable[A, E <: WithExtractor](underlying: SQLToTraversable[A, E])
    extends SQLToTraversable[A, E](underlying.statement)(underlying.parameters: _*)(underlying.extractor)(SQL.Output.traversable) {
  import GeneralizedTypeConstraintsForWithExtractor._

  def future()(implicit session: AsyncDBSession,
    hasExtractor: ThisSQL =:= SQLWithExtractor,
    cxt: ExecutionContext = ExecutionContext.Implicits.global): Future[Traversable[A]] = {
    session.traversable(underlying.statement, underlying.parameters: _*)(underlying.extractor)
  }
}

class AsyncSQLToList[A, E <: WithExtractor](underlying: SQLToList[A, E])
    extends SQLToTraversable[A, E](underlying.statement)(underlying.parameters: _*)(underlying.extractor)(SQL.Output.list) {
  import GeneralizedTypeConstraintsForWithExtractor._

  def future()(implicit session: AsyncDBSession,
    hasExtractor: ThisSQL =:= SQLWithExtractor,
    cxt: ExecutionContext = ExecutionContext.Implicits.global): Future[List[A]] = {
    session.list(underlying.statement, underlying.parameters: _*)(underlying.extractor)
  }
}

