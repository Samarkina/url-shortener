import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._

import scala.io.StdIn
import com.redis._

import scala.concurrent.duration.DurationInt


object Main extends App with RedisConfig with HttpConfig {
  val EXPIRE_REDIS_TIME = 30.minutes

  def main(): Unit = {
    implicit val system = ActorSystem(Behaviors.empty, "my-system")
    // needed for the future flatMap/onComplete in the end
    implicit val executionContext = system.executionContext

    // redis client
//    val redisClient = new RedisClient(redisHost, redisPort)
    val redisService = RedisService(new RedisClient(redisHost, redisPort))
    val urlShortenerService = UrlShortenerService(redisService)

    val shortRoute =
      path("hello") {
        get {
          parameters("url") { url =>
            // check value in Redis
            val encodedShortUrlSuffix = redisService.getValue(url, "url")

            if (encodedShortUrlSuffix.isEmpty) {
              // create a new urlShort
              urlShortenerService.createNewShortUrlSuffix(url)
            }
            else {
              // use existing urlShort
              urlShortenerService.useExistingShortUrlSuffix(encodedShortUrlSuffix)
            }
          }
        }
      }

    val getRoute =
      pathPrefix("hello") {
        concat(
          path(IntNumber) { shortUrlSuffix =>
            val encodedLongUrl = redisService.getValue(shortUrlSuffix.toString, "urlShort")
            val decodedLongUrl = Uri(Coder.decodeData(encodedLongUrl))

            extractUri { uri =>
              complete(HttpResponse(
                status = StatusCodes.PermanentRedirect,
                headers = headers.Location(decodedLongUrl) :: Nil,
                entity = StatusCodes.PermanentRedirect.htmlTemplate match {
                  case ""       => HttpEntity.Empty
                  case template => HttpEntity(ContentTypes.`text/html(UTF-8)`, template format uri)
                }))
            }
          }
        )
      }

    val fieldRoute =
      pathEndOrSingleSlash {
        val content =
          """<html>
            |<body>
            |<form action="/hello" method="GET">
            |    <p>Please enter some text below:</p>
            |    <input type="text" name="url">
            |</form>
            |</body>
            |</html>""".stripMargin

        urlShortenerService.createHTMLPage(content)
      }

    val bindingFuture = Http().newServerAt(httpHost, httpPort).bind(shortRoute ~ getRoute ~ fieldRoute)

    println(s"Server now online. Please navigate to http://localhost:8080/\nPress RETURN to stop...")
    StdIn.readLine() // let it run until user presses return
    bindingFuture
      .flatMap(_.unbind()) // trigger unbinding from the port
      .onComplete(_ => system.terminate()) // and shutdown when done

  }
  main()
}