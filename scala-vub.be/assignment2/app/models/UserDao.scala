package models

object UserDao {
  private var users: Seq[User] = Dao.users

  def exists(username: String): Boolean = {
    users.exists(u => u.username == username)
  }

  def create(user: User): Unit = {
    users = users :+ user
  }

  def find(username: String): Option[User] = {
    users.find(u => u.username == username)
  }
}
