package utils

def getSessionUsername(
    session: play.api.mvc.Session
): Option[String] = {
  session.get(models.Global.SESSION_USERNAME_KEY)
}
