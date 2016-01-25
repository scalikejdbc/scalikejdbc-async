package sample

import org.joda.time._
import org.scalatest._
import scala.concurrent._, duration.DurationInt, ExecutionContext.Implicits.global
import scalikejdbc._, async._
import unit._

class PostgreSQLSampleSpec extends FlatSpec with Matchers with DBSettings with Logging {

  val column = AsyncLover.column
  val createdTime = DateTime.now.withMillisOfSecond(0)
  val al = AsyncLover.syntax("al")

  it should "count" in {
    val countFuture: Future[Long] = AsyncDB.withPool { implicit s =>
      withSQL { select(sqls.count).from(AsyncLover as al) }.map(_.long(1)).single.future().map(_.get)
    }
    val c = Await.result(countFuture, 5.seconds)
    c should be > 0L
  }

  it should "select a single value" in {
    val resultFuture: Future[Option[AsyncLover]] = AsyncDB.withPool { implicit s =>
      withSQL { select.from(AsyncLover as al).where.eq(al.id, 1) }.map(AsyncLover(al)).single.future()
    }
    val result = Await.result(resultFuture, 5.seconds)
    result.isDefined should be(true)
  }

  it should "select values as a Traversable" in {
    val resultsFuture: Future[Traversable[AsyncLover]] = AsyncDB.withPool { implicit s =>
      withSQL { select.from(AsyncLover as al).limit(2) }.map(AsyncLover(al)).traversable.future()
    }
    Await.result(resultsFuture, 5.seconds)
    val results = resultsFuture.value.get.get
    results.size should equal(2)
  }

  it should "select values as a List" in {
    val resultsFuture: Future[List[AsyncLover]] = AsyncDB.withPool { implicit s =>
      withSQL { select.from(AsyncLover as al).limit(2) }.map(AsyncLover(al)).list.future()
    }
    val results = Await.result(resultsFuture, 5.seconds)
    results.size should equal(2)
  }

  it should "read values with convenience methods" in {
    val generatedIdFuture: Future[Long] = AsyncDB.withPool { implicit s =>
      withSQL {
        insert.into(AsyncLover).namedValues(
          column.name -> "Eric",
          column.rating -> 2,
          column.isReactive -> false,
          column.createdAt -> createdTime).returningId
      }.updateAndReturnGeneratedKey.future()
    }
    // in AsyncLover#apply we are using get with typebinders, specialized getters should work
    val generatedId = Await.result(generatedIdFuture, 5.seconds)
    val created = DB.readOnly { implicit s =>
      withSQL { select.from(AsyncLover as al).where.eq(al.id, generatedId) }.map((rs: WrappedResultSet) => {
        AsyncLover(
          id = rs.long(al.resultName.id),
          name = rs.stringOpt(al.resultName.name).get,
          rating = rs.int(al.resultName.rating),
          isReactive = rs.boolean(al.resultName.isReactive),
          lunchtime = rs.timeOpt(al.resultName.lunchtime),
          birthday = rs.jodaDateTimeOpt(al.resultName.lunchtime),
          createdAt = rs.jodaDateTime(al.resultName.createdAt))
      }).single.apply()
    }.get
    created.id should equal(generatedId)
    created.name should equal("Eric")
    created.rating should equal(2)
    created.isReactive should be(false)
    created.createdAt should equal(createdTime)
  }

  it should "return generated key" in {
    val generatedIdFuture: Future[Long] = AsyncDB.withPool { implicit s =>
      withSQL {
        insert.into(AsyncLover).namedValues(
          column.name -> "Eric",
          column.rating -> 2,
          column.isReactive -> false,
          column.createdAt -> createdTime).returningId
      }.updateAndReturnGeneratedKey.future()
    }
    // the generated key should be found
    val generatedId = Await.result(generatedIdFuture, 5.seconds)
    val created = DB.readOnly { implicit s =>
      withSQL { select.from(AsyncLover as al).where.eq(al.id, generatedId) }.map(AsyncLover(al)).single.apply()
    }.get
    created.id should equal(generatedId)
    created.name should equal("Eric")
    created.rating should equal(2)
    created.isReactive should be(false)
    created.createdAt should equal(createdTime)
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
    val deletion: Future[Int] = AsyncDB.withPool { implicit s =>
      withSQL { delete.from(AsyncLover).where.eq(column.id, 1004) }.update.future()
    }
    Await.result(deletion, 5.seconds)

    // should be committed
    val deleted = DB.readOnly { implicit s =>
      withSQL { select.from(AsyncLover as al).where.eq(al.id, 1004) }.map(AsyncLover(al)).single.apply()
    }
    deleted.isDefined should be(false)
  }

  it should "execute" in {
    // execution should be successful
    val id = DB.autoCommit { implicit s =>
      withSQL {
        insert.into(AsyncLover).namedValues(
          column.name -> "Chris",
          column.rating -> 5,
          column.isReactive -> true,
          column.createdAt -> createdTime
        )
      }.updateAndReturnGeneratedKey.apply()
    }
    val deletion: Future[Boolean] = AsyncDB.withPool { implicit s =>
      withSQL { delete.from(AsyncLover).where.eq(column.id, id) }.execute.future()
    }
    Await.result(deletion, 5.seconds)

    // should be committed
    val deleted = DB.readOnly { implicit s =>
      withSQL { select.from(AsyncLover as al).where.eq(al.id, id) }.map(AsyncLover(al)).single.apply()
    }
    deleted.isDefined should be(false)
  }

  it should "update in a local transaction" in {
    (1 to 10).foreach { _ =>

      val generatedIdFuture: Future[Long] = AsyncDB.localTx { implicit tx =>
        withSQL(insert.into(AsyncLover).namedValues(
          column.name -> "Patric",
          column.rating -> 2,
          column.isReactive -> false,
          column.createdAt -> createdTime
        ).returningId).updateAndReturnGeneratedKey.future
      }
      val generatedId = Await.result(generatedIdFuture, 5.seconds)
      val created = DB.readOnly { implicit s =>
        withSQL { select.from(AsyncLover as al).where.eq(al.id, generatedId) }.map(AsyncLover(al)).single.apply()
      }.get
      created.id should equal(generatedId)
      created.name should equal("Patric")
      created.rating should equal(2)
      created.isReactive should be(false)
      created.createdAt should equal(createdTime)
    }
  }

  it should "rollback in a local transaction" in {
    (1 to 10).foreach { _ =>

      DB.autoCommit { implicit s =>
        withSQL { delete.from(AsyncLover).where.eq(column.id, 1003) }.update.apply()
      }
      val failureFuture: Future[Unit] = AsyncDB.localTx { implicit tx =>
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
        Await.result(failureFuture, 5.seconds)
        fail("Exception expected")
      } catch {
        case e: Exception => log.debug("expected", e)
      }
      failureFuture.value.get.isFailure should be(true)

      // should be rolled back
      val notCreated = DB.readOnly { implicit s =>
        withSQL { select.from(AsyncLover as al).where.eq(al.id, 1003) }.map(AsyncLover(al)).single.apply()
      }
      notCreated.isDefined should be(false)
    }
  }

  it should "provide transaction by AsyncTx.withSQLBuilders" in {
    (1 to 10).foreach { _ =>

      val deletionAndCreation: Future[Seq[AsyncQueryResult]] = AsyncDB.withPool { implicit s =>
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
      val created = DB.readOnly { implicit s =>
        withSQL { select.from(AsyncLover as al).where.eq(al.id, 997) }.map(AsyncLover(al)).single.apply()
      }.get
      created.id should equal(997)
      created.name should equal("Eric")
      created.rating should equal(2)
      created.isReactive should be(false)
      created.createdAt should equal(createdTime)
    }
  }

  it should "provide transactional deletion by AsyncTx.withSQLBuilders" in {
    (1 to 10).foreach { _ =>

      DB.autoCommit { implicit s =>
        withSQL { delete.from(AsyncLover).where.eq(column.id, 998) }.update.apply()
      }
      val creationAndDeletion: Future[Seq[AsyncQueryResult]] = AsyncDB.withPool { implicit s =>
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
      val deleted = DB.readOnly { implicit s =>
        withSQL { select.from(AsyncLover as al).where.eq(al.id, 998) }.map(AsyncLover(al)).single.apply()
      }
      deleted.isDefined should be(false)
    }
  }

  it should "rollback in a transaction when using AsyncTx.withSQLs" in {
    (1 to 10).foreach { _ =>

      val failure: Future[Seq[AsyncQueryResult]] = AsyncDB.withPool { implicit s =>
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

      val notFound = DB.readOnly { implicit s =>
        withSQL { select.from(AsyncLover as al).where.eq(al.id, 999) }.map(AsyncLover(al)).single.apply()
      }
      notFound.isDefined should be(false)
    }
  }

}
