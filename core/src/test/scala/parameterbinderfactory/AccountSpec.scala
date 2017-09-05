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

class AccountSpec extends FlatSpec with Matchers with DBSettings with Logging {

  val p = Person.syntax("p")
  val acc = Account.syntax("a")

  private val personColumn = Person.column
  private val accountColumn = Account.column

  it should "insert person with custom binder sync" in {

    val result: Int = NamedDB('mysql).autoCommit { implicit s =>
      withSQL {
        insert.into(Person).namedValues(
          personColumn.id -> PersonId(23),
          personColumn.name -> "with account"
        )
      }.update.apply()
    }
    result should equal(1)
  }

  it should "insert account with custom binder async" in {
    val resultsFuture: Future[Int] = NamedAsyncDB('mysql).withPool { implicit s =>
      withSQL {
        insert.into(Account).namedValues(
          accountColumn.id -> AccountId(1),
          accountColumn.personId -> PersonId(23),
          accountColumn.accountDetails -> "account details"
        )
      }.update.future()
    }
    val results = Await.result(resultsFuture, 5.seconds)
    results should equal(1)
  }

  it should "select person joining account with custom binder sync" in {
    val id = PersonId(23)
    val name = "with account"

    val result: Option[Person] = NamedDB('mysql).readOnly { implicit s =>
      withSQL {
        selectFrom(Person as p).join(Account as acc).on(p.id, acc.personId)
          .where.eq(p.id, id)
      }.map(Person(p)).single.apply()
    }
    result should be(Some(Person(id, name)))
  }

  it should "select person joining account with custom binder async" in {
    val id = PersonId(23)
    val name = "with account"

    val resultFuture: Future[Option[Person]] = NamedAsyncDB('mysql).withPool { implicit s =>
      withSQL {
        selectFrom(Person as p).join(Account as acc).on(p.id, acc.personId)
          .where.eq(p.id, id)
      }.map(Person(p)).single.future()
    }
    val result = Await.result(resultFuture, 5.seconds)
    result should be(Some(Person(id, name)))
  }

  it should "select account joining person with custom binder sync" in {
    val id = PersonId(12)
    val name = "testperson"

    val result: Option[Person] = NamedDB('mysql).readOnly { implicit s =>
      withSQL {
        selectFrom(Person as p).join(Account as acc).on(p.id, acc.personId)
          .where.eq(p.id, id)
      }.map(Person(p)).single.apply()
    }
    result should be(Some(Person(id, name)))
  }

  it should "select account joining person with custom binder async" in {
    val id = PersonId(12)
    val name = "testperson"

    val resultsFuture: Future[Option[Person]] = NamedAsyncDB('mysql).withPool { implicit s =>
      withSQL {
        selectFrom(Person as p).join(Account as acc).on(p.id, acc.personId)
          .where.eq(p.id, id)
      }.map(Person(p)).single.future()
    }
    val result = Await.result(resultsFuture, 5.seconds)
    result should be(Some(Person(id, name)))
  }
}
