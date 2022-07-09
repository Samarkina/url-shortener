import akka.actor.ActorSystem
import org.scalatest.BeforeAndAfterAll
import com.github.sebruck.EmbeddedRedis
import com.redis.RedisClient
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import service.RedisService


class RedisServiceTest extends AnyWordSpecLike with Matchers with EmbeddedRedis with BeforeAndAfterAll {
  //This actor system will be used by `rediscala` Redis Client
  private implicit val actorSystem: ActorSystem = ActorSystem()

  override def afterAll(): Unit = actorSystem.terminate()

  "The service" should {
    "save value by encoded key" in {
      withRedis() { port =>
        // given
        val key = "12345"
        val encodedKey = "MTIzNDU="
        val value = "https://leetcode.com/problems/valid-parentheses/"

        val redisClient = new RedisClient(host = "localhost", port = port)
        val redisService = RedisService(redisClient)

        // actual
        redisService.setValue(key, value, "urlShort")
        val result = redisClient.get(s"urlShort:$encodedKey")

        // expected
        result.get shouldEqual value
      }
    }
  }

  "The service" should {
    "get value by encoded key" in {
      withRedis() { port =>
        // given
        val key = "12345"
        val encodedKey = "MTIzNDU="
        val value = "https://leetcode.com/problems/valid-parentheses/"

        val redisClient = new RedisClient(host = "localhost", port = port)
        val redisDataStore = service.RedisService(redisClient)

        // actual
        redisClient.set(key=s"urlShort:$encodedKey", value=value)

        val result = redisDataStore.getValue(key, "urlShort")

        // expected
        result shouldEqual value
      }
    }
  }
}
