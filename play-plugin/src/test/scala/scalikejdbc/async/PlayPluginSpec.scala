package scalikejdbc.async

import scalikejdbc._, SQLInterpolation._

import org.specs2.mutable.Specification

import play.api.test._
import play.api.test.Helpers._
import scala.concurrent._
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

object PlayPluginSpec extends Specification {

  // TODO [error] c.g.m.a.d.p.PostgreSQLConnection - Trying to give back a connection that is not ready for query

  override def intToRichLong(v: Int) = super.intToRichLong(v)

  sequential

  def fakeApp = FakeApplication(
    withoutPlugins = Seq("play.api.cache.EhCachePlugin"),
    additionalPlugins = Seq("scalikejdbc.async.PlayPlugin"),
    additionalConfiguration = Map(
      "logger.root" -> "INFO",
      "logger.play" -> "INFO",
      "logger.application" -> "INFO",
      "dbplugin" -> "disabled",
      "evolutionplugin" -> "disabled",
      "db.default.url" -> "jdbc:postgresql://localhost:5432/scalikejdbc",
      "db.default.user" -> "sa",
      "db.default.password" -> "sa",
      "db.default.schema" -> "",
      "db.default.poolInitialSize" -> "1",
      "db.default.poolMaxSize" -> "2",
      "db.default.poolValidationQuery" -> "select 1",
      "db.default.poolConnectionTimeoutMillis" -> "2000",
      "db.legacydb.url" -> "jdbc:postgresql://localhost:5432/scalikejdbc2",
      "db.legacydb.user" -> "sa",
      "db.legacydb.password" -> "sa",
      "db.legacydb.schema" -> "",
      "scalikejdbc.global.loggingSQLAndTime.enabled" -> "true",
      "scalikejdbc.global.loggingSQLAndTime.singleLineMode" -> "true",
      "scalikejdbc.global.loggingSQLAndTime.logLevel" -> "debug",
      "scalikejdbc.global.loggingSQLAndTime.warningEnabled" -> "true",
      "scalikejdbc.global.loggingSQLAndTime.warningThreasholdMillis" -> "1",
      "scalikejdbc.global.loggingSQLAndTime.warningLogLevel" -> "warn"
    )
  )

  def fakeAppWithoutCloseAllOnStop = FakeApplication(
    withoutPlugins = Seq("play.api.cache.EhCachePlugin"),
    additionalPlugins = Seq("scalikejdbc.async.PlayPlugin"),
    additionalConfiguration = Map(
      "db.default.url" -> "jdbc:postgresql://localhost:5432/scalikejdbc",
      "db.default.user" -> "sa",
      "db.default.password" -> "sa",
      "db.legacydb.url" -> "jdbc:postgresql://localhost:5432/scalikejdbc2",
      "db.legacydb.user" -> "sa",
      "db.legacydb.password" -> "sa",
      "scalikejdbc.play.closeAllOnStop.enabled" -> "false"
    )
  )

  def fakeAppWithDBPlugin = FakeApplication(
    withoutPlugins = Seq("play.api.cache.EhCachePlugin"),
    additionalPlugins = Seq("scalikejdbc.async.PlayPlugin"),
    additionalConfiguration = Map(
      "db.default.url" -> "jdbc:postgresql://localhost:5432/scalikejdbc",
      "db.default.user" -> "sa",
      "db.default.password" -> "sa",
      "db.default.schema" -> "",
      "db.legacydb.url" -> "jdbc:postgresql://localhost:5432/scalikejdbc2",
      "db.legacydb.user" -> "sa",
      "db.legacydb.password" -> "sa",
      "db.legacydb.schema" -> "",
      "scalikejdbc.global.loggingSQLAndTime.enabled" -> "true",
      "scalikejdbc.global.loggingSQLAndTime.logLevel" -> "debug",
      "scalikejdbc.global.loggingSQLAndTime.warningEnabled" -> "true",
      "scalikejdbc.global.loggingSQLAndTime.warningThreasholdMillis" -> "1",
      "scalikejdbc.global.loggingSQLAndTime.warningLogLevel" -> "warn"
    )
  )

  def plugin = fakeApp.plugin[PlayPlugin].get

  def simpleTest(table: SQLSyntax) = {

    try {
      val init = Future.sequence(Seq(
        AsyncDB localTx { implicit tx =>
          for {
            _ <- sql"drop table if exists ${table}".execute.future()
            _ <- sql"create table ${table} (id bigint primary key not null, name varchar(255))".execute.future()
            insert = sql"insert into ${table} (id, name) values ({id}, {name})"
            _ <- insert.bindByName('id -> 1, 'name -> "Alice").update.future()
            _ <- insert.bindByName('id -> 2, 'name -> "Bob").update.future()
            _ <- insert.bindByName('id -> 3, 'name -> "Eve").update.future()
          } yield ()
        },
        NamedAsyncDB('legacydb) localTx { implicit s =>
          for {
            _ <- sql"drop table if exists ${table}".execute.future()
            _ <- sql"create table ${table} (id bigint primary key not null, name varchar(255))".execute.future()
            insert = sql"insert into ${table} (id, name) values ({id}, {name})"
            _ <- insert.bindByName('id -> 1, 'name -> "Alice").update.future()
            _ <- insert.bindByName('id -> 2, 'name -> "Bob").update.future()
            _ <- insert.bindByName('id -> 3, 'name -> "Eve").update.future()
            _ <- insert.bindByName('id -> 4, 'name -> "Fred").update.future()
          } yield ()
        }
      ))

      case class User(id: Long, name: Option[String])

      val result = for {
        _ <- init
        users :: usersInLegacy :: Nil <- Future.sequence(Seq(
          AsyncDB localTx { implicit s =>
            sql"select * from ${table}".map(rs => User(rs.long("id"), Option(rs.string("name")))).list.future()
          },
          NamedAsyncDB('legacydb) localTx { implicit s =>
            sql"select * from ${table}".map(rs => User(rs.long("id"), Option(rs.string("name")))).list.future()
          }
        ))
      } yield (users, usersInLegacy)
      val (users, usersInLegacy) = Await.result(result, 5.seconds)
      users.size must_== (3)
      usersInLegacy.size must_== (4)

      Await.result(init, 5.seconds)
      10 must_== (10)

    } finally {
      val clean = Future.sequence(Seq(
        AsyncDB localTx { implicit s =>
          sql"drop table if exists ${table}".execute.future()
        },
        NamedAsyncDB('legacydb) localTx { implicit s =>
          sql"drop table if exists ${table}".execute.future()
        }
      ))
      Await.result(clean, 5.seconds)
    }
  }

  "Play plugin" should {

    "be available when DB plugin is not active" in {
      running(fakeApp) {
        val settings = AsyncConnectionPool.get('default).settings
        settings.initialSize must_== (1)
        settings.maxSize must_== (2)
        settings.validationQuery must_== ("select 1")
        settings.connectionTimeoutMillis must_== (2000)
        simpleTest(sqls"user_1")
      }
      running(fakeApp) { simpleTest(sqls"user_2") }
      running(fakeApp) { simpleTest(sqls"user_3") }
    }

    "be available when DB plugin is also active" in {
      running(fakeAppWithDBPlugin) { simpleTest(sqls"user_withdbplugin") }
    }

    "close connection pools after stopping Play app" in {
      running(fakeApp) {
        simpleTest(sqls"user_4")
      }
      simpleTest(sqls"user_5") must throwA[NullPointerException]
    }

    "skip closing connection pools after stopping Play app" in {
      running(fakeAppWithoutCloseAllOnStop) {
        simpleTest(sqls"user_4")
      }
      simpleTest(sqls"user_5")
    }

  }

}