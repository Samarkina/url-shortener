package util

import akka.http.scaladsl.model.{ContentTypes, HttpEntity}

object HtmlTemplates {

  def createNotFoundPage(message: String): HttpEntity.Strict = {
    val content =
      s"""<html>
        |<body>
        |<p>$message.</p>
        |</body>
        |</html>""".stripMargin

    HttpEntity(
      ContentTypes.`text/html(UTF-8)`,
      content
    )
  }

  def createMainPage(): HttpEntity.Strict = {
    val content =
      """<html>
        |<body>
        |<form action="/shortener" method="GET">
        |    <p>Please enter some text below:</p>
        |    <input type="text" name="url">
        |</form>
        |</body>
        |</html>""".stripMargin

    HttpEntity(
      ContentTypes.`text/html(UTF-8)`,
      content
    )
  }

  def createResultLinkPage(link: String): HttpEntity.Strict = {
    val content =
      s"""<html>
         |<body>
         |<form action="/shortener" method="GET">
         |  <p>Your short URL version is <a href=\"$link\">$link</a></p>
         |</form>
         |</body>
         |</html>""".stripMargin
    HttpEntity(
      ContentTypes.`text/html(UTF-8)`,
      content
    )
  }
}
