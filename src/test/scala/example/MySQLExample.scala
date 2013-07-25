package example

import scalikejdbc._, SQLInterpolation._, async._

import org.scalatest._
import org.scalatest.matchers._

import scala.concurrent._
import scala.concurrent.duration.DurationInt

class MySQLExample extends FlatSpec with ShouldMatchers {

  ConnectionPool.add('mysql, "jdbc:mysql://localhost/scalikejdbc", "sa", "sa")
  ExampleDBInitializer.initMySQL()

  AsyncConnectionPool.add('mysql, "jdbc:mysql://localhost/scalikejdbc", "sa", "sa")

  val c = Company.syntax("c")

  it should "retrieve a single value" in {
    val f: Future[Option[Company]] = NamedAsyncDB('mysql).withPool { implicit s =>
      withSQL { select.from(Company as c).where.eq(c.id, 1) }.map(Company(c)).single.future()
    }
    Await.result(f, 5.seconds)

    f.value.get.isSuccess should be(true)
    f.value.get.get.isDefined should be(true)
  }

  it should "retrieve several values as Traversable" in {
    val f: Future[Traversable[Company]] = NamedAsyncDB('mysql).withPool { implicit s =>
      withSQL { select.from(Company as c).limit(100) }.map(Company(c)).traversable.future()
    }
    Await.result(f, 5.seconds)

    f.value.get.isSuccess should be(true)
    f.value.get.get.size should be > 0
  }

  it should "retrieve several values" in {
    val f: Future[List[Company]] = NamedAsyncDB('mysql).withPool { implicit s =>
      withSQL { select.from(Company as c).limit(100) }.map(Company(c)).list.future()
    }
    Await.result(f, 5.seconds)

    f.value.get.isSuccess should be(true)
    f.value.get.get.size should be > 0
  }

  it should "update in a transaction" in {

    val f: Future[Unit] = NamedAsyncDB('mysql).withPool { implicit s =>
      val column = Company.column
      AsyncTx.withBuilders(
        delete.from(Company).where.eq(column.id, 997),
        insert.into(Company).namedValues(
          column.id -> 997,
          column.name -> "Foo",
          column.createdAt -> new java.util.Date())
      ).future()
    }
    Await.result(f, 5.seconds)

    f.value.get.isSuccess should be(true)

    val ff: Future[Option[Company]] = NamedAsyncDB('mysql).withPool { implicit s =>
      withSQL { select.from(Company as c).where.eq(c.id, 997) }.map(Company(c)).single.future()
    }
    Await.result(ff, 5.seconds)

    ff.value.get.isSuccess should be(true)
    ff.value.get.get.isDefined should be(true)

    val f0: Future[Unit] = NamedAsyncDB('mysql).withPool { implicit s =>
      val column = Company.column
      AsyncTx.withBuilders(
        insert.into(Company).namedValues(
          column.id -> 998,
          column.name -> "Foo",
          column.createdAt -> new java.util.Date()),
        delete.from(Company).where.eq(column.id, 998)
      ).future()
    }
    Await.result(f0, 5.seconds)
    f0.value.get.isSuccess should be(true)

    //val f1: Future[Seq[AsyncQueryResult]] = NamedAsyncDB('mysql).withPool { implicit s =>
    val f1: Future[Unit] = NamedAsyncDB('mysql).withPool { implicit s =>
      val column = Company.column
      AsyncTx.withSQLs(
        insert.into(Company).namedValues(
          column.id -> 9999,
          column.name -> "Foo",
          column.createdAt -> new java.util.Date()).toSQL,
        sql"invalid query"
      ).future()
    }
    try {
      Await.result(f1, 5.seconds)
    } catch {
      case e: Exception => e.printStackTrace()
    }

    f1.value.get.isSuccess should be(false)

    val f2: Future[Option[Company]] = NamedAsyncDB('mysql).withPool { implicit s =>
      withSQL { select.from(Company as c).where.eq(c.id, 9999) }.map(Company(c)).single.future()
    }
    Await.result(f2, 5.seconds)

    f2.value.get.isSuccess should be(true)
    f2.value.get.get.isDefined should be(true)
  }

}
