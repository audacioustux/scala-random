package models

import java.time.ZonedDateTime

object Dao {
  var users: Seq[User] = Seq(
    User("user1", "password1"),
    User("user2", "password2"),
    User("user3", "password3"),
    User("user4", "password4"),
    User("user5", "password5"),
    User("user6", "password6"),
    User("user7", "password7"),
    User("user8", "password8")
  )
  var posts: Seq[Post] = Seq(
    Post(
      id = 1,
      user = "user1",
      likes = Seq(
        Like("user2", utils.randomDateTime()),
        Like("user3", utils.randomDateTime()),
        Like("user4", utils.randomDateTime()),
        Like("user8", utils.randomDateTime())
      ),
      imageName = "image-1.jpg",
      createdAt = utils.randomDateTime(),
      description =
        "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Nullam euismod, nisl eget ultricies aliquam, nunc nisl aliquet nunc, vitae aliquam nisl nunc eu",
      comments = Seq(
        Comment(
          "user2",
          "Lorem ipsum dolor sit amet, consectetur adipiscing elit."
        ),
        Comment(
          "user3",
          "Nullam euismod, nisl eget ultricies aliquam, nunc nisl aliquet nunc, vitae aliquam nisl nunc eu"
        ),
        Comment(
          "user4",
          "Lorem ipsum dolor sit amet, consectetur adipiscing elit."
        ),
        Comment(
          "user8",
          "Nullam euismod, nisl eget ultricies aliquam, nunc nisl aliquet nunc, vitae aliquam nisl nunc eu"
        )
      )
    ),
    Post(
      id = 2,
      user = "user2",
      likes = Seq(
        Like("user1", utils.randomDateTime()),
        Like("user3", utils.randomDateTime()),
        Like("user4", utils.randomDateTime()),
        Like("user5", utils.randomDateTime()),
        Like("user6", utils.randomDateTime()),
        Like("user7", utils.randomDateTime()),
        Like("user8", utils.randomDateTime())
      ),
      imageName = "image-2.jpg",
      createdAt = utils.randomDateTime(),
      description =
        "Nullam euismod, nisl eget ultricies aliquam, nunc nisl aliquet nunc, vitae aliquam nisl nunc eu",
      comments = Seq(
        Comment(
          "user1",
          "Lorem ipsum dolor sit amet, consectetur adipiscing elit."
        ),
        Comment(
          "user3",
          "Nullam euismod, nisl eget ultricies aliquam, nunc nisl aliquet nunc, vitae aliquam nisl nunc eu"
        ),
        Comment(
          "user4",
          "Lorem ipsum dolor sit amet, consectetur adipiscing elit."
        ),
        Comment(
          "user5",
          "Nullam euismod, nisl eget ultricies aliquam, nunc nisl aliquet nunc, vitae aliquam nisl nunc eu"
        ),
        Comment(
          "user6",
          "Lorem ipsum dolor sit amet, consectetur adipiscing elit."
        ),
        Comment(
          "user7",
          "Nullam euismod, nisl eget ultricies aliquam, nunc nisl aliquet nunc, vitae aliquam nisl nunc eu"
        ),
        Comment(
          "user8",
          "Lorem ipsum dolor sit amet, consectetur adipiscing elit."
        )
      )
    )
  )
}
