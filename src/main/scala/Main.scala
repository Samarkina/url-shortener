import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._

import scala.io.StdIn
import com.redis._
import service.{RedisService, UrlShortenerService}
import util.{Coder, HtmlTemplates}

import scala.concurrent.duration.DurationInt


object Main extends App with RedisConfig with HttpConfig {
  val EXPIRE_REDIS_TIME = 30.minutes

  def main(): Unit = {
    implicit val system = ActorSystem(Behaviors.empty, "my-system")
    // needed for the future flatMap/onComplete in the end
    implicit val executionContext = system.executionContext

    // redis client
    val redisService = RedisService(new RedisClient(redisHost, redisPort))
    val urlShortenerService = UrlShortenerService(redisService)

    val shortenerRoute =
      pathPrefix("shortener") {
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

    val mainPageRoute =
      pathEndOrSingleSlash {
        complete(HtmlTemplates.createMainPage())
      }

    val bindingFuture = Http().newServerAt(httpHost, httpPort).bind(shortenerRoute ~ mainPageRoute)

    println(s"Server now online. Please navigate to http://localhost:8080/\nPress RETURN to stop...")
    StdIn.readLine() // let it run until user presses return
    bindingFuture
      .flatMap(_.unbind()) // trigger unbinding from the port
      .onComplete(_ => system.terminate()) // and shutdown when done

  }

  main()
}