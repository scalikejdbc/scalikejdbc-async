package programmerlist

import scalikejdbc._, SQLInterpolation._, async._
import scala.concurrent._, duration._, ExecutionContext.Implicits.global

import org.scalatest._, matchers._
import unit._

class ExampleSpec extends FlatSpec with ShouldMatchers with DBSettings with Logging {

  val p = Programmer.syntax("p")

  it should "work with ConnectionPool" in {
    val findAllFuture: Future[List[Programmer]] = AsyncDB.withPool { implicit session =>
      Programmer.findAll
    }

    Await.result(findAllFuture, 5.seconds)
    findAllFuture.foreach { programmers: List[Programmer] =>

      log.debug(s"Programmers: ${programmers}")
      programmers.size should be > 0
    }
  }

  it should "work within a transaction" in {

    // create a new record within a transaction
    val created: Future[Company] = AsyncDB.localTx { implicit tx =>
      for {
        company <- Company.create("ScalikeJDBC, Inc.", Some("http://scalikejdbc.org/"))
        seratch <- Programmer.create("seratch", Some(company.id))
        gakuzzzz <- Programmer.create("gakuzzzz", Some(company.id))
        tototoshi <- Programmer.create("tototoshi", Some(company.id))
        cb372 <- Programmer.create("cb372", Some(company.id))
      } yield company
    }

    Await.result(created, 5.seconds)
    created.foreach { newCompany: Company =>

      // delete a record and rollback
      val withinTx: Future[Unit] = AsyncDB.localTx { implicit tx =>
        for {
          restructuring <- Programmer.findAllBy(sqls.eq(p.companyId, newCompany.id)).map { programmers =>
            programmers.foreach(_.destroy())
          }
          dissolution <- newCompany.destroy()
          _ <- sql"Just joking!".update.future
        } yield ()
      }
      try Await.result(withinTx, 5.seconds)
      catch { case e: Exception => log.debug(e.getMessage, e) }

      // rollback expected
      val company = AsyncDB.withPool { implicit s =>
        Company.find(newCompany.id)
      }
      Await.result(company, 5.seconds)
      val found: Option[Company] = company.value.get.get
      found.isDefined should be(true)
    }
  }

}
