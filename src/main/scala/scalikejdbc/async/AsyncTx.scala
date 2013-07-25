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
        Future.traverse(sqlObjects) { sql =>
          txSession.connection.sendPreparedStatement(sql.statement, sql.parameters: _*)
        }.andThen {
          case Success(_) => txSession.commit()
          case Failure(e) => txSession.rollback()
        }.andThen {
          case _ => txSession.release()
        }
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

