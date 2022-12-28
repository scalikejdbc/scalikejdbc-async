package scalikejdbc.async.internal.r2dbc

import io.r2dbc.spi.{ Connection, Result, Row }
import reactor.core.publisher.{ Flux, Mono }
import scalikejdbc.async.{
  AsyncConnection,
  AsyncQueryResult,
  AsyncResultSet,
  NonSharedAsyncConnection,
  ShortenedNames
}

import scala.concurrent.Future
import scala.compat.java8.FutureConverters._
import scala.jdk.CollectionConverters._

class R2DBCAsyncConnection(
  private[scalikejdbc] val connection: Mono[Connection]
) extends AsyncConnection {

  /**
   * Returns non-shared connection.
   *
   * @param cxt execution context
   * @return connection
   */
  override def toNonSharedConnection()(implicit
    cxt: ShortenedNames.EC
  ): Future[NonSharedAsyncConnection] =
    Future.successful(new R2DBCNonSharedAsyncConnectionImpl(connection))

  /**
   * Send a query.
   *
   * @param statement statement
   * @param cxt       execution context
   * @return future
   */
  override def sendQuery(
    statement: String
  )(implicit cxt: ShortenedNames.EC): Future[AsyncQueryResult] = {
    connection
      .flatMapMany { x =>
        println(statement)
        x.createStatement(statement).execute()
      }
      .next()
      .flatMap { x =>
        Flux.from(x.map((r, m) => XPTO(r))).collectList()
      }
      .map { r =>
        new AsyncQueryResult(
          None,
          None,
          Some(new R2DBCAsyncResultSetImpl(r.asScala.toIndexedSeq))
        ) {
          override def generatedKey: Future[Option[Long]] =
            Future.successful(None)
        }
      }
      .toFuture
      .toScala
  }

  /**
   * Send a prepared statement.
   *
   * @param statement  statement
   * @param parameters parameters
   * @param cxt        execution context
   * @return future
   */
  override def sendPreparedStatement(statement: String, parameters: Any*)(
    implicit cxt: ShortenedNames.EC
  ): Future[AsyncQueryResult] = {
    connection
      .flatMapMany { x =>
        println(statement)
        val s = if (parameters.size == 1) {
          // horrible hack, because prepared statements with ? are not supported...
          try { x.createStatement(statement).bind(0, parameters.head) }
          catch {
            case _: IndexOutOfBoundsException =>
              x.createStatement(statement.replace("?", "$1"))
                .bind(0, parameters.head)
          }
        } else {
          val s = x.createStatement(statement)
          parameters.zipWithIndex.foreach { case (p, i) => s.bind(i, p) }
          s
        }
        s.execute()
      }
      .next()
      .flatMap { x =>
        Flux.from(x.map((r, m) => XPTO(r))).collectList()
      }
      .map { r =>
        new AsyncQueryResult(
          None,
          None,
          Some(new R2DBCAsyncResultSetImpl(r.asScala.toIndexedSeq))
        ) {
          override def generatedKey: Future[Option[Long]] =
            Future.successful(None)
        }
      }
      .toFuture
      .toScala
  }

  /**
   * Close or release this connection.
   */
  override def close(): Unit = connection.map(_.close()).block()
}
