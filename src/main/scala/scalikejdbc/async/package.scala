package scalikejdbc

import scalikejdbc._, SQLInterpolation._
import scala.concurrent.{ ExecutionContext, Future }

package object async {

  implicit def makeSQLExecutionAsync(sql: SQLExecution): AsyncSQLExecution = {
    new AsyncSQLExecution(sql)
  }

  implicit def makeSQLUpdateAsync(sql: SQLUpdate): AsyncSQLUpdate = {
    new AsyncSQLUpdate(sql)
  }

  implicit def makeSQLUpdateAndReturnGeneratedKeyAsync(sql: SQLUpdateWithGeneratedKey): AsyncSQLUpdateAndReturnGeneratedKey = {
    new AsyncSQLUpdateAndReturnGeneratedKey(sql)
  }

  implicit def makeSQLToOptionAsync[A, E <: WithExtractor](sql: SQLToOption[A, E]): AsyncSQLToOption[A, E] = {
    new AsyncSQLToOption[A, E](sql)
  }

  implicit def makeSQLToTraversableAsync[A, E <: WithExtractor](sql: SQLToTraversable[A, E]): AsyncSQLToTraversable[A, E] = {
    new AsyncSQLToTraversable[A, E](sql)
  }

  implicit def makeSQLToList[A, E <: WithExtractor](sql: SQLToList[A, E]): AsyncSQLToList[A, E] = {
    new AsyncSQLToList[A, E](sql)
  }

  object singleFuture {
    def apply[A](sql: => SQLBuilder[A])(extractor: (WrappedResultSet) => A)(
      implicit session: AsyncDBSession, cxt: ExecutionContext = ExecutionContext.Implicits.global): Future[Option[A]] = {
      withSQL(sql).map(extractor).single.future()
    }
  }

  object traversableFuture {
    def apply[A](sql: => SQLBuilder[A])(extractor: (WrappedResultSet) => A)(
      implicit session: AsyncDBSession, cxt: ExecutionContext = ExecutionContext.Implicits.global): Future[Traversable[A]] = {
      withSQL(sql).map(extractor).traversable().future()
    }
  }

  object listFuture {
    def apply[A](sql: => SQLBuilder[A])(extractor: (WrappedResultSet) => A)(
      implicit session: AsyncDBSession, cxt: ExecutionContext = ExecutionContext.Implicits.global): Future[List[A]] = {
      withSQL(sql).map(extractor).list.future()
    }
  }

  object updateFuture {
    def apply(sql: => SQLBuilder[_])(
      implicit session: AsyncDBSession, cxt: ExecutionContext = ExecutionContext.Implicits.global): Future[Int] = {
      withSQL(sql).update.future()
    }
  }

  object executeFuture {
    def apply(sql: => SQLBuilder[_])(
      implicit session: AsyncDBSession, cxt: ExecutionContext = ExecutionContext.Implicits.global): Future[Boolean] = {
      withSQL(sql).execute.future()
    }
  }

}