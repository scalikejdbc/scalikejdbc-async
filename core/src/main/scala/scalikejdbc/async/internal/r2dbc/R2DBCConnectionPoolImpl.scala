package scalikejdbc.async.internal.r2dbc

import com.github.jasync.sql.db.SSLConfiguration
import io.r2dbc.pool.{ ConnectionPool, ConnectionPoolConfiguration }
import io.r2dbc.spi.{
  ConnectionFactories,
  ConnectionFactory,
  ConnectionFactoryOptions
}
import scalikejdbc.async.{
  AsyncConnection,
  AsyncConnectionPool,
  AsyncConnectionPoolSettings,
  NonSharedAsyncConnection
}

import java.time.Duration

class R2DBCConnectionPoolImpl(
  url: String,
  user: String,
  password: String,
  settings: AsyncConnectionPoolSettings
) extends AsyncConnectionPool {

  val pool: ConnectionPool = {
    val connectionFactory = ConnectionFactories.get(
      ConnectionFactoryOptions
        .parse(url)
        .mutate()
        .option(ConnectionFactoryOptions.USER, user)
        .option(ConnectionFactoryOptions.PASSWORD, password)
        .option(
          ConnectionFactoryOptions.SSL,
          java.lang.Boolean.valueOf(
            settings.connectionSettings.ssl
              .fold(false)(_.getMode != SSLConfiguration.Mode.Disable)
          )
        )
        // .option(ConnectionFactoryOptions.CONNECT_TIMEOUT, settings.connectionSettings.connectTimeout)
        // .option(???, settings.connectionSettings.testTimeout)
        // .option(ConnectionFactoryOptions.STATEMENT_TIMEOUT, settings.connectionSettings.queryTimeout)
        .build
    )

    // Create a ConnectionPool for connectionFactory
    val configuration = ConnectionPoolConfiguration
      .builder(connectionFactory)
      .maxIdleTime(Duration.ofMillis(settings.maxIdleMillis))
      .maxSize(settings.maxPoolSize)
      .build

    new ConnectionPool(configuration)
  }

  /**
   * Borrows a connection from pool.
   *
   * @return connection
   */
  override def borrow(): AsyncConnection = {
    println("borrow")
    new R2DBCAsyncConnection(
      pool.create()
    )
  }

  /**
   * Close this pool.
   */
  override def close(): Unit = pool.close()

  /**
   * Gives back the connection.
   *
   * @param conn connection
   */
  override def giveBack(conn: NonSharedAsyncConnection): Unit = pool.create()
}
