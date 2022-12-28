package scalikejdbc.async.internal.r2dbc

import io.r2dbc.spi.Connection
import reactor.core.publisher.Mono
import scalikejdbc.async.NonSharedAsyncConnection

class R2DBCNonSharedAsyncConnectionImpl(connection: Mono[Connection])
  extends R2DBCAsyncConnection(connection)
  with NonSharedAsyncConnection {

  /**
   * Gives back this connection to the pool, and the connection will be shared again.
   */
  override def release(): Unit = {
    println("release is not implemented")
  }
}
