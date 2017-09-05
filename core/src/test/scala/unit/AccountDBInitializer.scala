package unit

import scalikejdbc._

object AccountDBInitializer {

  val mysqlCreateTable = """
create table account (
  id bigint primary key,
  person_id bigint not null,
  account_details varchar(2000) not null,
  parent bigint
);
"""

  val insertQueries = Seq(
    """insert into account (id, person_id, account_details, parent) 
        values (13, 12, 'test account details', null)"""
  )

  def initMySQL(): Unit = {
    NamedDB('mysql) autoCommit { implicit s =>
      try sql"drop table account".execute.apply()
      catch { case e: Exception => }

      SQL(mysqlCreateTable).execute.apply()
      insertQueries.foreach(q => SQL(q).update.apply())
    }
  }

}

