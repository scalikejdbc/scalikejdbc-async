package example

import scalikejdbc._, SQLInterpolation._
import org.joda.time.DateTime

case class Company(id: Long, name: String, createdAt: Option[DateTime] = None)

object Company extends SQLSyntaxSupport[Company] {
  override val columns = Seq("id", "name", "created_at")

  def apply(c: SyntaxProvider[Company])(rs: WrappedResultSet): Company = apply(c.resultName)(rs)

  def apply(c: ResultName[Company])(rs: WrappedResultSet): Company = new Company(
    id = rs.long(c.id),
    name = rs.string(c.name),
    createdAt = rs.timestampOpt(c.createdAt).map(_.toDateTime))
}

