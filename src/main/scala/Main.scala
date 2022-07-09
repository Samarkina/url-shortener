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
    val redisClient = new RedisClient(redisHost, redisPort)

    val route =
      path("hello") {
        get {
          parameters("url") { url =>
            // check value in Redis
            val encodedShortUrlSuffix = RedisService.getEncodedDataFromRedis(url, redisClient, "url")

            if (encodedShortUrlSuffix.isEmpty) {
              // create a new urlShort
              val urlShort = UrlShortnerService.hashCreate(url)

              extractUri { uri =>
                RedisService.setDataToRedis(urlShort, redisClient, EXPIRE_REDIS_TIME, "urlShort")
                RedisService.setDataToRedis(url, redisClient, EXPIRE_REDIS_TIME, "url")
                val link = s"${uri.scheme}://${uri.authority}${uri.path}/$urlShort"

                val content = s"""<html>
                                |<body>
                                |<form action="/hello" method="GET">
                                |    <p>Your short URL version is <a href=\"$link\">$link</a></p>
                                |</form>
                                |</body>
                                |</html>""".stripMargin
                complete(
                  HttpEntity(
                    ContentTypes.`text/html(UTF-8)`,
                    content
                  )
                )
              }
            }
            else {
              // use existing urlShort
              val decodedUrlShort = Coder.decodeData(encodedShortUrlSuffix)
              extractUri { uri =>
                val link = s"${uri.scheme}://${uri.authority}${uri.path}/$decodedUrlShort"

                lazy val content = s"""<html>
                                |<body>
                                |<form action="/hello" method="GET">
                                |    <p>Your short URL version is <a href=\"$link\">$link</a></p>
                                |</form>
                                |</body>
                                |</html>""".stripMargin
                complete(
                  HttpEntity(
                    ContentTypes.`text/html(UTF-8)`,
                    content
                  )
                )
              }
            }
          }
        }
      }

    val route2 =
      pathPrefix("hello") {
        concat(
          path(IntNumber) { shortUrlSuffix =>
            val encodedRedisUrlLong = RedisService.getEncodedDataFromRedis(shortUrlSuffix.toString, redisClient, "urlShort")
            val decodedRedisUrlLong = Uri(Coder.decodeData(encodedRedisUrlLong))

            extractUri { uri =>
              complete(HttpResponse(
                status = StatusCodes.PermanentRedirect,
                headers = headers.Location(decodedRedisUrlLong) :: Nil,
                entity = StatusCodes.PermanentRedirect.htmlTemplate match {
                  case ""       => HttpEntity.Empty
                  case template => HttpEntity(ContentTypes.`text/html(UTF-8)`, template format uri)
                }))
            }
          }
        )
      }

    val routeField =
      pathEndOrSingleSlash {
        val content = """<html>
                        |<body>
                        |<form action="/hello" method="GET">
                        |    <p>Please enter some text below:</p>
                        |    <input type="text" name="url">
                        |</form>
                        |</body>
                        |</html>""".stripMargin
        complete(
          HttpEntity(
          ContentTypes.`text/html(UTF-8)`,
          content
          )
        )
      }

    val bindingFuture = Http().newServerAt(httpHost, httpPort).bind(route ~ route2 ~ routeField)

    println(s"Server now online. Please navigate to http://localhost:8080/\nPress RETURN to stop...")
    StdIn.readLine() // let it run until user presses return
    bindingFuture
      .flatMap(_.unbind()) // trigger unbinding from the port
      .onComplete(_ => system.terminate()) // and shutdown when done

  }
  main()
}