package models

import java.time.ZonedDateTime

object PostDao {
  var posts: Seq[Post] = Dao.posts

  def getAll(): Seq[Post] = posts

  def getById(id: Long): Option[Post] = {
    posts.find((post: Post) => post.getId == id)
  }

  def create(user: String, description: String, fileName: String): Post = {
    val lastIndex = posts.lastOption.maxByOption(_.getId)
    val id = lastIndex match {
      case Some(post) => post.getId + 1
      case None       => 1
    }
    val post = Post(
      id,
      user = user,
      imageName = s"$fileName",
      description = description,
      createdAt = ZonedDateTime.now(),
      likes = Seq(),
      comments = Seq()
    )
    posts = posts :+ post
    post
  }
}
