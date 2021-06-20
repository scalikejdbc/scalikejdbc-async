package unit

import scalikejdbc._

object SampleDBInitializer {

  val pgCreateTable = """
create table async_lover (
  id bigserial primary key,
  name varchar(64) not null,
  rating integer not null,
  is_reactive boolean default true, 
  lunchtime time, 
  nanotime time(6),
  birthday date,
  created_at timestamp with time zone not null,
  deleted_at timestamp with time zone
);
"""

  val mysqlCreateTable = """
create table async_lover (
  id bigint auto_increment primary key,
  name varchar(64) not null,
  rating integer not null,
  is_reactive boolean default true,
  lunchtime time(6),
  nanotime time(6),
  birthday date default '1980-01-02', -- mysql-async 0.2.4 cannot parse nullable date values
  created_at timestamp(6) not null,
  deleted_at timestamp(6) default CURRENT_TIMESTAMP(6)
);
"""

  val insertQueries = Seq(
    """insert into async_lover (name, rating, is_reactive, lunchtime, birthday, created_at) 
        values ('Alice', 4, true, '12:30:00', '1980-01-02', '2013-05-06 01:02:03')""",
    """insert into async_lover (name, rating, is_reactive, lunchtime, birthday, created_at) 
        values ('Bob', 3, false, '13:20:00', '1973-03-12', '2013-05-06 03:04:02')""",
    """insert into async_lover (name, rating, is_reactive, lunchtime, birthday, created_at) 
        values ('Chris', 5, true, '11:45:00', '1984-12-31', '2013-05-06 19:32:54')"""
  )

  def initPostgreSQL(): Unit = {
    DB autoCommit { implicit s =>
      try sql"drop table async_lover".execute.apply()
      catch { case e: Exception => }

      SQL(pgCreateTable).execute.apply()
      insertQueries.foreach(q => SQL(q).update.apply())
    }
  }

  def initMySQL(): Unit = {
    NamedDB("mysql") autoCommit { implicit s =>
      try sql"drop table async_lover".execute.apply()
      catch { case e: Exception => }

      SQL(mysqlCreateTable).execute.apply()
      insertQueries.foreach(q => SQL(q).update.apply())
    }
  }

}
