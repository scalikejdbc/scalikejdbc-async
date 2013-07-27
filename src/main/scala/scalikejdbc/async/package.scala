package scalikejdbc

import scalikejdbc._, SQLInterpolation._
import scala.concurrent.{ ExecutionContext, Future }

/**
 * Default package to import
 *
 * {{{
 *   import scalikejdbc._, async._, SQLInterpolation._
 * }}}
 */
package object async {

  // ---------------------
  // implicit conversions
  // ---------------------

  implicit def makeSQLExecutionAsync(sql: SQLExecution): AsyncSQLExecution = {
    new AsyncSQLExecution(sql)
  }

  implicit def makeSQLUpdateAsync(sql: SQLUpdate): AsyncSQLUpdate = {
    new AsyncSQLUpdate(sql)
  }

  implicit def makeSQLUpdateAndReturnGeneratedKeyAsync(sql: SQLUpdateWithGeneratedKey): AsyncSQLUpdateAndReturnGeneratedKey = {
    new AsyncSQLUpdateAndReturnGeneratedKey(sql)
  }

  implicit def makeSQLToOptionAsync[A, B1, B2, B3, B4, B5, Z](sql: SQLToOption[A, HasExtractor]): AsyncSQLToOption[_] = {
    sql match {
      case s: OneToOneSQLToOption[A, B1, HasExtractor, Z] => new AsyncOneToOneSQLToOption[A, B1, Z](s)
      case s: OneToManySQLToOption[A, B1, HasExtractor, Z] => new AsyncOneToManySQLToOption[A, B1, Z](s)
      case s: OneToManies2SQLToOption[A, B1, B2, HasExtractor, Z] => new AsyncOneToManies2SQLToOption[A, B1, B2, Z](s)
      case s: OneToManies3SQLToOption[A, B1, B2, B3, HasExtractor, Z] => new AsyncOneToManies3SQLToOption[A, B1, B2, B3, Z](s)
      case s: OneToManies4SQLToOption[A, B1, B2, B3, B4, HasExtractor, Z] => new AsyncOneToManies4SQLToOption[A, B1, B2, B3, B4, Z](s)
      case s: OneToManies5SQLToOption[A, B1, B2, B3, B4, B5, HasExtractor, Z] => new AsyncOneToManies5SQLToOption[A, B1, B2, B3, B4, B5, Z](s)
      case _ => new AsyncSQLToOption[A](sql)
    }
  }

  implicit def makeSQLToTraversableAsync[A, B1, B2, B3, B4, B5, Z](sql: SQLToTraversable[A, HasExtractor]): AsyncSQLToTraversable[_] = {
    sql match {
      case s: OneToOneSQLToTraversable[A, B1, HasExtractor, Z] => new AsyncOneToOneSQLToTraversable[A, B1, Z](s)
      case s: OneToManySQLToTraversable[A, B1, HasExtractor, Z] => new AsyncOneToManySQLToTraversable[A, B1, Z](s)
      case s: OneToManies2SQLToTraversable[A, B1, B2, HasExtractor, Z] => new AsyncOneToManies2SQLToTraversable[A, B1, B2, Z](s)
      case s: OneToManies3SQLToTraversable[A, B1, B2, B3, HasExtractor, Z] => new AsyncOneToManies3SQLToTraversable[A, B1, B2, B3, Z](s)
      case s: OneToManies4SQLToTraversable[A, B1, B2, B3, B4, HasExtractor, Z] => new AsyncOneToManies4SQLToTraversable[A, B1, B2, B3, B4, Z](s)
      case s: OneToManies5SQLToTraversable[A, B1, B2, B3, B4, B5, HasExtractor, Z] => new AsyncOneToManies5SQLToTraversable[A, B1, B2, B3, B4, B5, Z](s)
      case _ => new AsyncSQLToTraversable[A](sql)
    }
  }

  implicit def makeSQLToListAsync[A, B1, B2, B3, B4, B5, Z](sql: SQLToList[A, HasExtractor]): AsyncSQLToList[_] = {
    sql match {
      case s: OneToOneSQLToList[A, B1, HasExtractor, Z] => new AsyncOneToOneSQLToList[A, B1, Z](s)
      case s: OneToManySQLToList[A, B1, HasExtractor, Z] => new AsyncOneToManySQLToList[A, B1, Z](s)
      case s: OneToManies2SQLToList[A, B1, B2, HasExtractor, Z] => new AsyncOneToManies2SQLToList[A, B1, B2, Z](s)
      case s: OneToManies3SQLToList[A, B1, B2, B3, HasExtractor, Z] => new AsyncOneToManies3SQLToList[A, B1, B2, B3, Z](s)
      case s: OneToManies4SQLToList[A, B1, B2, B3, B4, HasExtractor, Z] => new AsyncOneToManies4SQLToList[A, B1, B2, B3, B4, Z](s)
      case s: OneToManies5SQLToList[A, B1, B2, B3, B4, B5, HasExtractor, Z] => new AsyncOneToManies5SQLToList[A, B1, B2, B3, B4, B5, Z](s)
      case _ => new AsyncSQLToList[A](sql)
    }
  }

  // ---------------------
  // utilities
  // ---------------------

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