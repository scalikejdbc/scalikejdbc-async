package parameterbinderfactory

import scalikejdbc._
import scalikejdbc.TypeBinder._
import scalikejdbc.async._
import scala.concurrent.Future

case class AccountId(id: Long)

case class Account(id: AccountId, personId: PersonId, accountDetails: String, parent: Option[AccountId])

object AccountId {
  implicit val AccountIdParameterBinderFactory: Binders[AccountId] = Binders.long.xmap(AccountId.apply, _.id)
}

object Account extends SQLSyntaxSupport[Account] {
  //override def columnNames 
  def apply(s: SyntaxProvider[Account])(rs: WrappedResultSet): Account = apply(s.resultName)(rs)

  def opt(s: SyntaxProvider[Account])(rs: WrappedResultSet): Option[Account] = try {
    rs.stringOpt(s.resultName.id).map(_ => apply(s)(rs))
  } catch {
    case e: IllegalArgumentException => None
  }

  override lazy val columns = autoColumns[Account]()
  def apply(rn: ResultName[Account])(rs: WrappedResultSet): Account = autoConstruct(rs, rn)
}