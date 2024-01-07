package models

import java.time.ZonedDateTime

case class Like(user: String, createdAt: ZonedDateTime = ZonedDateTime.now())
