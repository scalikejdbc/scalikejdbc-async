package scalikejdbc.async

import scalikejdbc._, SQLInterpolation._
import scala.concurrent._

object FutureImplicits {

  implicit def fromConditionSQLBuilderToFuture(b: ConditionSQLBuilder[UpdateOperation])(
    implicit session: AsyncDBSession, cxt: ExecutionContext = ExecutionContext.Implicits.global): Future[Int] = updateFuture(b)

  implicit def fromInsertSQLBuilderToFuture[A](b: InsertSQLBuilder)(
    implicit session: AsyncDBSession, cxt: ExecutionContext = ExecutionContext.Implicits.global): Future[Int] = updateFuture(b)

  implicit def fromUpdateSQLBuilderToFuture[A](b: UpdateSQLBuilder)(
    implicit session: AsyncDBSession, cxt: ExecutionContext = ExecutionContext.Implicits.global): Future[Int] = updateFuture(b)

  implicit def fromDeleteSQLBuilderToFuture[A](b: DeleteSQLBuilder)(
    implicit session: AsyncDBSession, cxt: ExecutionContext = ExecutionContext.Implicits.global): Future[Int] = updateFuture(b)

  implicit def fromSQLExecutionToExecuteFuture(e: SQLExecution)(
    implicit session: AsyncDBSession, cxt: ExecutionContext = ExecutionContext.Implicits.global): Future[Boolean] = e.future()

  implicit def fromSQLUpdateWithGeneratedKeyToFuture[A](b: SQLUpdateWithGeneratedKey)(
    implicit session: AsyncDBSession, cxt: ExecutionContext = ExecutionContext.Implicits.global): Future[Long] = b.future()

  implicit def fromSQLUpdateToFuture(e: SQLUpdate)(
    implicit session: AsyncDBSession, cxt: ExecutionContext = ExecutionContext.Implicits.global): Future[Int] = e.future()

}
