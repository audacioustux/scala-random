package models

import java.time.ZonedDateTime

class Post(
    id: Long,
    user: String,
    imageName: String,
    createdAt: ZonedDateTime,
    description: String,
    var likes: Seq[Like],
    var comments: Seq[Comment]
) {
  def getId: Long = id
  def getUser: String = user
  def getLikes: Seq[Like] = likes
  def getImagePath: String = s"${Global.PUBLIC_IMAGES_PATH}${imageName}"
  def getCreatedAt: ZonedDateTime = createdAt
  def getDescription: String = description
  def getComments: Seq[Comment] = comments

  private def addLike(username: String): Unit = {
    likes = likes :+ Like(username)
  }

  private def removeLike(username: String): Unit = {
    likes = likes.filterNot(_.user == username)
  }

  def hasUserLiked(username: String): Boolean = {
    likes.exists(_.user == username)
  }

  def likedByUser(username: String): Unit = {
    this.hasUserLiked(username) match {
      case true  => this.removeLike(username)
      case false => this.addLike(username)
    }
  }

  def addComment(comment: Comment): Unit = {
    comments = comments :+ comment
  }

  def removeComment(index: Int): Unit = {
    comments = comments.take(index) ++ comments.drop(index + 1)
  }

}
