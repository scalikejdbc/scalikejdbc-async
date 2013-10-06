package unit

import scalikejdbc._, SQLInterpolation._
import org.slf4j.LoggerFactory

object PgListDBInitializer {

  val log = LoggerFactory.getLogger(this.getClass)

  def initPostgreSQL() {
    DB autoCommit { implicit s =>
      try sql"drop table programmer".execute.apply()
      catch { case e: Exception => log.debug(e.getMessage, e) }
      try {
        sql"""
create table programmer (
  id bigserial primary key,
  name varchar(255) not null,
  company_id bigint,
  created_timestamp timestamp without time zone not null,
  deleted_timestamp timestamp without time zone
);
""".execute.apply()
      } catch { case e: Exception => log.debug(e.getMessage, e) }

      try sql"drop table company".execute.apply()
      catch { case e: Exception => log.debug(e.getMessage, e) }
      try {
        sql"""
create table company (
  id bigserial primary key,
  name varchar(255) not null,
  url varchar(255),
  created_at timestamp without time zone not null,
  deleted_at timestamp without time zone
);
""".execute.apply()
      } catch { case e: Exception => log.debug(e.getMessage, e) }

      try sql"drop table skill".execute.apply()
      catch { case e: Exception => log.debug(e.getMessage, e) }
      try {
        sql"""
create table skill (
  id bigserial primary key,
  name varchar(255) not null,
  created_at timestamp without time zone not null,
  deleted_at timestamp without time zone
);
""".execute.apply()
      } catch { case e: Exception => log.debug(e.getMessage, e) }

      try sql"drop table programmer_skill".execute.apply()
      catch { case e: Exception => log.debug(e.getMessage, e) }
      try {
        sql"""
create table programmer_skill (
  programmer_id bigint not null,
  skill_id bigint not null,
  primary key(programmer_id, skill_id)
);
""".execute.apply()
      } catch { case e: Exception => log.debug(e.getMessage, e) }

      try {
        sql"""
insert into company (name, url, created_at) values ('Typesafe', 'http://typesafe.com/', current_timestamp);
insert into company (name, url, created_at) values ('Oracle', 'http://www.oracle.com/', current_timestamp);
insert into company (name, url, created_at) values ('Google', 'http://www.google.com/', current_timestamp);
insert into company (name, url, created_at) values ('Microsoft', 'http://www.microsoft.com/', current_timestamp);

insert into skill (name, created_at) values ('Scala', current_timestamp);
insert into skill (name, created_at) values ('Java', current_timestamp);
insert into skill (name, created_at) values ('Ruby', current_timestamp);
insert into skill (name, created_at) values ('MySQL', current_timestamp);
insert into skill (name, created_at) values ('PostgreSQL', current_timestamp);

insert into programmer (name, company_id, created_timestamp) values ('Alice', 1, current_timestamp);
insert into programmer (name, company_id, created_timestamp) values ('Bob', 2, current_timestamp);
insert into programmer (name, company_id, created_timestamp) values ('Chris', 1, current_timestamp);

insert into programmer_skill values (1, 1);
insert into programmer_skill values (1, 2);
insert into programmer_skill values (2, 2);
   """.execute.apply()
      } catch { case e: Exception => log.debug(e.getMessage, e) }
    }
  }

  def initMySQL() {
    NamedDB('mysql) autoCommit { implicit s =>

      try sql"drop table programmer_skill".execute.apply()
      catch { case e: Exception => log.debug(e.getMessage, e) }
      try {
        sql"""
create table programmer (
  id bigint auto_increment primary key,
  name varchar(255) not null,
  company_id bigint,
  created_timestamp timestamp not null,
  deleted_timestamp timestamp 
);
""".execute.apply()
      } catch { case e: Exception => log.debug(e.getMessage, e) }

      try sql"drop table company".execute.apply()
      catch { case e: Exception => log.debug(e.getMessage, e) }
      try {
        sql"""
create table company (
  id bigint auto_increment primary key,
  name varchar(255) not null,
  url varchar(255),
  created_at timestamp not null,
  deleted_at timestamp 
);
""".execute.apply()
      } catch { case e: Exception => log.debug(e.getMessage, e) }

      try sql"drop table skill".execute.apply()
      catch { case e: Exception => log.debug(e.getMessage, e) }
      try {
        sql"""
create table skill (
  id bigint auto_increment primary key,
  name varchar(255) not null,
  created_at timestamp not null,
  deleted_at timestamp 
);
""".execute.apply()
      } catch { case e: Exception => log.debug(e.getMessage, e) }

      try sql"drop table programmer_skill".execute.apply()
      catch { case e: Exception => log.debug(e.getMessage, e) }
      try {
        sql"""
create table programmer_skill (
  programmer_id bigint not null,
  skill_id bigint not null,
  primary key(programmer_id, skill_id)
);
""".execute.apply()
      } catch { case e: Exception => log.debug(e.getMessage, e) }

      try {
        sql"""
insert into company (name, url, created_at) values ('Typesafe', 'http://typesafe.com/', current_timestamp);
insert into company (name, url, created_at) values ('Oracle', 'http://www.oracle.com/', current_timestamp);
insert into company (name, url, created_at) values ('Google', 'http://www.google.com/', current_timestamp);
insert into company (name, url, created_at) values ('Microsoft', 'http://www.microsoft.com/', current_timestamp);

insert into skill (name, created_at) values ('Scala', current_timestamp);
insert into skill (name, created_at) values ('Java', current_timestamp);
insert into skill (name, created_at) values ('Ruby', current_timestamp);
insert into skill (name, created_at) values ('MySQL', current_timestamp);
insert into skill (name, created_at) values ('PostgreSQL', current_timestamp);

insert into programmer (name, company_id, created_timestamp) values ('Alice', 1, current_timestamp);
insert into programmer (name, company_id, created_timestamp) values ('Bob', 2, current_timestamp);
insert into programmer (name, company_id, created_timestamp) values ('Chris', 1, current_timestamp);

insert into programmer_skill values (1, 1);
insert into programmer_skill values (1, 2);
insert into programmer_skill values (2, 2);
   """.execute.apply()
      } catch { case e: Exception => log.debug(e.getMessage, e) }
    }
  }

}
