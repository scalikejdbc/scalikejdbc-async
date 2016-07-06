package unit

import scalikejdbc._

object PersonDBInitializer {

  val mysqlCreateTable = """
create table person (
  id bigint primary key,
  name varchar(64) not null
);
"""

  val insertQueries = Seq(
    """insert into person (id, name) 
        values (12, 'testperson')"""
  )

  def initMySQL(): Unit = {
    NamedDB('mysql) autoCommit { implicit s =>
      try sql"drop table person".execute.apply()
      catch { case e: Exception => }

      SQL(mysqlCreateTable).execute.apply()
      insertQueries.foreach(q => SQL(q).update.apply())
    }
  }

}

