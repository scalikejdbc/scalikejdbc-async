package scalikejdbc.async.internal

import com.github.mauricio.async.db.{ Connection => MauricioConnection }
import com.github.mauricio.async.db.pool.{ ConnectionPool => MauricioConnectionPool }
import scalikejdbc.async.NonSharedAsyncConnection

/**
 * Non-shared Asynchronous Connection
 * @param underlying mauricio connection
 * @param pool mauricio connection
 */
case class MauricioNonSharedAsyncConnection(underlying: MauricioConnection, pool: Option[MauricioConnectionPool[MauricioConnection]] = None)
    extends MauricioConnectionBaseImpl
    with NonSharedAsyncConnection {

  override def release(): Unit = pool.map(_.giveBack(this.underlying))

}
