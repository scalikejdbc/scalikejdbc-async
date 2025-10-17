package unit

import scalikejdbc._
import scalikejdbc.async._
import org.testcontainers.utility.DockerImageName
import org.scalatest.{ BeforeAndAfterAll, Suite }
import org.testcontainers.mysql.MySQLContainer
import org.testcontainers.postgresql.PostgreSQLContainer

trait DBSettings extends BeforeAndAfterAll { self: Suite =>
  protected[this] def mysqlVersion: String = sys.props("mysql_version")
  protected[this] final val mysql =
    new MySQLContainer(
      DockerImageName.parse(s"mysql:${mysqlVersion}")
    )
  protected[this] final val postgres = new PostgreSQLContainer(
    DockerImageName.parse("postgres:11.4")
  )

  protected[this] final def mysqlJdbcUrl = mysql.getJdbcUrl
  protected[this] final def postgresJdbcUrl = postgres.getJdbcUrl

  protected[this] final val asyncConnectionPoolSettings
    : AsyncConnectionPoolSettings = AsyncConnectionPoolSettings()

  override def beforeAll(): Unit = {
    super.beforeAll()
    mysql.start()
    postgres.start()

    GlobalSettings.loggingSQLErrors = false

    // default: PostgreSQL
    ConnectionPool.singleton(
      url = postgresJdbcUrl,
      user = postgres.getUsername,
      password = postgres.getPassword
    )
    AsyncConnectionPool.singleton(
      url = postgresJdbcUrl,
      user = postgres.getUsername,
      password = postgres.getPassword,
      settings = asyncConnectionPoolSettings
    )

    SampleDBInitializer.initPostgreSQL()
    PgListDBInitializer.initPostgreSQL()

    // MySQL
    ConnectionPool.add(
      "mysql",
      url = mysqlJdbcUrl,
      user = mysql.getUsername,
      password = mysql.getPassword
    )
    AsyncConnectionPool.add(
      name = "mysql",
      url = mysqlJdbcUrl,
      user = mysql.getUsername,
      password = mysql.getPassword,
      settings = asyncConnectionPoolSettings
    )

    SampleDBInitializer.initMySQL()
    PgListDBInitializer.initMySQL()
    PersonDBInitializer.initMySQL()
    AccountDBInitializer.initMySQL()
  }

}
