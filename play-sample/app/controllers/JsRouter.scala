package controllers

import play.api._, mvc._
import play.api.routing.JavaScriptReverseRouter

object JsRouter extends Controller {

  def javascriptRoutes = Action { implicit request =>
    import routes.javascript._
    Ok(
      JavaScriptReverseRouter()(
        Companies.all,
        Companies.show,
        Companies.create,
        Companies.delete,
        Programmers.all,
        Programmers.show,
        Programmers.create,
        Programmers.addSkill,
        Programmers.deleteSkill,
        Programmers.joinCompany,
        Programmers.leaveCompany,
        Programmers.delete,
        Root.index,
        Skills.all,
        Skills.show,
        Skills.create,
        Skills.delete
      )
    ).as("text/javascript")
  }

}
