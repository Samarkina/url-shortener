import Main.EXPIRE_REDIS_TIME
import akka.actor.ActorSystem
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpec}
import com.github.sebruck.EmbeddedRedis
import com.redis.RedisClient


class RedisServiceTest extends WordSpec with Matchers with EmbeddedRedis with BeforeAndAfterAll {
  //This actor system will be used by `rediscala` Redis Client
  private implicit val actorSystem: ActorSystem = ActorSystem()

  override def afterAll(): Unit = actorSystem.terminate()

  "Service" should {
    "save value by encoded key" in {
      withRedis() { port =>
        // given
        val key = "12345"
        val encodedKey = "MTIzNDU="
        val value = "https://leetcode.com/problems/valid-parentheses/"

        val redisClient = new RedisClient(host = "localhost", port = port)
        val redisDataStore = RedisService(redisClient)

        // actual
        redisDataStore.setValue(key, value, "urlShort")
        val result = redisClient.get(s"urlShort:$encodedKey")

        // expected
        result.get shouldEqual value
      }
    }
  }

  "Service" should {
    "get value by encoded key" in {
      withRedis() { port =>
        // given
        val key = "12345"
        val encodedKey = "MTIzNDU="
        val value = "https://leetcode.com/problems/valid-parentheses/"

        val redisClient = new RedisClient(host = "localhost", port = port)
        val redisDataStore = RedisService(redisClient)

        // actual
        redisClient.set(key=s"urlShort:$encodedKey", value=value)

        val result = redisDataStore.getValue(key, "urlShort")

        // expected
        result.get shouldEqual value
      }
    }
  }
}
