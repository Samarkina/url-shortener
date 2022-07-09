import akka.actor.ActorSystem
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.model.headers.Location
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.testkit.{ImplicitSender, TestKit}
import com.github.sebruck.EmbeddedRedis
import com.redis.RedisClient
import org.scalatest.BeforeAndAfterAll
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.{AnyWordSpec, AnyWordSpecLike}
import route.ShortenerRoute
import service.{RedisService, UrlShortenerService}

class ShortenerRouteTest
    extends AnyWordSpec
    with EmbeddedRedis
    with Matchers
    with ScalatestRouteTest
    with BeforeAndAfterAll {

  override def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system)
  }

  "The service" should {
    "redirect to long URL when a short url exists" in {
      withRedis() { port =>
        // given
        val key = "12345"
        val encodedKey = "MTIzNDU="
        val value = "https://leetcode.com/problems/valid-parentheses/"

        val redisClient = new RedisClient(host = "localhost", port = port)
        val redisService = RedisService(redisClient)
        val urlShortenerService = UrlShortenerService(redisService)

        redisClient.set(s"urlShort:$encodedKey", value)

        // actual
        Get(s"/shortener/$key") ~> ShortenerRoute(urlShortenerService, redisService).routes ~> check {
          // expected
          status shouldEqual StatusCodes.PermanentRedirect
          header[Location].get.value() shouldEqual value
        }
      }
    }
  }

  "The service" should {
    "return 404 on invalid short url" in {
      withRedis() { port =>
        // given
        val key = "00000"
        val encodedKey = "MTIzNDU="
        val value = "https://leetcode.com/problems/valid-parentheses/"

        val redisClient = new RedisClient(host = "localhost", port = port)
        val redisService = RedisService(redisClient)
        val urlShortenerService = UrlShortenerService(redisService)

        redisClient.set(s"urlShort:$encodedKey", value)

        // actual
        Get(s"/shortener/$key") ~> ShortenerRoute(urlShortenerService, redisService).routes ~> check {
          // expected
          status shouldEqual StatusCodes.NotFound
        }
      }
    }
  }
}