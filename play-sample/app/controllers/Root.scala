package controllers

import play.api._, mvc._

object Root extends Controller {

  def index = Action {
    Ok(views.html.index())
  }

}
