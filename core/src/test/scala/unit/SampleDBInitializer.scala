package unit

import scalikejdbc._, SQLInterpolation._

object SampleDBInitializer {

  val pgCreateTable = """
create table async_lover (
  id bigserial primary key,
  name varchar(64) not null,
  rating integer not null,
  is_reactive boolean default true, 
  lunchtime time, 
  birthday date,
  created_at timestamp without time zone not null,
  deleted_at timestamp without time zone
);
"""

  val mysqlCreateTable = """
create table async_lover (
  id bigint auto_increment primary key,
  name varchar(64) not null,
  rating integer not null,
  is_reactive boolean default true,
  lunchtime time,
  birthday date default '1980-01-02', -- mysql-async 0.2.4 cannot parse nullable date values
  created_at timestamp not null,
  deleted_at timestamp
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
      try {
        sql"select 1 from async_lover limit 1".map(_.long(1)).single.apply()
      } catch {
        case e: Exception =>
          SQL(pgCreateTable).execute.apply()
          insertQueries.foreach(q => SQL(q).update.apply())
      }
    }
  }

  def initMySQL(): Unit = {
    NamedDB('mysql) autoCommit { implicit s =>
      try {
        sql"select 1 from async_lover limit 1".map(_.long(1)).single.apply()
      } catch {
        case e: Exception =>
          SQL(mysqlCreateTable).execute.apply()
          insertQueries.foreach(q => SQL(q).update.apply())
      }
    }
  }

}

