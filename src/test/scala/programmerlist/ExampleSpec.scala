package programmerlist

import org.joda.time._
import org.scalatest._, matchers._
import org.slf4j.LoggerFactory
import scala.concurrent._, duration._, ExecutionContext.Implicits.global
import scalikejdbc._, async._

class ExampleSpec extends FlatSpec with ShouldMatchers with unit.DBSettings {

  val log = LoggerFactory.getLogger(classOf[ExampleSpec])
  val createdTime = DateTime.now.withMillisOfSecond(0)

  it should "work with ConnectionPool" in {
    val findAllFuture: Future[List[Programmer]] = AsyncDB.withPool { implicit session =>
      Programmer.findAll
    }
    // TODO one-to-x support
    try {
      Await.result(findAllFuture, 5.seconds)
      findAllFuture.foreach { programmers =>
        log.debug(s"Programmers: ${programmers}")
        programmers.size should be > 0
      }
    } catch { case e: Exception => e.printStackTrace() }
  }

}
