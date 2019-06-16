package programmerlist

import scalikejdbc._, async._, FutureImplicits._
import scalikejdbc.jodatime.JodaParameterBinderFactory._
import scalikejdbc.jodatime.JodaTypeBinder._
import org.joda.time.DateTime
import scala.concurrent._

case class Programmer(
  id: Long,
  name: String,
  companyId: Option[Long] = None,
  company: Option[Company] = None,
  skills: collection.Seq[Skill] = Nil,
  createdAt: DateTime,
  deletedAt: Option[DateTime] = None) extends ShortenedNames {

  def save()(implicit session: AsyncDBSession = AsyncDB.sharedSession, cxt: EC = ECGlobal): Future[Programmer] = Programmer.save(this)(session, cxt)
  def destroy()(implicit session: AsyncDBSession = AsyncDB.sharedSession, cxt: EC = ECGlobal): Future[Int] = Programmer.destroy(id)(session, cxt)

  private val column = ProgrammerSkill.column

  def addSkill(skill: Skill)(implicit session: AsyncDBSession = AsyncDB.sharedSession, cxt: EC = ECGlobal): Future[Int] = {
    insert.into(ProgrammerSkill).namedValues(
      column.programmerId -> id,
      column.skillId -> skill.id)
  }

  def deleteSkill(skill: Skill)(implicit session: AsyncDBSession = AsyncDB.sharedSession, cxt: EC = ECGlobal): Future[Int] = {
    delete.from(ProgrammerSkill).where.eq(column.programmerId, id).and.eq(column.skillId, skill.id)
  }

}

object Programmer extends SQLSyntaxSupport[Programmer] with ShortenedNames {

  override val columnNames = Seq("id", "name", "company_id", "created_timestamp", "deleted_timestamp")
  override val nameConverters = Map("At$" -> "_timestamp")

  // simple extractor
  def apply(p: SyntaxProvider[Programmer])(rs: WrappedResultSet): Programmer = apply(p.resultName)(rs)
  def apply(p: ResultName[Programmer])(rs: WrappedResultSet): Programmer = new Programmer(
    id = rs.get[Long](p.id),
    name = rs.get[String](p.name),
    companyId = rs.get[Option[Long]](p.companyId),
    createdAt = rs.get[DateTime](p.createdAt),
    deletedAt = rs.get[Option[DateTime]](p.deletedAt))

  // join query with company table
  def apply(p: SyntaxProvider[Programmer], c: SyntaxProvider[Company])(rs: WrappedResultSet): Programmer = {
    apply(p.resultName)(rs).copy(company = rs.longOpt(c.resultName.id).flatMap { _ =>
      if (rs.timestampOpt(c.resultName.deletedAt).isEmpty) Some(Company(c)(rs)) else None
    })
  }

  // SyntaxProvider objects
  lazy val p = Programmer.syntax("p")

  private val (c, s, ps) = (Company.c, Skill.s, ProgrammerSkill.ps)

  // reusable part of SQL
  private val isNotDeleted = sqls.isNull(p.deletedAt)

  // find by primary key
  def find(id: Long)(implicit session: AsyncDBSession = AsyncDB.sharedSession, cxt: EC = ECGlobal): Future[Option[Programmer]] = {
    withSQL {
      select
        .from(Programmer as p)
        .leftJoin(Company as c).on(p.companyId, c.id)
        .leftJoin(ProgrammerSkill as ps).on(ps.programmerId, p.id)
        .leftJoin(Skill as s).on(sqls.eq(ps.skillId, s.id).and.isNull(s.deletedAt))
        .where.eq(p.id, id).and.append(isNotDeleted)
    }.one(Programmer(p, c))
      .toMany(Skill.opt(s))
      .map { (programmer, skills) => programmer.copy(skills = skills) }
      .single.future
  }

  // programmer with company(optional) with skills(many)
  def findAll()(implicit session: AsyncDBSession = AsyncDB.sharedSession, cxt: EC = ECGlobal): Future[List[Programmer]] = {
    withSQL {
      select
        .from[Programmer](Programmer as p)
        .leftJoin(Company as c).on(p.companyId, c.id)
        .leftJoin(ProgrammerSkill as ps).on(ps.programmerId, p.id)
        .leftJoin(Skill as s).on(sqls.eq(ps.skillId, s.id).and.isNull(s.deletedAt))
        .where.append(isNotDeleted)
        .orderBy(p.id)
    }.one(Programmer(p, c))
      .toMany(Skill.opt(s))
      .map { (programmer, skills) => programmer.copy(skills = skills) }
      .list.future
  }

  def findNoSkillProgrammers()(implicit session: AsyncDBSession = AsyncDB.sharedSession, cxt: EC = ECGlobal): Future[List[Programmer]] = {
    withSQL {
      select
        .from(Programmer as p)
        .leftJoin(Company as c).on(p.companyId, c.id)
        .where.notIn(p.id, select(sqls.distinct(ps.programmerId)).from(ProgrammerSkill as ps))
        .and.append(isNotDeleted)
        .orderBy(p.id)
    }.map(Programmer(p, c)).list.future
  }

  def countAll()(implicit session: AsyncDBSession = AsyncDB.sharedSession, cxt: EC = ECGlobal): Future[Long] = withSQL {
    select(sqls.count).from(Programmer as p).where.append(isNotDeleted)
  }.map(rs => rs.long(1)).single.future.map(_.get)

  def findAllBy(where: SQLSyntax, withCompany: Boolean = true)(
    implicit
    session: AsyncDBSession = AsyncDB.sharedSession, cxt: EC = ECGlobal): Future[List[Programmer]] = {
    withSQL {
      select
        .from[Programmer](Programmer as p)
        .map(sql => if (withCompany) sql.leftJoin(Company as c).on(p.companyId, c.id) else sql) // dynamic
        .leftJoin(ProgrammerSkill as ps).on(ps.programmerId, p.id)
        .leftJoin(Skill as s).on(sqls.eq(ps.skillId, s.id).and.isNull(s.deletedAt))
        .where.append(isNotDeleted).and.append(sqls"${where}")
    }.one { rs => if (withCompany) Programmer(p, c)(rs) else Programmer(p)(rs) }
      .toMany(Skill.opt(s))
      .map { (pg, skills) => pg.copy(skills = skills) }
      .list.future
  }

  def countBy(where: SQLSyntax)(
    implicit
    session: AsyncDBSession = AsyncDB.sharedSession, cxt: EC = ECGlobal): Future[Long] = withSQL {
    select(sqls.count).from(Programmer as p).where.append(isNotDeleted).and.append(sqls"${where}")
  }.map(_.long(1)).single.future.map(_.get)

  def create(name: String, companyId: Option[Long] = None, createdAt: DateTime = DateTime.now)(
    implicit
    session: AsyncDBSession = AsyncDB.sharedSession, cxt: EC = ECGlobal): Future[Programmer] = {
    for {
      id <- withSQL {
        insert.into(Programmer).namedValues(
          column.name -> name,
          column.companyId -> companyId,
          column.createdAt -> createdAt)
          .returningId // if you run this example for MySQL, please remove this line
      }.updateAndReturnGeneratedKey.future
    } yield Programmer(
      id = id,
      name = name,
      companyId = companyId,
      createdAt = createdAt)
  }

  def save(m: Programmer)(implicit session: AsyncDBSession = AsyncDB.sharedSession, cxt: EC = ECGlobal): Future[Programmer] = {
    withSQL {
      update(Programmer).set(
        column.name -> m.name,
        column.companyId -> m.companyId).where.eq(column.id, m.id).and.isNull(column.deletedAt)
    }.update.future.map(_ => m)
  }

  def destroy(id: Long)(implicit session: AsyncDBSession = AsyncDB.sharedSession, cxt: EC = ECGlobal): Future[Int] = withSQL {
    update(Programmer).set(column.deletedAt -> DateTime.now).where.eq(column.id, id)
  }.update.future()

}
