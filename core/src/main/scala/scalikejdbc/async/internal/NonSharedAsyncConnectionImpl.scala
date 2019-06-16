package scalikejdbc.async.internal

import com.github.jasync.sql.db.ConcreteConnection
import com.github.jasync.sql.db.pool.ConnectionPool
import scalikejdbc.async.NonSharedAsyncConnection
import scala.concurrent._
import scalikejdbc.async.ShortenedNames._

/**
 * Non-shared Asynchronous Connection
 * @param underlying jasync connection
 * @param pool jasync connection pool
 */
abstract class NonSharedAsyncConnectionImpl(
  val underlying: ConcreteConnection,
  val pool: Option[ConnectionPool[ConcreteConnection]] = None)
  extends AsyncConnectionCommonImpl
  with NonSharedAsyncConnection {

  override def toNonSharedConnection()(implicit cxt: EC = ECGlobal): Future[NonSharedAsyncConnection] =
    Future.successful(this)

  override def release(): Unit = pool.map(_.giveBack(this.underlying))

}
