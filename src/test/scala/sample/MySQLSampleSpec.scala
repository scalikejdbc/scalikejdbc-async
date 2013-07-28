package sample

import org.joda.time._
import org.scalatest._, matchers._
import scala.concurrent._, duration._, ExecutionContext.Implicits.global
import scalikejdbc._, SQLInterpolation._, async._
import unit._

class MySQLSampleSpec extends FlatSpec with ShouldMatchers with DBSettings with Logging {

  val column = AsyncLover.column
  val createdTime = DateTime.now.withMillisOfSecond(0)
  val al = AsyncLover.syntax("al")

  it should "select a single value" in {
    val f: Future[Option[AsyncLover]] = NamedAsyncDB('mysql).withPool { implicit s =>
      withSQL { select.from(AsyncLover as al).where.eq(al.id, 1) }.map(AsyncLover(al)).single.future()
    }
    Await.result(f, 5.seconds)
    f.value.get.isSuccess should be(true)
    f.value.get.get.isDefined should be(true)
  }

  it should "select values as a Traversable" in {
    val f: Future[Traversable[AsyncLover]] = NamedAsyncDB('mysql).withPool { implicit s =>
      withSQL { select.from(AsyncLover as al).limit(2) }.map(AsyncLover(al)).traversable.future()
    }
    Await.result(f, 5.seconds)
    f.value.get.isSuccess should be(true)
    f.value.get.get.size should equal(2)
  }

  it should "select values as a List" in {
    val f: Future[List[AsyncLover]] = NamedAsyncDB('mysql).withPool { implicit s =>
      withSQL { select.from(AsyncLover as al).limit(2) }.map(AsyncLover(al)).list.future()
    }
    Await.result(f, 5.seconds)
    f.value.get.isSuccess should be(true)
    f.value.get.get.size should equal(2)
  }

  it should "return generated key" in {
    val f: Future[Long] = NamedAsyncDB('mysql).withPool { implicit s =>
      withSQL {
        insert.into(AsyncLover).namedValues(
          column.name -> "Eric",
          column.rating -> 2,
          column.isReactive -> false,
          column.createdAt -> createdTime)
      }.updateAndReturnGeneratedKey.future()
    }
    // the generated key should be found
    Await.result(f, 5.seconds)
    f.value.get.isSuccess should be(true)
    f.foreach { generatedId =>

      // record should be found by the generated key
      val created = NamedDB('mysql).readOnly { implicit s =>
        withSQL { select.from(AsyncLover as al).where.eq(al.id, generatedId) }.map(AsyncLover(al)).single.apply()
      }.get
      created.id should equal(generatedId)
      created.name should equal("Eric")
      created.rating should equal(2)
      created.isReactive should be(false)
      created.createdAt should equal(createdTime)
    }
  }

  it should "update" in {
    // updating queries should be successful
    DB autoCommit { implicit s =>
      withSQL { delete.from(AsyncLover).where.eq(column.id, 1004) }.update.apply()
      withSQL {
        insert.into(AsyncLover).namedValues(
          column.id -> 1004,
          column.name -> "Chris",
          column.rating -> 5,
          column.isReactive -> true,
          column.createdAt -> createdTime
        )
      }.update.apply()
    }
    val deletion: Future[Int] = NamedAsyncDB('mysql).withPool { implicit s =>
      withSQL { delete.from(AsyncLover).where.eq(column.id, 1004) }.update.future()
    }
    Await.result(deletion, 5.seconds)
    deletion.value.get.isSuccess should be(true)
    deletion.foreach { _ =>

      // should be committed
      val deleted = NamedDB('mysql).readOnly { implicit s =>
        withSQL { select.from(AsyncLover as al).where.eq(al.id, 1004) }.map(AsyncLover(al)).single.apply()
      }
      deleted.isDefined should be(false)
    }
  }

  it should "execute" in {
    // execution should be successful
    val id = NamedDB('mysql).autoCommit { implicit s =>
      withSQL {
        insert.into(AsyncLover).namedValues(
          column.name -> "Chris",
          column.rating -> 5,
          column.isReactive -> true,
          column.createdAt -> createdTime
        )
      }.updateAndReturnGeneratedKey.apply()
    }
    val deletion: Future[Boolean] = NamedAsyncDB('mysql).withPool { implicit s =>
      withSQL { delete.from(AsyncLover).where.eq(column.id, id) }.execute.future()
    }
    Await.result(deletion, 5.seconds)
    deletion.value.get.isSuccess should be(true)
    deletion.foreach { _ =>

      // should be committed
      val deleted = NamedDB('mysql).readOnly { implicit s =>
        withSQL { select.from(AsyncLover as al).where.eq(al.id, id) }.map(AsyncLover(al)).single.apply()
      }
      deleted.isDefined should be(false)
    }
  }

  it should "update in a local transaction" in {
    val generatedKey: Future[Long] = NamedAsyncDB('mysql).localTx { implicit tx =>
      withSQL(insert.into(AsyncLover).namedValues(
        column.name -> "Patric",
        column.rating -> 2,
        column.isReactive -> false,
        column.createdAt -> createdTime
      )).updateAndReturnGeneratedKey.future
    }
    Await.result(generatedKey, 5.seconds)
    generatedKey.value.get.isSuccess should be(true)
    generatedKey.foreach { id =>

      val created = NamedDB('mysql).readOnly { implicit s =>
        withSQL { select.from(AsyncLover as al).where.eq(al.id, id) }.map(AsyncLover(al)).single.apply()
      }.get
      created.id should equal(id)
      created.name should equal("Patric")
      created.rating should equal(2)
      created.isReactive should be(false)
      created.createdAt should equal(createdTime)
    }
  }

  it should "rollback in a local transaction" in {
    NamedDB('mysql).autoCommit { implicit s =>
      withSQL { delete.from(AsyncLover).where.eq(column.id, 1003) }.update.apply()
    }
    val f: Future[Unit] = NamedAsyncDB('mysql).localTx { implicit tx =>
      import FutureImplicits._
      for {
        _ <- insert.into(AsyncLover).namedValues(
          column.id -> 1003,
          column.name -> "Patric",
          column.rating -> 2,
          column.isReactive -> false,
          column.createdAt -> createdTime)
        _ <- sql"invalid_query".execute // failure
      } yield ()
    }
    // exception should be thrown
    try {
      Await.result(f, 5.seconds)
      fail("Exception expected")
    } catch {
      case e: Exception => log.debug("expected", e)
    }
    f.value.get.isFailure should be(true)

    // should be rolled back
    val notCreated = NamedDB('mysql).readOnly { implicit s =>
      withSQL { select.from(AsyncLover as al).where.eq(al.id, 1003) }.map(AsyncLover(al)).single.apply()
    }
    notCreated.isDefined should be(false)
  }

  it should "provide transaction by AsyncTx.withSQLBuilders" in {
    val deletionAndCreation: Future[Seq[AsyncQueryResult]] = NamedAsyncDB('mysql).withPool { implicit s =>
      AsyncTx.withSQLBuilders(
        delete.from(AsyncLover).where.eq(column.id, 997),
        insert.into(AsyncLover).namedValues(
          column.id -> 997,
          column.name -> "Eric",
          column.rating -> 2,
          column.isReactive -> false,
          column.createdAt -> createdTime)
      ).future()
    }
    Await.result(deletionAndCreation, 5.seconds)
    deletionAndCreation.value.get.isSuccess should be(true)
    deletionAndCreation.value.get.get.size should be(2)

    // should be found
    val created = NamedDB('mysql).readOnly { implicit s =>
      withSQL { select.from(AsyncLover as al).where.eq(al.id, 997) }.map(AsyncLover(al)).single.apply()
    }.get
    created.id should equal(997)
    created.name should equal("Eric")
    created.rating should equal(2)
    created.isReactive should be(false)
    created.createdAt should equal(createdTime)
  }

  it should "provide transactional deletion by AsyncTx.withSQLBuilders" in {
    val creationAndDeletion: Future[Seq[AsyncQueryResult]] = NamedAsyncDB('mysql).withPool { implicit s =>
      AsyncTx.withSQLBuilders(
        insert.into(AsyncLover).namedValues(
          column.id -> 998,
          column.name -> "Fred",
          column.rating -> 5,
          column.isReactive -> true,
          column.createdAt -> new java.util.Date),
        delete.from(AsyncLover).where.eq(column.id, 998)
      ).future()
    }
    Await.result(creationAndDeletion, 5.seconds)
    creationAndDeletion.value.get.isSuccess should be(true)
    creationAndDeletion.value.get.get.size should be(2)

    // should be committed
    val deleted = NamedDB('mysql).readOnly { implicit s =>
      withSQL { select.from(AsyncLover as al).where.eq(al.id, 998) }.map(AsyncLover(al)).single.apply()
    }
    deleted.isDefined should be(false)
  }

  it should "rollback in a transaction when using AsyncTx.withSQLs" in {
    val failure: Future[Seq[AsyncQueryResult]] = NamedAsyncDB('mysql).withPool { implicit s =>
      AsyncTx.withSQLs(
        insert.into(AsyncLover).namedValues(
          column.id -> 999,
          column.name -> "George",
          column.rating -> 1,
          column.isReactive -> false,
          column.createdAt -> DateTime.now).toSQL,
        sql"invalid_query" // failure
      ).future()
    }
    try {
      Await.result(failure, 5.seconds)
      fail("Exception expected")
    } catch {
      case e: Exception => log.debug("expected", e)
    }
    failure.value.get.isSuccess should be(false)

    val notFound = NamedDB('mysql).readOnly { implicit s =>
      withSQL { select.from(AsyncLover as al).where.eq(al.id, 999) }.map(AsyncLover(al)).single.apply()
    }
    notFound.isDefined should be(false)
  }

}
