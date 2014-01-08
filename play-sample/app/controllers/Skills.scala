package controllers

import play.api._, mvc._
import play.api.data._, Forms._, validation.Constraints._

import org.json4s._, ext.JodaTimeSerializers
import com.github.tototoshi.play2.json4s.native._

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

import models._

object Skills extends Controller with Json4s {

  implicit val formats = DefaultFormats ++ JodaTimeSerializers.all

  def all = Action.async {
    Skill.findAll.map(skills => Ok(Extraction.decompose(skills)))
  }

  def show(id: Long) = Action.async {
    Skill.find(id).map { skillOpt =>
      skillOpt map { skill =>
        Ok(Extraction.decompose(skill))
      } getOrElse NotFound
    }
  }

  case class SkillForm(name: String)

  private val skillForm = Form(
    mapping("name" -> text.verifying(nonEmpty))(SkillForm.apply)(SkillForm.unapply)
  )

  def create = Action.async { implicit req =>
    skillForm.bindFromRequest.fold(
      formWithErrors => Future.successful(BadRequest("invalid parameters")),
      form => Skill.create(name = form.name).map { skill =>
        Created.withHeaders(LOCATION -> s"/skills/${skill.id}")
      }
    )
  }

  def delete(id: Long) = Action.async {
    Skill.find(id).map { skillOpt =>
      skillOpt map { skill =>
        skill.destroy()
        NoContent
      } getOrElse NotFound
    }
  }

}
