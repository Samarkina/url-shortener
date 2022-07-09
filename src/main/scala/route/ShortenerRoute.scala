package route

import akka.http.scaladsl.model._
import akka.http.scaladsl.server.{Directives, Route}
import service.{RedisService, UrlShortenerService}
import util.HtmlTemplates

case class ShortenerRoute(urlShortenerService: UrlShortenerService, redisService: RedisService)
  extends Directives
  with HttpConfig {

  val routes: Route = pathPrefix("shortener") {
    concat(
      get {
        parameters("url") { url =>
          extractUri { uri =>
            try {
              // check value in Redis
              val encodedShortUrlSuffix = redisService.getValue(url, "url")
              val link = urlShortenerService.useExistingShortUrl(encodedShortUrlSuffix, uri)
              complete(HtmlTemplates.createResultLinkPage(link))
            } catch {
              case e: IllegalArgumentException =>
                val link = urlShortenerService.createNewShortUrl(url, uri)
                complete(HtmlTemplates.createResultLinkPage(link))
            }
          }
        }
      },
      path(IntNumber) { shortUrlSuffix =>
        extractUri { uri =>
          try {
            val longUrl = redisService.getValue(shortUrlSuffix.toString, "urlShort")
            complete(HttpResponse(
              status = StatusCodes.PermanentRedirect,
              headers = headers.Location(longUrl) :: Nil,
              entity = StatusCodes.PermanentRedirect.htmlTemplate match {
                case "" => HttpEntity.Empty
                case template => HttpEntity(ContentTypes.`text/html(UTF-8)`, template format uri)
              }))
          } catch {
            case e: IllegalArgumentException => complete(HttpResponse(
              status = StatusCodes.NotFound,
              entity = HtmlTemplates.createNotFoundPage(e.getMessage)))
          }
        }
      }
    )
  }
}
