package sample

import scalikejdbc._
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
    "id", "name", "rating", "is_reactive", "lunchtime", "birthday", "created_at")

  def apply(c: SyntaxProvider[AsyncLover])(rs: WrappedResultSet): AsyncLover = apply(c.resultName)(rs)

  def apply(c: ResultName[AsyncLover])(rs: WrappedResultSet): AsyncLover = new AsyncLover(
    id = rs.get[Long](c.id),
    name = rs.get[Option[String]](c.name).get,
    rating = rs.get[Int](c.rating),
    isReactive = rs.get[Boolean](c.isReactive),
    lunchtime = rs.get[Option[java.sql.Time]](c.lunchtime),
    birthday = rs.get[Option[DateTime]](c.lunchtime),
    createdAt = rs.get[DateTime](c.createdAt))

}

