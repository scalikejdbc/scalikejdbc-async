package scalikejdbc.async

import scalikejdbc._, SQLInterpolation._
import scala.concurrent._
import scala.util._

class AsyncTxQuery(sqlObjects: Seq[SQL[_, _]]) {

  def future()(
    implicit session: AsyncDBSession,
    cxt: ExecutionContext = ExecutionContext.Implicits.global): Future[Seq[AsyncQueryResult]] = {

    session.connection.toNonSharedConnection.map(conn => AsyncTxDBSession(conn)).flatMap { txSession: AsyncTxDBSession =>
      txSession.begin().flatMap { _ =>
        val initial = Future.successful[Seq[AsyncQueryResult]](Nil)
        val query: Future[Seq[AsyncQueryResult]] = sqlObjects.foldLeft(initial) { (future, sql) =>
          future.flatMap { results: Seq[AsyncQueryResult] =>
            txSession.connection.sendPreparedStatement(sql.statement, sql.parameters: _*).map {
              result => results :+ result
            }
          }
        }
        val result: Future[Seq[AsyncQueryResult]] = query.flatMap { results: Seq[AsyncQueryResult] =>
          txSession.commit()
          Future.successful(results)
        }.recoverWith {
          case e: Throwable => txSession.rollback().map { _ => throw e }
        }
        result.onComplete(_ => txSession.release())
        result
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

