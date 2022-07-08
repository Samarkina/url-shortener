import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import scala.io.StdIn
import com.redis._
import java.util.Base64
import java.nio.charset.StandardCharsets


object Main {
  def main(args: Array[String]): Unit = {
    implicit val system = ActorSystem(Behaviors.empty, "my-system")
    // needed for the future flatMap/onComplete in the end
    implicit val executionContext = system.executionContext

    // redis client
    val redisClient = new RedisClient("localhost", 6379)

    val route =
      path("hello") {
        get {
          parameters("url") { url =>
            // check value in Redis
            val encodedRedisUrlShort = redisClient.get(s"url:$url")

            if (encodedRedisUrlShort.isEmpty) {
              // create a new urlShort
              val defaultShortCodeServiceObject = new DefaultShortCodeService
              val urlShort = defaultShortCodeServiceObject.create(url)

              extractUri { uri =>
                // encoding only short part of url (suffix)
                val base64fullUrlShortPath = Base64.getEncoder.encodeToString(urlShort.getBytes(StandardCharsets.UTF_8))
                val base64url = Base64.getEncoder.encodeToString(url.getBytes(StandardCharsets.UTF_8))

                // push urlShort to Redis
                redisClient.set(s"urlShort:$base64fullUrlShortPath", s"$base64url")
                redisClient.set(s"url:$base64url", s"$base64fullUrlShortPath")

                complete(s"Your short URL version is ${uri.scheme}://${uri.authority}${uri.path}/$urlShort")
              }
            }
            else {
              // use existing urlShort
              val decodedArray = Base64.getDecoder.decode(encodedRedisUrlShort.get)
              val decodedRedisUrlShort = new String(decodedArray, StandardCharsets.UTF_8)

              extractUri { uri =>
                complete(s"Your short URL version is ${uri.scheme}://${uri.authority}${uri.path}/$decodedRedisUrlShort")
              }
            }
          }
        }
      }

    val route2 =
      pathPrefix("hello") {
        concat(
          path(IntNumber) { shortUrl =>
            val encodedUrl = Base64.getEncoder.encodeToString(shortUrl.toString.getBytes(StandardCharsets.UTF_8))

            // get long url from Redis
            val encodedRedisUrlLong = redisClient.get(s"urlShort:$encodedUrl")

            val decodedArray = Base64.getDecoder.decode(encodedRedisUrlLong.get)
            val decodedRedisUrlShort = new String(decodedArray, StandardCharsets.UTF_8)
            val originalUrl = Uri(decodedRedisUrlShort)

            extractUri { uri =>
              complete(HttpResponse(
                status = StatusCodes.PermanentRedirect,
                headers = headers.Location(originalUrl) :: Nil,
                entity = StatusCodes.PermanentRedirect.htmlTemplate match {
                  case ""       => HttpEntity.Empty
                  case template => HttpEntity(ContentTypes.`text/html(UTF-8)`, template format uri)
                }))
            }
          }
        )
      }

    val bindingFuture = Http().newServerAt("localhost", 8080).bind(route ~ route2)

    println(s"Server now online. Please navigate to http://localhost:8080/hello\nPress RETURN to stop...")
    StdIn.readLine() // let it run until user presses return
    bindingFuture
      .flatMap(_.unbind()) // trigger unbinding from the port
      .onComplete(_ => system.terminate()) // and shutdown when done

  }
}