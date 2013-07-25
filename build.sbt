scalariformSettings

net.virtualvoid.sbt.graph.Plugin.graphSettings

initialCommands := """
import scalikejdbc._, SQLInterpolation._, async._, Implicits._
Class.forName("org.postgresql.Driver")
ConnectionPool.singleton("jdbc:postgresql://localhost:5432/scalikejdbc", "sa", "sa")
implicit val session = AutoSession
import scala.concurrent._
import ExecutionContext.Implicits.global
"""

