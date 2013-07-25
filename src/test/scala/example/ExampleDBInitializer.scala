package example

import scalikejdbc._, SQLInterpolation._

object ExampleDBInitializer {

  val pgCreateSequence = "create sequence company_id_seq start with 1"
  val pgCreateTable = """
create table company (
  id bigint not null default nextval('company_id_seq') primary key,
  name varchar(255) not null,
  url varchar(255),
  created_at timestamp not null,
  deleted_at timestamp
);
"""

  val mysqlCreateTable = """
create table company (
  id bigint not null auto_increment primary key,
  name varchar(255) not null,
  url varchar(255),
  created_at timestamp not null,
  deleted_at timestamp
);
"""

  val insert = """
    insert into company (name, url, created_at) values ('Typesafe', 'http://typesafe.com/', current_timestamp);
  """

  def initPostgreSQL(): Unit = {
    DB autoCommit { implicit s =>
      try {
        sql"select 1 from company limit 1".map(_.long(1)).single.apply()
      } catch {
        case e: Exception =>
          SQL(pgCreateSequence).execute.apply()
          SQL(pgCreateTable).execute.apply()
          SQL(insert).update.apply()
      }
    }
  }

  def initMySQL(): Unit = {
    NamedDB('mysql) autoCommit { implicit s =>
      try {
        sql"select 1 from company limit 1".map(_.long(1)).single.apply()
      } catch {
        case e: Exception =>
          SQL(mysqlCreateTable).execute.apply()
          SQL(insert).update.apply()
      }
    }
  }

}

