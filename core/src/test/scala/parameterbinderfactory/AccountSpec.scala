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

  case class AccountAndPerson(account: Account, person: Person)
  case class PersonWithAccounts(person: Person, accounts: collection.Seq[Account])

  val personId = PersonId(23)
  val personName = "with account"
  val person = Person(personId, personName)

  val accountDetails = "account details"
  val accountId = AccountId(1)
  val account = Account(accountId, personId, accountDetails, None)
  val accountId2 = AccountId(2)
  val account2 = Account(accountId2, personId, accountDetails, Option(accountId))

  it should "insert person with custom binder sync" in {

    val result: Int = NamedDB("mysql").autoCommit { implicit s =>
      withSQL {
        insert.into(Person).namedValues(
          personColumn.id -> personId,
          personColumn.name -> personName)
      }.update.apply()
    }
    result should equal(1)
  }

  it should "insert account with custom binder async" in {
    val resultsFuture: Future[Int] = NamedAsyncDB("mysql").withPool { implicit s =>
      withSQL {
        insert.into(Account).namedValues(
          accountColumn.id -> accountId,
          accountColumn.personId -> personId,
          accountColumn.accountDetails -> accountDetails)
      }.update.future()
    }
    val results = Await.result(resultsFuture, 5.seconds)
    results should equal(1)
  }

  it should "insert account with custom binder with parent set to Some async" in {
    val resultsFuture: Future[Int] = NamedAsyncDB("mysql").withPool { implicit s =>
      withSQL {
        insert.into(Account).namedValues(
          accountColumn.id -> accountId2,
          accountColumn.personId -> personId,
          accountColumn.accountDetails -> accountDetails,
          accountColumn.parent -> Option(accountId))
      }.update.future()
    }
    val results = Await.result(resultsFuture, 5.seconds)
    results should equal(1)
  }

  it should "select person joining account with custom binder sync" in {

    val result: Option[PersonWithAccounts] = NamedDB("mysql").readOnly { implicit s =>
      withSQL {
        selectFrom(Person as p).join(Account as acc).on(p.id, acc.personId)
          .where.eq(p.id, personId)
      }
        .one(Person(p))
        .toMany(
          rs => Account.opt(acc)(rs))
        .map({ (person, accounts) => PersonWithAccounts(person, accounts) })
        .single.apply()
    }
    result should be(Some(PersonWithAccounts(person, Seq(account, account2))))
  }

  it should "select person joining account with custom binder async" in {

    val resultFuture: Future[Option[PersonWithAccounts]] = NamedAsyncDB("mysql").withPool { implicit s =>
      withSQL {
        selectFrom(Person as p).join(Account as acc).on(p.id, acc.personId)
          .where.eq(p.id, personId)
      }
        .one(Person(p))
        .toMany(
          rs => Account.opt(acc)(rs))
        .map({ (person, accounts) => PersonWithAccounts(person, accounts) })
        .single.future()
    }
    val result = Await.result(resultFuture, 5.seconds)
    result should be(Some(PersonWithAccounts(person, Seq(account, account2))))
  }

  it should "select account joining person with custom binder sync" in {
    val result: Option[AccountAndPerson] = NamedDB("mysql").readOnly { implicit s =>
      withSQL {
        selectFrom(Account as acc)
          .join(Person as p)
          .on(acc.personId, p.id)
          .where.eq(acc.id, accountId)
      }
        .one(Account(acc))
        .toMany(
          rs => Person.opt(p)(rs))
        .map({ (acc, p) => AccountAndPerson(acc, p.head) })
        .single
        .apply()
    }
    result should be(Some(AccountAndPerson(account, person)))
  }

  it should "select account joining person with custom binder async" in {
    val resultsFuture: Future[Option[AccountAndPerson]] = NamedAsyncDB("mysql").withPool { implicit s =>
      withSQL {
        selectFrom(Account as acc)
          .join(Person as p)
          .on(acc.personId, p.id)
          .where.eq(acc.id, accountId)
      }
        .one(Account(acc))
        .toMany(
          rs => Person.opt(p)(rs))
        .map({ (acc, p) => AccountAndPerson(acc, p.head) })
        .single.future()
    }
    val result = Await.result(resultsFuture, 5.seconds)
    result should be(Some(AccountAndPerson(account, person)))
  }
}
