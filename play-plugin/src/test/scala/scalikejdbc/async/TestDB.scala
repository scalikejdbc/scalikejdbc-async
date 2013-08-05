package scalikejdbc.async

import scala.sys.process._

object TestDB {

  @volatile var process: Process = _

  def setup(): Unit = {
    process = "java -cp h2-1.3.173.jar org.h2.tools.Server -pg".run
    Thread.sleep(2000) // waiting for H2DB server awakening
  }

  def cleanup(): Unit = {
    process.destroy()
  }

}
