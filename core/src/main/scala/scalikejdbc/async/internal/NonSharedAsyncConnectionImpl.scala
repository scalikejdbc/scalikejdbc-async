package scalikejdbc.async.internal

import com.github.mauricio.async.db.{ Connection => MauricioConnection }
import com.github.mauricio.async.db.pool.{ ConnectionPool => MauricioConnectionPool }
import scalikejdbc.async.NonSharedAsyncConnection
import scala.concurrent._
import scalikejdbc.async.ShortenedNames._

/**
 * Non-shared Asynchronous Connection
 * @param underlying mauricio connection
 * @param pool mauricio connection
 */
abstract class NonSharedAsyncConnectionImpl(
  val underlying: MauricioConnection,
  val pool: Option[MauricioConnectionPool[MauricioConnection]] = None)
    extends AsyncConnectionCommonImpl
    with NonSharedAsyncConnection {

  override def toNonSharedConnection()(implicit cxt: EC = ECGlobal): Future[NonSharedAsyncConnection] = Future(this)

  override def release(): Unit = pool.map(_.giveBack(this.underlying))

}
