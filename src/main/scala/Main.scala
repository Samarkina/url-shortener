import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import scala.io.StdIn
import com.redis._


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
            val defaultShortCodeServiceObject = new DefaultShortCodeService
            val urlShort = defaultShortCodeServiceObject.create(url)
            extractUri { uri =>
              // push
              redisClient.set(s"$urlShort", s"$url")
              complete(s"Your short URL version is ${uri.scheme}://${uri.authority}${uri.path}/$urlShort")
            }
          }
        }
      }

    val route2 =
      pathPrefix("hello") {
        concat(
          path(IntNumber) { url =>
            // get
            val originalUrl = Uri(redisClient.get(s"$url").get)
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