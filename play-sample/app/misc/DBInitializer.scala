package misc

import scalikejdbc._, SQLInterpolation._

object DBInitializer {

  def run() {
    DB readOnly { implicit s =>
      try {
        sql"select 1 from programmer limit 1".map(_.long(1)).single.apply()
      } catch {
        case e: java.sql.SQLException =>
          DB autoCommit { implicit s =>
            sql"""
create sequence programmer_id_seq start with 1;
create table programmer (
  id bigint not null default nextval('programmer_id_seq') primary key,
  name varchar(255) not null,
  company_id bigint,
  created_timestamp timestamp not null,
  deleted_timestamp timestamp
);

create sequence company_id_seq start with 1;
create table company (
  id bigint not null default nextval('company_id_seq') primary key,
  name varchar(255) not null,
  url varchar(255),
  created_at timestamp not null,
  deleted_at timestamp
);

create sequence skill_id_seq start with 1;
create table skill (
  id bigint not null default nextval('skill_id_seq') primary key,
  name varchar(255) not null,
  created_at timestamp not null,
  deleted_at timestamp
);

create table programmer_skill (
  programmer_id bigint not null,
  skill_id bigint not null,
  primary key(programmer_id, skill_id)
);

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
          }
      }
    }
  }

}

