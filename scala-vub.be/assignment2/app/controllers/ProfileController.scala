package controllers
import play.api.*
import play.api.mvc.*

import javax.inject.*

@Singleton
class ProfileController @Inject() (
    val controllerComponents: ControllerComponents
) extends BaseController {

  def index(): Action[AnyContent] = Action {
    implicit request: Request[AnyContent] =>
      Ok(views.html.profile())
  }
}
