package models

import scalikejdbc._

case class ProgrammerSkill(programmerId: Long, skillId: Long)

object ProgrammerSkill extends SQLSyntaxSupport[ProgrammerSkill] {

  override val columnNames = Seq("programmer_id", "skill_id")

  lazy val ps = ProgrammerSkill.syntax("ps")
}

