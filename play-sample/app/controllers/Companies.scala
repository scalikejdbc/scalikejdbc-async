package controllers

import play.api._, mvc._
import play.api.data._, Forms._, validation.Constraints._

import org.json4s._, ext.JodaTimeSerializers
import com.github.tototoshi.play2.json4s.native._

import scala.concurrent.ExecutionContext.Implicits.global

import models._

object Companies extends Controller with Json4s {

  implicit val formats = DefaultFormats ++ JodaTimeSerializers.all

  def all = Action {
    Async {
      Company.findAll.map(companies => Ok(Extraction.decompose(companies)))
    }
  }

  def show(id: Long) = Action {
    Async {
      Company.find(id) map { companyOpt =>
        companyOpt map { company => Ok(Extraction.decompose(company)) } getOrElse NotFound
      }
    }
  }

  case class CompanyForm(name: String, url: Option[String] = None)

  private val companyForm = Form(
    mapping(
      "name" -> text.verifying(nonEmpty),
      "url" -> optional(text)
    )(CompanyForm.apply)(CompanyForm.unapply)
  )

  def create = Action { implicit req =>
    companyForm.bindFromRequest.fold(
      formWithErrors => BadRequest("invalid parameters"),
      form => {
        Async {
          Company.create(name = form.name, url = form.url).map { company =>
            Created.withHeaders(LOCATION -> s"/companies/${company.id}")
          }
        }
      }
    )
  }

  def delete(id: Long) = Action {
    Async {
      Company.find(id).map { companyOpt =>
        companyOpt map { company =>
          company.destroy()
          NoContent
        } getOrElse NotFound
      }
    }
  }

}
