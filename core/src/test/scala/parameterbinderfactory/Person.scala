package parameterbinderfactory

import scalikejdbc._
import scalikejdbc.TypeBinder._
import scalikejdbc.async._
import scala.concurrent.Future

case class PersonId(id: Long)

case class Person(id: PersonId, name: String)

object PersonId {
  implicit val personIdParameterBinderFactory: Binders[PersonId] = Binders.long.xmap(PersonId.apply, _.id)
}

object Person extends SQLSyntaxSupport[Person] {
  //override def columnNames
  def apply(p: SyntaxProvider[Person])(rs: WrappedResultSet): Person = apply(p.resultName)(rs)

  def opt(p: SyntaxProvider[Person])(rs: WrappedResultSet): Option[Person] = try {
    rs.stringOpt(p.resultName.id).map(_ => apply(p)(rs))
  } catch {
    case e: IllegalArgumentException => None
  }

  override lazy val columns = autoColumns[Person]()
  def apply(rn: ResultName[Person])(rs: WrappedResultSet): Person = autoConstruct(rs, rn)
}