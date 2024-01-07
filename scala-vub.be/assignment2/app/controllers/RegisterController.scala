package controllers

import models.{User, UserDao}
import play.api.*
import play.api.data.Form
import play.api.data.Forms.{mapping, nonEmptyText}
import play.api.mvc.*

import javax.inject.*

@Singleton
class RegisterController @Inject() (
    cc: MessagesControllerComponents
) extends MessagesAbstractController(cc) {
  val userForm: Form[User] = Form(
    mapping(
      "username" -> nonEmptyText,
      "password" -> nonEmptyText
    )(User.apply)(User.unapply).verifying(
      "Username already exists",
      user => {
        !UserDao.exists(user.username)
      }
    )
  )

  def index(): Action[AnyContent] = {
    Action { implicit request: MessagesRequest[AnyContent] =>
      Ok(views.html.register(userForm))
    }
  }

  def registerSubmit(): Action[AnyContent] = {
    Action { implicit request: MessagesRequest[AnyContent] =>
      userForm
        .bindFromRequest()
        .fold(
          formWithErrors => {
            BadRequest(views.html.register(formWithErrors))
          },
          user => {
            UserDao.create(user)
            Redirect(routes.LoginController.index()).flashing(
              s"INFO" -> s"User ${user.username} created successfully"
            )
          }
        )
    }
  }
}
