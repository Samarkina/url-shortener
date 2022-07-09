import akka.http.scaladsl.model.{ContentTypes, HttpEntity, Uri}
import akka.http.scaladsl.server.Directives.{complete, extractUri}
import akka.http.scaladsl.server.{RequestContext, RouteResult, StandardRoute}
import util.{Coder, HtmlTemplates}

import scala.concurrent.Future
import scala.math.abs

case class UrlShortenerService(redisService: RedisService) {
  def createHash(data: String): String = {
    s"${abs(data.hashCode()).toString}"
  }

  def createNewShortUrl(url: String, uri: Uri): String = {
    // create a new shortUrlSuffix
    val shortUrlSuffix = createHash(url)

    redisService.setValue(key = shortUrlSuffix, value = url, "urlShort")
    redisService.setValue(key = url, value = shortUrlSuffix, "url")
    s"${uri.scheme}://${uri.authority}${uri.path}/$shortUrlSuffix"
  }

  def useExistingShortUrl(decodedShortUrlSuffix: String, uri:Uri): String = {
    // use existing shortUrlSuffix
    s"${uri.scheme}://${uri.authority}${uri.path}/$decodedShortUrlSuffix"
  }



}
