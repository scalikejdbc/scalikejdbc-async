package controllers

import play.api._, mvc._
import play.api.data._, Forms._, validation.Constraints._

import org.json4s._, ext.JodaTimeSerializers
import com.github.tototoshi.play2.json4s.native._

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

import models._

object Programmers extends Controller with Json4s {

  implicit val formats = DefaultFormats ++ JodaTimeSerializers.all

  def all = Action.async {
    Programmer.findAll.map(programmers => Ok(Extraction.decompose(programmers)))
  }

  def show(id: Long) = Action.async {
    Programmer.find(id).map { programmerOpt =>
      programmerOpt map { programmer => Ok(Extraction.decompose(programmer)) } getOrElse NotFound
    }
  }

  case class ProgrammerForm(name: String, companyId: Option[Long] = None)

  private val programmerForm = Form(
    mapping(
      "name" -> text.verifying(nonEmpty),
      "companyId" -> optional(longNumber)
    )(ProgrammerForm.apply)(ProgrammerForm.unapply)
  )

  def create = Action.async { implicit req =>
    programmerForm.bindFromRequest.fold(
      formWithErrors => Future.successful(BadRequest("invalid parameters")),
      form => Programmer.create(name = form.name, companyId = form.companyId).map { programmer =>
        Created.withHeaders(LOCATION -> s"/programmers/${programmer.id}")
      }
    )
  }

  def addSkill(programmerId: Long, skillId: Long) = Action.async {
    Programmer.find(programmerId).map { programmerOpt =>
      programmerOpt map { programmer =>
        try {
          Skill.find(skillId).map { skillOpt =>
            skillOpt map { skill => programmer.addSkill(skill) }
          }
          Ok
        } catch { case e: Exception => Conflict }
      } getOrElse NotFound
    }
  }

  def deleteSkill(programmerId: Long, skillId: Long) = Action.async {
    Programmer.find(programmerId).map { programmerOpt =>
      programmerOpt map { programmer =>
        Skill.find(skillId).map { skillOpt =>
          skillOpt map { skill => programmer.addSkill(skill) }
        }
        Ok
      } getOrElse NotFound
    }
  }

  def joinCompany(programmerId: Long, companyId: Long) = Action.async {
    for {
      companyOpt <- Company.find(companyId)
      programmerOpt <- Programmer.find(programmerId)
    } yield {
      companyOpt map { company =>
        programmerOpt.map { programmer =>
          programmer.copy(companyId = Some(company.id)).save()
          Ok
        } getOrElse BadRequest("Programmer not found!")
      } getOrElse BadRequest("Company not found!")
    }
  }

  def leaveCompany(programmerId: Long) = Action.async {
    Programmer.find(programmerId).map { programmerOpt =>
      programmerOpt map { programmer =>
        programmer.copy(companyId = None).save()
        Ok
      } getOrElse BadRequest("Programmer not found!")
    }
  }

  def delete(id: Long) = Action.async {
    Programmer.find(id).map { programmerOpt =>
      programmerOpt map { programmer =>
        programmer.destroy()
        NoContent
      } getOrElse NotFound
    }
  }

}
