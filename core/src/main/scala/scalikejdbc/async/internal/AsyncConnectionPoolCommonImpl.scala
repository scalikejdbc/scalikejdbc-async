package scalikejdbc.async.internal

import com.github.mauricio.async.db.{ Configuration, Connection }
import com.github.mauricio.async.db.pool.{ ConnectionPool, ObjectFactory, PoolConfiguration }
import scalikejdbc.LogSupport
import scalikejdbc.async.{ AsyncConnectionPool, AsyncConnectionPoolSettings, NonSharedAsyncConnection }

abstract class AsyncConnectionPoolCommonImpl[T <: Connection](
    url: String,
    user: String,
    password: String,
    factoryF: Configuration => ObjectFactory[T],
    settings: AsyncConnectionPoolSettings = AsyncConnectionPoolSettings()
) extends AsyncConnectionPool(settings) with MauricioConfiguration with LogSupport {

  private[this] val factory = factoryF(configuration(url, user, password))
  private[internal] val pool = new ConnectionPool[T](
    factory = factory,
    configuration = PoolConfiguration(
      maxObjects = settings.maxPoolSize,
      maxIdle = settings.maxIdleMillis,
      maxQueueSize = settings.maxQueueSize
    )
  )

  override def close(): Unit = pool.disconnect

  override def giveBack(conn: NonSharedAsyncConnection): Unit = conn match {
    case conn: NonSharedAsyncConnectionImpl => pool.giveBack(conn.underlying.asInstanceOf[T])
    case _ => log.debug("You don't need to give back this connection to the pool.")
  }
}
