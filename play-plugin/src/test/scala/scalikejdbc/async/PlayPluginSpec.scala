package scalikejdbc.async

import scalikejdbc._

import org.specs2.mutable.Specification

import play.api.test._
import play.api.test.Helpers._
import scala.concurrent.{ Future, Await }
import scala.concurrent.duration._

object PlayPluginSpec extends Specification {

  override def intToRichLong(v: Int) = super.intToRichLong(v)

  sequential

  def fakeApp = FakeApplication(
    withoutPlugins = Seq("play.api.cache.EhCachePlugin"),
    additionalPlugins = Seq("scalikejdbc.async.PlayPlugin"),
    additionalConfiguration = Map(
      "logger.root" -> "INFO",
      "logger.play" -> "INFO",
      "logger.application" -> "DEBUG",
      "dbplugin" -> "disabled",
      "evolutionplugin" -> "disabled",
      "db.default.url" -> "jdbc:postgresql://localhost:5435/mem:default",
      "db.default.user" -> "sa",
      "db.default.password" -> "sa",
      "db.default.schema" -> "",
      "db.default.poolInitialSize" -> "1",
      "db.default.poolMaxSize" -> "2",
      "db.default.poolValidationQuery" -> "select 1",
      "db.default.poolConnectionTimeoutMillis" -> "2000",
      "db.legacydb.url" -> "jdbc:postgresql://localhost:5435/mem:legacy",
      "db.legacydb.user" -> "l",
      "db.legacydb.password" -> "g",
      "db.legacydb.schema" -> "",
      "db.global.loggingSQLAndTime.enabled" -> "true",
      "db.global.loggingSQLAndTime.logLevel" -> "debug",
      "db.global.loggingSQLAndTime.warningEnabled" -> "true",
      "db.global.loggingSQLAndTime.warningThreasholdMillis" -> "1",
      "db.global.loggingSQLAndTime.warningLogLevel" -> "warn",
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
      "db.default.url" -> "jdbc:postgresql://localhost:5435/mem:default",
      "db.default.user" -> "sa",
      "db.default.password" -> "sa",
      "db.legacydb.url" -> "jdbc:postgresql://localhost:5435/mem:legacy",
      "db.legacydb.user" -> "l",
      "db.legacydb.password" -> "g",
      "scalikejdbc.play.closeAllOnStop.enabled" -> "false"
    )
  )

  def fakeAppWithDBPlugin = FakeApplication(
    withoutPlugins = Seq("play.api.cache.EhCachePlugin"),
    additionalPlugins = Seq("scalikejdbc.async.PlayPlugin"),
    additionalConfiguration = Map(
      "db.default.url" -> "jdbc:postgresql://localhost:5435/mem:default",
      "db.default.user" -> "sa",
      "db.default.password" -> "sa",
      "db.default.schema" -> "",
      "db.legacydb.url" -> "jdbc:postgresql://localhost:5435/mem:legacy",
      "db.legacydb.user" -> "l",
      "db.legacydb.password" -> "g",
      "db.legacydb.schema" -> "",
      "scalikejdbc.global.loggingSQLAndTime.enabled" -> "true",
      "scalikejdbc.global.loggingSQLAndTime.logLevel" -> "debug",
      "scalikejdbc.global.loggingSQLAndTime.warningEnabled" -> "true",
      "scalikejdbc.global.loggingSQLAndTime.warningThreasholdMillis" -> "1",
      "scalikejdbc.global.loggingSQLAndTime.warningLogLevel" -> "warn"
    )
  )

  def plugin = fakeApp.plugin[PlayPlugin].get

  def simpleTest(table: String) = {
    import scala.concurrent.ExecutionContext.Implicits.global

    try {
      val init = Future.sequence(Seq(
        AsyncDB localTx { implicit s =>
          for {
            _ <- SQL("DROP TABLE " + table + " IF EXISTS").execute.future()
            _ <- SQL("CREATE TABLE " + table + " (ID BIGINT PRIMARY KEY NOT NULL, NAME VARCHAR(256))").execute.future()
            insert = SQL("INSERT INTO " + table + " (ID, NAME) VALUES (/*'id*/123, /*'name*/'Alice')")
            _ <- insert.bindByName('id -> 1, 'name -> "Alice").update.future()
            _ <- insert.bindByName('id -> 2, 'name -> "Bob").update.future()
            _ <- insert.bindByName('id -> 3, 'name -> "Eve").update.future()
          } yield ()
        },
        NamedAsyncDB('legacydb) localTx { implicit s =>
          for {
            _ <- SQL("DROP TABLE " + table + " IF EXISTS").execute.future()
            _ <- SQL("CREATE TABLE " + table + " (ID BIGINT PRIMARY KEY NOT NULL, NAME VARCHAR(256))").execute.future()
            insert = SQL("INSERT INTO " + table + " (ID, NAME) VALUES (/*'id*/123, /*'name*/'Alice')")
            _ <- insert.bindByName('id -> 1, 'name -> "Alice").update.future()
            _ <- insert.bindByName('id -> 2, 'name -> "Bob").update.future()
            _ <- insert.bindByName('id -> 3, 'name -> "Eve").update.future()
            _ <- insert.bindByName('id -> 4, 'name -> "Fred").update.future()
          } yield ()
        }
      ))

      case class User(id: Long, name: Option[String])

//      val result = for {
//        _ <- init
//        users :: usersInLegacy :: Nil <- Future.sequence(Seq(
//          AsyncDB localTx { implicit s =>
//            SQL("SELECT * FROM " + table).map(rs => User(rs.long("id"), Option(rs.string("name")))).list.future()
//          },
//          NamedAsyncDB('legacydb) localTx { implicit s =>
//            SQL("SELECT * FROM " + table).map(rs => User(rs.long("id"), Option(rs.string("name")))).list.future()
//          }
//        ))
//      } yield (users, usersInLegacy)
//      val (users, usersInLegacy) = Await.result(result, 5.seconds)
//      users.size must_== (3)
//      usersInLegacy.size must_== (4)

      Await.result(init, 5.seconds)
      10 must_== (10)

    } finally {
      val clean = Future.sequence(Seq(
        AsyncDB localTx { implicit s =>
          SQL("DROP TABLE " + table + " IF EXISTS").execute.future()
        },
        NamedAsyncDB('legacydb) localTx { implicit s =>
          SQL("DROP TABLE " + table + " IF EXISTS").execute.future()
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
        simpleTest("user_1")
      }
      running(fakeApp) { simpleTest("user_2") }
      running(fakeApp) { simpleTest("user_3") }
    }

    "be available when DB plugin is also active" in {
      running(fakeAppWithDBPlugin) { simpleTest("user_withdbplugin") }
    }

    "close connection pools after stopping Play app" in {
      try {
        // Play 2.0.4 throws Exception here
        running(fakeApp) { simpleTest("user_4") }
      } catch { case e: Exception => }
      simpleTest("user_5") must throwA[NullPointerException]
    }

    "skip closing connection pools after stopping Play app" in {
      running(fakeAppWithoutCloseAllOnStop) {
        simpleTest("user_4")
      }
      simpleTest("user_5")
    }
  }

}