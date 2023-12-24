package sample

import java.sql.Timestamp
import java.time.Instant
import scalikejdbc._

case class AsyncLover(
  id: Long,
  name: String,
  rating: Int,
  isReactive: Boolean,
  lunchtime: Option[java.sql.Time] = None,
  birthday: Option[Instant] = None,
  nanotime: Option[Timestamp] = None,
  createdAt: Instant = Instant.now,
  deletedAt: Option[Instant] = None
)

object AsyncLover extends SQLSyntaxSupport[AsyncLover] {

  override val columns: Seq[String] = Seq(
    "id",
    "name",
    "rating",
    "is_reactive",
    "lunchtime",
    "birthday",
    "created_at",
    "nanotime"
  )

  def apply(c: SyntaxProvider[AsyncLover])(rs: WrappedResultSet): AsyncLover =
    apply(c.resultName)(rs)

  def apply(c: ResultName[AsyncLover])(rs: WrappedResultSet): AsyncLover =
    new AsyncLover(
      id = rs.get[Long](c.id),
      name = rs.get[Option[String]](c.name).get,
      rating = rs.get[Int](c.rating),
      isReactive = rs.get[Boolean](c.isReactive),
      lunchtime = rs.get[Option[java.sql.Time]](c.lunchtime),
      birthday = rs.get[Option[Instant]](c.lunchtime),
      nanotime = rs.get[Option[Timestamp]](c.nanotime),
      createdAt = rs.get[Instant](c.createdAt)
    )

}
