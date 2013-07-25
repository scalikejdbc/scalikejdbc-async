package scalikejdbc.async

import scalikejdbc._, SQLInterpolation._
import scala.concurrent._

class AsyncTxQuery(sqlObjects: Seq[SQL[_, _]]) {

  def future()(
    implicit session: AsyncDBSession,
    cxt: ExecutionContext = ExecutionContext.Implicits.global): Future[Unit] = {

    session.connection.toNonSharedConnection.map(conn => AsyncTxDBSession(conn)).flatMap { txSession: AsyncTxDBSession =>
      txSession.begin().flatMap { _ =>
        val query: Future[_] = sqlObjects.foldLeft(Future.successful()) { (future: Future[_], sql: SQL[_, _]) =>
          future.flatMap { _ =>
            txSession.connection.sendPreparedStatement(sql.statement, sql.parameters: _*).map { _ => }
          }
        }
        val result = query.flatMap(_ => txSession.commit()).recoverWith {
          case e: Throwable => txSession.rollback().map { _ => throw e }
        }
        result.onComplete(_ => txSession.release())
        result.map { _ => }
      }
    }
  }

}

object AsyncTx {

  def withBuilders(builders: SQLBuilder[_]*)(
    implicit session: AsyncDBSession,
    cxt: ExecutionContext = ExecutionContext.Implicits.global): AsyncTxQuery = withSQLs(builders.map(_.toSQL): _*)

  def withSQLs(sqlObjects: SQL[_, _]*)(
    implicit session: AsyncDBSession,
    cxt: ExecutionContext = ExecutionContext.Implicits.global): AsyncTxQuery = new AsyncTxQuery(sqlObjects)

}

