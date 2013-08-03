package unit

import scalikejdbc._, async._
import sample._
import programmerlist._

trait DBSettings {
  DBSettings.initialize()
}

object DBSettings {

  private var isInitialized = false

  def initialize(): Unit = this.synchronized {
    if (isInitialized) return
    GlobalSettings.loggingSQLErrors = false

    // default: PostgreSQL
    ConnectionPool.singleton("jdbc:postgresql://localhost:5432/scalikejdbc", "sa", "sa")
    AsyncConnectionPool.singleton("jdbc:postgresql://localhost:5432/scalikejdbc", "sa", "sa")

    SampleDBInitializer.initPostgreSQL()
    PgListDBInitializer.initPostgreSQL()

    // MySQL
    ConnectionPool.add('mysql, "jdbc:mysql://localhost:3306/scalikejdbc", "sa", "sa")
    AsyncConnectionPool.add('mysql, "jdbc:mysql://localhost:3306/scalikejdbc", "sa", "sa")

    SampleDBInitializer.initMySQL()
    PgListDBInitializer.initMySQL()

    isInitialized = true
  }

}
