package models

import scalikejdbc._, async._, FutureImplicits._, SQLInterpolation._
import scala.concurrent._
import org.joda.time.DateTime

case class Company(
    id: Long,
    name: String,
    url: Option[String] = None,
    createdAt: DateTime,
    deletedAt: Option[DateTime] = None) extends ShortenedNames {

  def save()(implicit session: AsyncDBSession = AsyncDB.sharedSession, cxt: EC = ECGlobal): Future[Company] = Company.save(this)(session, cxt)
  def destroy()(implicit session: AsyncDBSession = AsyncDB.sharedSession, cxt: EC = ECGlobal): Future[Int] = Company.destroy(id)(session, cxt)
}

object Company extends SQLSyntaxSupport[Company] with ShortenedNames {

  override val columnNames = Seq("id", "name", "url", "created_at", "deleted_at")

  def apply(c: SyntaxProvider[Company])(rs: WrappedResultSet): Company = apply(c.resultName)(rs)
  def apply(c: ResultName[Company])(rs: WrappedResultSet): Company = new Company(
    id = rs.long(c.id),
    name = rs.string(c.name),
    url = rs.stringOpt(c.url),
    createdAt = rs.timestamp(c.createdAt).toDateTime,
    deletedAt = rs.timestampOpt(c.deletedAt).map(_.toDateTime)
  )

  lazy val c = Company.syntax("c")
  private val isNotDeleted = sqls.isNull(c.deletedAt)

  def find(id: Long)(implicit session: AsyncDBSession = AsyncDB.sharedSession, cxt: EC = ECGlobal): Future[Option[Company]] = withSQL {
    select.from(Company as c).where.eq(c.id, id).and.append(isNotDeleted)
  }.map(Company(c))

  def findAll()(implicit session: AsyncDBSession = AsyncDB.sharedSession, cxt: EC = ECGlobal): Future[List[Company]] = withSQL {
    select.from(Company as c)
      .where.append(isNotDeleted)
      .orderBy(c.id)
  }.map(Company(c))

  def countAll()(implicit session: AsyncDBSession = AsyncDB.sharedSession, cxt: EC = ECGlobal): Future[Long] = withSQL {
    select(sqls.count).from(Company as c).where.append(isNotDeleted)
  }.map(rs => rs.long(1)).single.future.map(_.get)

  def findAllBy(where: SQLSyntax)(
    implicit session: AsyncDBSession = AsyncDB.sharedSession, cxt: EC = ECGlobal): Future[List[Company]] = withSQL {
    select.from(Company as c)
      .where.append(isNotDeleted).and.append(sqls"${where}")
      .orderBy(c.id)
  }.map(Company(c))

  def countBy(where: SQLSyntax)(
    implicit session: AsyncDBSession = AsyncDB.sharedSession, cxt: EC = ECGlobal): Future[Long] = withSQL {
    select(sqls.count).from(Company as c).where.append(isNotDeleted).and.append(sqls"${where}")
  }.map(_.long(1)).single.future.map(_.get)

  def create(name: String, url: Option[String] = None, createdAt: DateTime = DateTime.now)(
    implicit session: AsyncDBSession = AsyncDB.sharedSession, cxt: EC = ECGlobal): Future[Company] = {
    for {
      id <- withSQL {
        insert.into(Company).namedValues(
          column.name -> name,
          column.url -> url,
          column.createdAt -> createdAt)
          .returningId // if you run this example for MySQL, please remove this line
      }.updateAndReturnGeneratedKey()
    } yield Company(id = id, name = name, url = url, createdAt = createdAt)
  }

  def save(m: Company)(implicit session: AsyncDBSession = AsyncDB.sharedSession, cxt: EC = ECGlobal): Future[Company] = {
    withSQL {
      update(Company).set(
        column.name -> m.name,
        column.url -> m.url
      ).where.eq(column.id, m.id).and.isNull(column.deletedAt)
    }.update.future.map(_ => m)
  }

  def destroy(id: Long)(implicit session: AsyncDBSession = AsyncDB.sharedSession, cxt: EC = ECGlobal): Future[Int] = {
    update(Company).set(column.deletedAt -> DateTime.now).where.eq(column.id, id)
  }

}
