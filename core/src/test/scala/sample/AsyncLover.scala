package sample

import scalikejdbc._, SQLInterpolation._
import org.joda.time.DateTime

case class AsyncLover(
  id: Long,
  name: String,
  rating: Int,
  isReactive: Boolean,
  lunchtime: Option[java.sql.Time] = None,
  birthday: Option[DateTime] = None,
  createdAt: DateTime = DateTime.now,
  deletedAt: Option[DateTime] = None)

object AsyncLover extends SQLSyntaxSupport[AsyncLover] {

  override val columns = Seq(
    "id", "name", "rating", "is_reactive", "lunchtime", "birthday", "created_at"
  )

  def apply(c: SyntaxProvider[AsyncLover])(rs: WrappedResultSet): AsyncLover = apply(c.resultName)(rs)

  def apply(c: ResultName[AsyncLover])(rs: WrappedResultSet): AsyncLover = new AsyncLover(
    id = rs.long(c.id),
    //name = rs.string(c.name),
    name = rs.stringOpt(c.name).get,
    rating = rs.int(c.rating),
    isReactive = rs.boolean(c.isReactive),
    lunchtime = rs.timeOpt(c.lunchtime),
    birthday = rs.jodaDateTimeOpt(c.lunchtime),
    createdAt = rs.jodaDateTime(c.createdAt))

}

