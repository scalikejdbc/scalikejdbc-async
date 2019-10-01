package unit

import scalikejdbc._
import async.{ AsyncConnectionPoolSettings, _ }
import com.dimafeng.testcontainers.{ ForAllTestContainer, MultipleContainers, MySQLContainer, PostgreSQLContainer }
import org.scalatest.Suite

trait DBSettings extends ForAllTestContainer { self: Suite =>
  protected[this] final val mysql = MySQLContainer(mysqlImageVersion = "mysql:5.7.26")
  protected[this] final val postgres = PostgreSQLContainer("postgres:11.4")
  override val container = MultipleContainers(mysql, postgres)

  protected[this] final val asyncConnectionPoolSettings: AsyncConnectionPoolSettings = AsyncConnectionPoolSettings()

  override def afterStart(): Unit = {
    GlobalSettings.loggingSQLErrors = false

    // default: PostgreSQL
    ConnectionPool.singleton(url = postgres.jdbcUrl, user = postgres.username, password = postgres.password)
    AsyncConnectionPool.singleton(
      url = postgres.jdbcUrl,
      user = postgres.username,
      password = postgres.password,
      settings = asyncConnectionPoolSettings)

    SampleDBInitializer.initPostgreSQL()
    PgListDBInitializer.initPostgreSQL()

    // MySQL
    ConnectionPool.add("mysql", url = mysql.jdbcUrl, user = mysql.username, password = mysql.password)
    AsyncConnectionPool.add(
      name = "mysql",
      url = mysql.jdbcUrl,
      user = mysql.username,
      password = mysql.password,
      settings = asyncConnectionPoolSettings)

    SampleDBInitializer.initMySQL()
    PgListDBInitializer.initMySQL()
    PersonDBInitializer.initMySQL()
    AccountDBInitializer.initMySQL()
  }

}
