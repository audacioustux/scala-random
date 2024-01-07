package controllers
import models.{Post, PostDao}
import play.api.*
import play.api.mvc.*

import javax.inject.*

@Singleton
class FeedController @Inject() (val controllerComponents: ControllerComponents)
    extends BaseController {

  def index(
      sort: Option[String] = None
  ): Action[AnyContent] = Action { implicit request: Request[AnyContent] =>
    val posts = PostDao.getAll()
    sort match {
      case Some("date") => {
        var sortedPosts = posts.sortBy(_.getCreatedAt).reverse
        Ok(views.html.feed(sortedPosts))
      }
      case Some("like") => {
        var sortedPosts = posts.sortBy(_.likes.length).reverse
        Ok(views.html.feed(sortedPosts))
      }
      case _ => Ok(views.html.feed(posts))
    }
  }

  def likedPost(
      postId: Long,
      sort: Option[String] = None
  ): Action[AnyContent] = Action { implicit request: Request[AnyContent] =>
    PostDao.getById(postId) match
      case Some(post) => {
        post.likedByUser(utils.getSessionUsername(request.session).get)
        Redirect(routes.FeedController.index(sort))

      }
      case None => BadRequest("Post not found")
  }
}
