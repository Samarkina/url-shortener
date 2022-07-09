import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._

import scala.io.StdIn
import com.redis._
import route.{ShortenerRoute, HttpConfig, MainPageRoute, RedisConfig}
import service.{RedisService, UrlShortenerService}


object Main extends App with RedisConfig with HttpConfig {

  def main(): Unit = {
    implicit val system = ActorSystem(Behaviors.empty, "my-system")
    // needed for the future flatMap/onComplete in the end
    implicit val executionContext = system.executionContext

    val redisService = RedisService(new RedisClient(redisHost, redisPort))
    val urlShortenerService = UrlShortenerService(redisService)

    val shortenerRoute = ShortenerRoute(urlShortenerService, redisService)
    val mainPageRoute = MainPageRoute()

    val bindingFuture = Http().newServerAt(httpHost, httpPort).bind(shortenerRoute.routes ~ mainPageRoute.routes)

    println(s"Server now online. Please navigate to http://localhost:8080/\nPress RETURN to stop...")
    StdIn.readLine() // let it run until user presses return
    bindingFuture
      .flatMap(_.unbind()) // trigger unbinding from the port
      .onComplete(_ => system.terminate()) // and shutdown when done
  }

  main()
}