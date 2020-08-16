package scalikejdbc.async

import scalikejdbc._
import scala.concurrent._
import scalikejdbc.async.ShortenedNames._

/**
 * Provides power mode by implicit conversions.
 *
 * {{{
 *   import scalikejdbc._, async._, FutureImplicits._
 *   val wc = Worker.column
 *   val future: Future[Unit] = AsyncDB.localTx { implicit tx =>
 *     for {
 *       companyId <- withSQL {
 *           insert.into(Company).values("Typesafe", DateTime.now)
 *         }.updateAndReturnGeneratedKey
 *       _ <- update(Worker).set(wc.companyId -> companyId).where.eq(wc.id, 123)
 *     } yield ()
 *   }
 * }}}
 */
object FutureImplicits {

  implicit def fromSQLToIterableFuture[A](sql: SQL[A, HasExtractor])(
    implicit
    session: AsyncDBSession, cxt: EC = EC.Implicits.global): Future[Iterable[A]] = sql.iterable.future()

  implicit def fromSQLToListFuture[A](sql: SQL[A, HasExtractor])(
    implicit
    session: AsyncDBSession, cxt: EC = EC.Implicits.global): Future[List[A]] = sql.list.future()

  implicit def fromSQLToSingleFuture[A](sql: SQL[A, HasExtractor])(
    implicit
    session: AsyncDBSession, cxt: EC = EC.Implicits.global): Future[Option[A]] = sql.single.future()

  implicit def fromSQLToListToListFuture[A](sql: SQLToList[A, HasExtractor])(
    implicit
    session: AsyncDBSession, cxt: EC = EC.Implicits.global): Future[List[A]] = sql.future()

  implicit def fromConditionSQLBuilderToIntFuture(b: ConditionSQLBuilder[UpdateOperation])(
    implicit
    session: AsyncDBSession, cxt: EC = EC.Implicits.global): Future[Int] = updateFuture(b)

  implicit def fromConditionSQLBuilderToUnitFuture(b: ConditionSQLBuilder[UpdateOperation])(
    implicit
    session: AsyncDBSession, cxt: EC = EC.Implicits.global): Future[Unit] = updateFuture(b).map { _ => }

  implicit def fromInsertSQLBuilderToFuture[A](b: InsertSQLBuilder)(
    implicit
    session: AsyncDBSession, cxt: EC = EC.Implicits.global): Future[Int] = updateFuture(b)

  implicit def fromUpdateSQLBuilderToFuture[A](b: UpdateSQLBuilder)(
    implicit
    session: AsyncDBSession, cxt: EC = EC.Implicits.global): Future[Int] = updateFuture(b)

  implicit def fromDeleteSQLBuilderToFuture[A](b: DeleteSQLBuilder)(
    implicit
    session: AsyncDBSession, cxt: EC = EC.Implicits.global): Future[Int] = updateFuture(b)

  implicit def fromSQLExecutionToExecuteFuture(e: SQLExecution)(
    implicit
    session: AsyncDBSession, cxt: EC = EC.Implicits.global): Future[Boolean] = e.future()

  implicit def fromSQLUpdateWithGeneratedKeyToFuture[A](b: SQLUpdateWithGeneratedKey)(
    implicit
    session: AsyncDBSession, cxt: EC = EC.Implicits.global): Future[Long] = b.future()

  implicit def fromSQLUpdateToFuture(e: SQLUpdate)(
    implicit
    session: AsyncDBSession, cxt: EC = EC.Implicits.global): Future[Int] = e.future()

}
