package scalikejdbc.async.internal

import com.github.jasync.sql.db.pool.ConnectionPool
import com.github.jasync.sql.db.{ ConcreteConnection, Connection }
import scalikejdbc.async.NonSharedAsyncConnection
import scalikejdbc.async.ShortenedNames._

import scala.concurrent._

/**
 * Non-shared Asynchronous Connection
 * @param underlying jasync connection
 * @param pool jasync connection pool
 */
abstract class NonSharedAsyncConnectionImpl(
  val underlying: Connection,
  val pool: Option[ConnectionPool[ConcreteConnection]] = None)
  extends AsyncConnectionCommonImpl
  with NonSharedAsyncConnection {

  override def toNonSharedConnection()(implicit cxt: EC = ECGlobal): Future[NonSharedAsyncConnection] =
    Future.successful(this)

  override def release(): Unit = this.underlying match {
    case con: ConcreteConnection =>
      pool.map(_.giveBack(con))
    case _ =>
    // nothing to do for pool
  }

}
