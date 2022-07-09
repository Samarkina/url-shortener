import Main.EXPIRE_REDIS_TIME
import akka.http.scaladsl.model.{ContentTypes, HttpEntity}
import akka.http.scaladsl.server.Directives.{complete, extractUri}
import akka.http.scaladsl.server.{RequestContext, RouteResult, StandardRoute}
import com.redis.RedisClient

import scala.concurrent.Future
import scala.math.abs

case class UrlShortenerService(redisService: RedisService) {
  def createHash(data: String): String = {
    s"${abs(data.hashCode()).toString}"
  }

  def createNewShortUrlSuffix(url: String): RequestContext => Future[RouteResult] = {
    {
      // create a new shortUrlSuffix
      val shortUrlSuffix = createHash(url)

      extractUri { uri =>
        redisService.setValue(key=shortUrlSuffix, value=url, "urlShort")
        redisService.setValue(key=url, value=shortUrlSuffix, "url")
        val link = s"${uri.scheme}://${uri.authority}${uri.path}/$shortUrlSuffix"

        createHTMLLinkPage(link)
      }
    }
  }

  def useExistingShortUrlSuffix(encodedShortUrlSuffix: Option[String]): RequestContext => Future[RouteResult] = {
    // use existing shortUrlSuffix
    val decodedShortUrlSuffix = Coder.decodeData(encodedShortUrlSuffix)
    extractUri { uri =>
      val link = s"${uri.scheme}://${uri.authority}${uri.path}/$decodedShortUrlSuffix"

      createHTMLLinkPage(link)
    }
  }

  def createHTMLPage(content: String): StandardRoute = {
    complete(
      HttpEntity(
        ContentTypes.`text/html(UTF-8)`,
        content
      )
    )
  }

  def createHTMLLinkPage(link: String): StandardRoute = {
    val content =
      s"""<html>
         |<body>
         |<form action="/hello" method="GET">
         |    <p>Your short URL version is <a href=\"$link\">$link</a></p>
         |</form>
         |</body>
         |</html>""".stripMargin
    createHTMLPage(content)
  }

}
