package controllers

import models.{Comment, PostDao}
import play.api.mvc.{
  Action,
  AnyContent,
  MessagesAbstractController,
  MessagesControllerComponents
}

import javax.inject.{Inject, Singleton}

@Singleton
class PostController @Inject() (
    cc: MessagesControllerComponents
) extends MessagesAbstractController(cc) {

  def index(id: Long): Action[AnyContent] = Action { implicit request =>
    val post = PostDao.getById(id)
    post match {
      case Some(p) => {
        Ok(views.html.post(p))
      }
      case None => NotFound("Not Found")
    }
  }

  def pressedLike(id: Long): Action[AnyContent] = Action { implicit request =>
    PostDao.getById(id) match {
      case Some(post) => {
        post.likedByUser(utils.getSessionUsername(request.session).get)
        Redirect(routes.PostController.index(id))
      }
      case None => NotFound("Not Found")
    }
  }

  def addComment(id: Long): Action[AnyContent] = Action { implicit request =>
    val commentContent = request.body.asFormUrlEncoded.get("comment").head
    if (commentContent.isEmpty) {
      Redirect(routes.PostController.index(id))
        .flashing("Error" -> "Comment cannot be empty")
    } else {

      val comment =
        Comment(utils.getSessionUsername(request.session).get, commentContent)
      PostDao.getById(id) match {
        case Some(post) => {
          post.addComment(comment)
          Redirect(routes.PostController.index(id))
        }
        case None => NotFound("Not Found")
      }
    }
  }
}
