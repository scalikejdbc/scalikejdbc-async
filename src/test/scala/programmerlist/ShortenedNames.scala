package programmerlist

import scala.concurrent.ExecutionContext
import scalikejdbc.async.AsyncDBSession

trait ShortenedNames {

  type EC = ExecutionContext
  val EC = ExecutionContext
  val ECGlobal = ExecutionContext.Implicits.global
  type Session = AsyncDBSession

}
