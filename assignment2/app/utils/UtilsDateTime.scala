package utils

import java.time.format.DateTimeFormatter
import java.time.{Instant, LocalDateTime, ZoneId, ZonedDateTime}
import java.util.concurrent.ThreadLocalRandom
val dateTimeFormat =
  DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
val dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd")

def displayDateTime(dateTime: ZonedDateTime): String = {
  dateTime.format(dateTimeFormat)
}

def displayDate(dateTime: ZonedDateTime): String = {
  dateTime.format(dateFormat)
}

def randomDateTime(): ZonedDateTime = {
  val start = LocalDateTime.of(2020, 1, 1, 0, 0)
  val end = LocalDateTime.of(2023, 12, 31, 23, 59)
  val random = ThreadLocalRandom
    .current()
    .nextLong(
      start.toEpochSecond(ZoneId.systemDefault().getRules.getOffset(start)),
      end.toEpochSecond(ZoneId.systemDefault().getRules.getOffset(end))
    )
  ZonedDateTime.ofInstant(Instant.ofEpochSecond(random), ZoneId.systemDefault())
}
