package controllers

import models.{Global, User}
import play.api.data.*
import play.api.data.Forms.*
import play.api.mvc.*

import javax.inject.*

@Singleton
class LoginController @Inject() (
    cc: MessagesControllerComponents
) extends MessagesAbstractController(cc) {

  val form: Form[User] = Form(
    mapping(
      "username" -> nonEmptyText,
      "password" -> nonEmptyText
    )(User.apply)(User.unapply)
      .verifying(
        "Invalid username/password",
        user => {
          User.verifyUser(user)
        }
      )
  )

  def index(): Action[AnyContent] = {
    Action { implicit request: MessagesRequest[AnyContent] =>
      Ok(views.html.login(form))
    }
  }

  def processLogin(): Action[AnyContent] = {
    Action { implicit request: MessagesRequest[AnyContent] =>
      {
        val errorFunction = { (formWithErrors: Form[User]) =>
          BadRequest(views.html.login(formWithErrors))
        }
        val successFunction = { (user: User) =>
          {
            Redirect(routes.HomeController.index())
              .flashing("INFO" -> "You are logged in.")
              .withSession(Global.SESSION_USERNAME_KEY -> user.username)
          }
        }
        val formValidationResult: Form[User] = form.bindFromRequest()
        formValidationResult.fold(
          errorFunction,
          successFunction
        )
      }
    }
  }
}
