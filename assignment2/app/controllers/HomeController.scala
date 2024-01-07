package controllers
import play.api.*
import play.api.mvc.*

import javax.inject.*

@Singleton
class HomeController @Inject() (val controllerComponents: ControllerComponents)
    extends BaseController {

  def index(): Action[AnyContent] = Action {
    implicit request: Request[AnyContent] =>
      Redirect(routes.FeedController.index())
  }

  def logout(): Action[AnyContent] = Action {
    implicit request: Request[AnyContent] =>
      Redirect(routes.LoginController.index()).withNewSession
  }
}
