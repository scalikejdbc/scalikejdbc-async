package scalikejdbc.async.internal

import com.github.jasync.sql.db.{ ConcreteConnection, Configuration, ConnectionPoolConfigurationBuilder }
import com.github.jasync.sql.db.pool.{ ConnectionPool, ObjectFactory }
import scalikejdbc.LogSupport
import scalikejdbc.async.{ AsyncConnectionPool, AsyncConnectionPoolSettings, NonSharedAsyncConnection }

abstract class AsyncConnectionPoolCommonImpl[T <: ConcreteConnection](
  url: String,
  user: String,
  password: String,
  factoryF: Configuration => ObjectFactory[T],
  settings: AsyncConnectionPoolSettings = AsyncConnectionPoolSettings()) extends AsyncConnectionPool(settings) with JasyncConfiguration with LogSupport {

  private[this] val factory = factoryF(configuration(url, user, password, settings.connectionSettings))
  private[internal] val pool = new ConnectionPool[T](
    factory,
    {
      val builder = new ConnectionPoolConfigurationBuilder()
      builder.setMaxActiveConnections(settings.maxPoolSize)
      builder.setMaxIdleTime(settings.maxIdleMillis)
      builder.setMaxPendingQueries(settings.maxQueueSize)
      builder.build()
    })

  override def close(): Unit = pool.disconnect

  override def giveBack(conn: NonSharedAsyncConnection): Unit = conn match {
    case conn: NonSharedAsyncConnectionImpl => pool.giveBack(conn.underlying.asInstanceOf[T])
    case _ => log.debug("You don't need to give back this connection to the pool.")
  }
}
