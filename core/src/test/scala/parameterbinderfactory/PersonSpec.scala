package parameterbinderfactory

import scalikejdbc._
import scalikejdbc.async._
import scala.concurrent._
import duration._
import ExecutionContext.Implicits.global
import org.scalatest._
import unit._
import programmerlist.Company
import programmerlist.Programmer
import scalikejdbc.async.NamedAsyncDB

class PersonSpec extends FlatSpec with Matchers with DBSettings with Logging {

  val p = Person.syntax("p")
  private val column = Person.column

  it should "insert person with custom binder sync" in {
    val result: Int = NamedDB('mysql).autoCommit { implicit s =>
      withSQL {
        insert.into(Person).namedValues(
          column.id -> PersonId(1),
          column.name -> "test")
      }.update.apply()
    }
    result should equal(1)
  }

  it should "insert person with custom binder async" in {
    val resultsFuture: Future[Int] = NamedAsyncDB('mysql).withPool { implicit s =>
      withSQL {
        insert.into(Person).namedValues(
          column.id -> PersonId(1),
          column.name -> "test")
      }.update.future()
    }
    val results = Await.result(resultsFuture, 5.seconds)
    results should equal(1)
  }

  it should "select person with custom binder sync" in {
    val id = PersonId(12)
    val name = "testperson"

    val result: Option[Person] = NamedDB('mysql).readOnly { implicit s =>
      withSQL {
        selectFrom(Person as p).where.eq(p.id, id)
      }.map(Person(p)).single.apply()
    }
    result should be(Some(Person(id, name)))
  }

  it should "select person with custom binder async" in {
    val id = PersonId(12)
    val name = "testperson"

    val resultsFuture: Future[Option[Person]] = NamedAsyncDB('mysql).withPool { implicit s =>
      withSQL {
        selectFrom(Person as p).where.eq(p.id, id)
      }.map(Person(p)).single.future()
    }
    val result = Await.result(resultsFuture, 5.seconds)
    result should be(Some(Person(id, name)))
  }
}
